package utils;

import modelling.Modeller;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.color.IAtomColorer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.*;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple utility class with some depiction methods.
 *
 * Created by sicho on 10/5/16.
 */
public class Depictor {

    private static final String is_som_prop = Modeller.is_som_fld;
    private RendererModel model;
    private Graphics2D graphics;
    private Image image;
    private AtomContainerRenderer renderer;
    private int side;

    public static class SoMColorer implements IAtomColorer {

        @Override
        public Color getAtomColor(IAtom iAtom) {
            if (iAtom.getProperty(is_som_prop) != null) {
                boolean som_status = (Boolean) iAtom.getProperty(is_som_prop);
                if (som_status) {
                    return Color.MAGENTA;
                }
            }

            if (iAtom.getSymbol().equals("H")) {
                return Color.GRAY;
            } else if (iAtom.getSymbol().equals("N")) {
                return Color.BLUE;
            } else if (iAtom.getSymbol().equals("O")) {
                return Color.RED;
            } else if (iAtom.getSymbol().equals("S")) {
                return Color.YELLOW;
            } else if (iAtom.getSymbol().equals("C")) {
                return Color.BLACK;
            } else {
                return Color.CYAN;
            }
        }

        @Override
        public Color getAtomColor(IAtom iAtom, Color color) {
            return getAtomColor(iAtom);
        }
    }

    public Depictor() {
        // make generators
        List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
        generators.add(new BasicSceneGenerator());
        generators.add(new BasicBondGenerator());
        generators.add(new RingGenerator());
        generators.add(new BasicAtomGenerator());
        generators.add(new AtomNumberGenerator());

        // setup the renderer
        renderer = new AtomContainerRenderer(generators, new AWTFontManager());
        model = renderer.getRenderer2DModel();
//		model.set(BasicAtomGenerator.CompactAtom.class, true);
//		model.set(BasicAtomGenerator.CompactShape.class, BasicAtomGenerator.Shape.OVAL);
        model.set(BasicAtomGenerator.KekuleStructure.class, true);
        model.set(BasicAtomGenerator.ShowExplicitHydrogens.class, true);
        model.set(AtomNumberGenerator.WillDrawAtomNumbers.class, true);
        model.set(AtomNumberGenerator.ColorByType.class, true);

        // get the image
        side = 3 * 400;
        image = new BufferedImage(side, side, BufferedImage.TYPE_4BYTE_ABGR);
        graphics = (Graphics2D)image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill(new Rectangle2D.Double(0, 0, side, side));
    }

    public Depictor(SoMColorer colorer) {
        this();
        model.set(AtomNumberGenerator.AtomColorer.class, colorer);
    }

    /**
     * generate a PNG file depicting a molecule with SOMs highlighted
     *
     * @param molecule the molecule
     * @param path path to the output file
     * @throws Exception thrown if sth breaks
     */
    public void generateDepiction(IMolecule molecule, String path) throws Exception {
        // layout the molecule
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(molecule, false);
        sdg.generateCoordinates();

        // paint
        renderer.paint(molecule, new AWTDrawVisitor(graphics),
                new Rectangle2D.Double(0, 0, side, side), true);
        graphics.dispose();

        // write to file
        File file = new File(path);
        ImageIO.write((RenderedImage)image, "PNG", file);
    }
}
