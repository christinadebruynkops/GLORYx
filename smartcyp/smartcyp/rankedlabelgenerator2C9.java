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

import java.awt.Color;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

import smartcyp.MoleculeKU.SMARTCYP_PROPERTY;

public class rankedlabelgenerator2C9 implements IGenerator<IAtomContainer> {
	       
	       public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
	    	   IAtom atom;
	           // this contains the rendering elements
	           ElementGroup rankedCircles = new ElementGroup();
	           
	           // most parameters are in screen space, and have to be scaled
	           double r = model.getParameter(BasicAtomGenerator.AtomRadius.class).getValue() / model.getParameter(BasicSceneGenerator.Scale.class).getValue();
	           
	           // make a circle at each ranked atom
	           for (int atomIndex=0; atomIndex < ac.getAtomCount(); atomIndex++) {
	        	   atom = ac.getAtom(atomIndex);
	        	   if (SMARTCYP_PROPERTY.Ranking2C9.get(atom) != null && SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 1){
	        		   Point2d p = atom.getPoint2d();
		               
		               IRenderingElement oval = 
		                   new OvalElement(p.x, p.y, r, true, new Color(255,204,102));
		               
		               rankedCircles.add(oval);
	        	   }
	        	   if (SMARTCYP_PROPERTY.Ranking2C9.get(atom) != null && SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 2){
	        		   Point2d p = atom.getPoint2d();
		               
		               IRenderingElement oval = 
		                   new OvalElement(p.x, p.y, r, true, new Color(223,189,174));
		               
		               rankedCircles.add(oval);
	        	   }
	        	   if (SMARTCYP_PROPERTY.Ranking2C9.get(atom) != null && SMARTCYP_PROPERTY.Ranking2C9.get(atom).intValue() == 3){
	        		   Point2d p = atom.getPoint2d();
		               
		               IRenderingElement oval = 
		                   new OvalElement(p.x, p.y, r, true, new Color(214,227,181));
		               
		               rankedCircles.add(oval);
	        	   }

	           }
	           return rankedCircles;
	       }

			@Override
			public List<IGeneratorParameter<?>> getParameters() {
				// TODO Auto-generated method stub
				return null;
			}
	   }

