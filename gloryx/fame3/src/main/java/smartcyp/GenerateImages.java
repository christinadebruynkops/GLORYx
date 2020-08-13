/* 
 * Copyright (C) 2010-2011  David Gloriam <davidgloriam@googlemail.com> & Patrik Rydberg <patrik.rydberg@gmail.com>
 * 
 * Contact: smartcyp@farma.ku.dk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package smartcyp;


import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.geometry.Projector;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;


// This class writes the results to a HTML file
// This class is the last to be called
public class GenerateImages {


	// The draw area and the image should be the same size
	private int WIDTH = 800;
	private int HEIGHT = 400;
	private Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
	private Image image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	private String dateAndTime;
	private String OutputDir;




	public GenerateImages(String dateTime, String outputdir){
		dateAndTime = dateTime;
		OutputDir = outputdir;
	}



	// This is the "main" method that calls all other methods below
	public void generateAndWriteImages(AtomContainerSet moleculeSet) throws CloneNotSupportedException, CDKException{



		if(moleculeSet == null){
			System.out.println("moleculeSet is null");
		}

		//Renderer renderer;

		// Create smartcyp_images directory
		this.createDirectory(OutputDir + "smartcyp_images_" + dateAndTime);
		
		// Iterate MoleculKUs
		IAtomContainer iAtomContainer;
		for (int moleculeIndex=0; moleculeIndex < moleculeSet.getAtomContainerCount(); moleculeIndex++) {

			iAtomContainer = moleculeSet.getAtomContainer(moleculeIndex); 


			//			iAtomContainer = AtomContainerManipulator.removeHydrogensPreserveMultiplyBonded(iAtomContainer);

			// Generate 2D coordinates for moleculeKU
			iAtomContainer = this.generate2Dcoordinates(iAtomContainer);

			//String id = moleculeSet.getMolecule(moleculeIndex).getID();
			// System.out.println(id);

			iAtomContainer.setID(moleculeSet.getAtomContainer(moleculeIndex).getID());

			// Generators make the image elements
			List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
			generators.add(new BasicSceneGenerator());
			generators.add(new RingGenerator());
			generators.add(new BasicAtomGenerator());
			generators.add(new AtomNumberGenerator());
			
			

			// The renderer renders the picture
			//AWTFontManager fontman = new AWTFontManager();
			//renderer = new Renderer(generators, fontman, false);
			AtomContainerRenderer renderer =
				  new AtomContainerRenderer(generators, new AWTFontManager());
			renderer.setup(iAtomContainer, drawArea);
			
			// Set layout of molecule
			// This method is not used because default layout looks ok
			// this.setMoleculeLayout(r2dm);


			// Write 2 types of images with: 1) heteroatoms and 2) atom Numbers
			renderer.getRenderer2DModel().set(AtomNumberGenerator.ColorByType.class, true);
			this.paintAndWriteMolecule(renderer, iAtomContainer, "atomNumbers", OutputDir);
			generators.removeAll(generators);
			generators.add(new BasicSceneGenerator());
			generators.add(new rankedlabelgenerator());
			generators.add(new RingGenerator());
			generators.add(new BasicAtomGenerator());
			this.paintAndWriteMolecule(renderer, iAtomContainer, "heteroAtoms", OutputDir);	
			generators.removeAll(generators);
			generators.add(new BasicSceneGenerator());
			generators.add(new rankedlabelgenerator2D6());
			generators.add(new RingGenerator());
			generators.add(new BasicAtomGenerator());
			this.paintAndWriteMolecule(renderer, iAtomContainer, "heteroAtoms2D6", OutputDir);
			generators.removeAll(generators);
			generators.add(new BasicSceneGenerator());
			generators.add(new rankedlabelgenerator2C9());
			generators.add(new RingGenerator());
			generators.add(new BasicAtomGenerator());
			this.paintAndWriteMolecule(renderer, iAtomContainer, "heteroAtoms2C9", OutputDir);

		}
	}





	// Generates 2D coordinates of molecules
	public IAtomContainer generate2Dcoordinates(IAtomContainer iAtomContainer){ 


		IAtomContainer molecule = new AtomContainer(iAtomContainer);


		//		boolean isConnected = ConnectivityChecker.isConnected(iAtomContainer);
		//		System.out.println("isConnected " + isConnected);

		final StructureDiagramGenerator structureDiagramGenerator = new StructureDiagramGenerator();

		// Generate 2D coordinates?
		if (GeometryTools.has2DCoordinates(iAtomContainer))
		{
			// System.out.println(iAtomContainer.toString() + " already had 2D coordinates");
			return iAtomContainer; // already has 2D coordinates.
		}
		else
		{

			// Generate 2D structure diagram (for each connected component).
			final IAtomContainer iAtomContainer2d = new AtomContainer();	

			/*
			final IMoleculeSet som = ConnectivityChecker.partitionIntoMolecules(iAtomContainer);
			for (int n = 0;
			n < som.getMoleculeCount();
			n++)
			{
			 */
			synchronized (structureDiagramGenerator)
			{
				//				IMolecule molecule = som.getMolecule(n);


				structureDiagramGenerator.setMolecule(molecule, true);
				try
				{
					// Generate 2D coords for this molecule.
					structureDiagramGenerator.generateCoordinates();
					molecule = structureDiagramGenerator.getMolecule();
				}
				catch (final Exception e)
				{
					// Use projection instead.
					Projector.project2D(molecule);
					System.out.println("Exception in generating 2D coordinates");
					e.printStackTrace();     			
				}

				iAtomContainer2d.add(molecule);  		// add 2D molecule.		

			}

			/*
	     		// Test
	     		Atom atom;
	     		for(int atomIndex = 0; atomIndex < iAtomContainer2d.getAtomCount(); atomIndex++){
	     			atom = (Atom) iAtomContainer2d.getAtom(atomIndex);
	     			System.out.println("atom.getPoint2d(): " + atom.getPoint2d());
	     		}
			 */
			//			}
			if(GeometryTools.has2DCoordinates(iAtomContainer2d)) return  iAtomContainer2d;
			else {
				System.out.println("Generating 2D coordinates for " + iAtomContainer2d + " failed.");
				return null;
			}
		}	
	}


	// Creates a directory with the name directoryName
	public void createDirectory(String directoryName){
		try{

			// Create one directory
			boolean success = (new File(directoryName)).mkdir();
			if (success) {
				System.out.println("Directory: " + directoryName + " created");
			}    

		}catch (Exception e){//Catch exception if any
			System.err.println("Could not create image directory smartcyp_images \n Error: " + e.getMessage());
		}
	}




	public void paintAndWriteMolecule(AtomContainerRenderer renderer, IAtomContainer iAtomContainer, String nameBase, String outputdir){


		// Paint background
		Graphics2D g2 = (Graphics2D)image.getGraphics();
		//	g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, WIDTH, HEIGHT);

		// the paint method also needs a toolkit-specific renderer
		renderer.paint(iAtomContainer, new AWTDrawVisitor(g2), drawArea, true);
		
		String moleculeID = iAtomContainer.getID();
		//System.out.println(moleculeID);
		String moleculeIDstartingFromOne = Integer.toString(Integer.parseInt(moleculeID));
		String fileName = outputdir + "smartcyp_images_" + this.dateAndTime + File.separator + "molecule_" + moleculeIDstartingFromOne + "_" + nameBase + ".png";
		
		try {ImageIO.write((RenderedImage)image, "PNG", new File(fileName));} 
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Molecule images could not be written to file. " +
			"If you have not already you need to create the directory 'smartcyp_images' in which the images are to be written");
		}

	}
}





