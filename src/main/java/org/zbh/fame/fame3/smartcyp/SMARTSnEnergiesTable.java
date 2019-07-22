/* 
 * Copyright (C) 2010-2013  David Gloriam <davidgloriam@googlemail.com> & Patrik Rydberg <patrik.rydberg@gmail.com>
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

package org.zbh.fame.fame3.smartcyp;

import java.util.HashMap;


public class SMARTSnEnergiesTable {

	// Local Variables
	private HashMap<String, Double> SMARTSnEnergiesTable;		// Lookup table in which the SMARTS are key values to retrieve Energies


	/* Matches atoms against SMARTS patterns and assigns predefined energies
	Input is two columnes 1) SMARTS pattern and 2) Energy
	[CX4;!CH0]	29.7
	[CX3;!CH0]	13.3
	[cX3;!CH0]	0.7
	[N]			999.9
	[S]			44.5
	...
	 */
	public SMARTSnEnergiesTable() {

		// Local Variable
		SMARTSnEnergiesTable = new HashMap<String, Double>();
		
		// Sulphurs without Oxygens
		// Sulphurs with Oxygens
		// Aldehydes
		//Phosphor
		// sp2 Carbons
		// sp2 Carbons
		// Nitrogens

     

		SMARTSnEnergiesTable.put("[SX2H1]", 41.5);
		SMARTSnEnergiesTable.put("[$([SX2H0]);!$([S][*^2]);!$([S][CX4H0]);!$([S][P])]", 26.3);
		SMARTSnEnergiesTable.put("[$([SX2H0][*^2]),$([S][P]=[S]);!$([S](~[^2])[^2]);!$([S][CX4H0])]", 34.5);
		SMARTSnEnergiesTable.put("[$([S][*D4H0]);$([SX2H0])]", 44.4);
		SMARTSnEnergiesTable.put("[$([SX2H0]([*^2])[*^2]);!$([S][CX4H0])]", 46.9);
		SMARTSnEnergiesTable.put("[$([sX2r5]1:[c]2:[c]:[c]:[c]:[c]:[c]2:[c]:[c]1);$([sX2r5])]", 56.9);
		SMARTSnEnergiesTable.put("[sX2r5]", 70.0);
		SMARTSnEnergiesTable.put("[$([#16X3](=[OX1]));$([#16]);!$([#16X3](=[OX1])[#6^2](~[#7^2]));!$([#16X3](=[OX1])[OH1])]", 30.4);
		SMARTSnEnergiesTable.put("[$([#16X3](=[OX1]));$([#16]);$([#16X3](=[OX1])[#6^2](~[#7^2]))]", 46.9);
		SMARTSnEnergiesTable.put("[$([CX3H1](=O)[#6])]", 40.2);
		SMARTSnEnergiesTable.put("[$([S]=[PX4])]", 50.9);
		SMARTSnEnergiesTable.put("[$([S]=[PX4][S])]", 40.0);
		SMARTSnEnergiesTable.put("[$([S]=[C])]", 43.5);
		SMARTSnEnergiesTable.put("[$([CX4][N^3R2r6]1[C]2[C][C][C][C]1[C][C][C]2);!$([CH0])]", 22.9); //new rule in 2.3
		SMARTSnEnergiesTable.put("[$([CX4]1[N^3][C][C]=[C]1);!$([CH0])]", 28.3); //new rule in 2.3
		SMARTSnEnergiesTable.put("[!$([CH0]);$([CX4]([#6^2])([#6^2])[#6^2]),$([CX4][N^3R0][C]=[C]),$([CX4R0,CX4R1][N^3r5]);!$([CX4][N][*^2]);!$([CX4][NX3][#16X4](=[OX1])(=[OX1]))]", 32.8);  //modified in 2.3
		SMARTSnEnergiesTable.put("[$([CX4][N]);!$([CH0]);!$([C][N]([*^2])[*^2]);!$([C][N]=[#6X3]);!$([CX4][NX3][C]=[O]);!$([CX4][NX3][#16X4](=[OX1])(=[OX1]));!$([CX4][NX3][N]=[O]);!$([CX4]1[C][C]2[C][C][N]1[C][C]2);!$([CX4H1](@[C])(@[C])@[N^3]);!$([CX4]1[N][C][C][N]([*^2])[C]1)]", 41.1); //modified in 2.3
		SMARTSnEnergiesTable.put("[!$([CH0]);$([CX4]([C^2]~[C])[C^2]~[C]),$([CX4][#7]=[#6X3]),$([CX4]([#8])[#8]);!$([CX4]([#8])[#8][C]=[O])]", 48.5);
		SMARTSnEnergiesTable.put("[$([CX4][S]);!$([CH0]);!$([C][S]=[O]);!$([C][S][P]=[S])]", 57.7);
		SMARTSnEnergiesTable.put("[$([CX4][#6^2]~[#8]),$([CX4][cr5]),$([CX4]([c])[c]),$([CX4]([c])[C]=[C]),$([CX4][#6^1]),$([CX4][C^2]=[C^2]-[#6^2]),$([CX4][NX3][N]=[O]),$([CX4]1[N][C][C][N]([*^2])[C]1);!$([CH0]);!$([CX4][C](=[O])[NX3]);!$([CX4][#6^2](=[#8])-[#8]);!$([CX4][C^2]([C^2])=[C^2]-[#6^2]);!$([CX4][#6^2](=[#8])[#6^2])]", 59.9); //modified in 2.3
		SMARTSnEnergiesTable.put("[$([CX4][O]);!$([CH0]);!$([C][O][C]=[O]);!$([CX4]1[O][C]1);!$([C][O][P]=[S])]", 62.2);
		SMARTSnEnergiesTable.put("[$([CX4][NX3H1][C]=[O]),$([CX4][#7](~[*^2])~[*^2]);!$([CH0]);!$([CX4][NX3H0][C]=[O])]", 63.9);
		SMARTSnEnergiesTable.put("[$([CX4][S][P]),$([CX4]1[N]([C][C]2)[C][C][C]2[C]1);!$([CH0]);!$([C][S]=[O])]", 66.9); //modified in 2.3
		SMARTSnEnergiesTable.put("[$([CX4][#6^2]);!$([CH0]);!$([CX4][C](=[O])[NX3])]", 66.4);
		SMARTSnEnergiesTable.put("[$([CX4][S](=[O])=[O]),$([CX4][NX3][#16X4](=[OX1])(=[OX1])),$([CX4][#6^2](=[#8])[#6^2]);!$([CH0]);!$([CX4][S](=[OX1])(=[OX1])[c])]", 72.3);
		SMARTSnEnergiesTable.put("[CX4;CH1,CH2,$([CH3][NX3,C^2]),$([CH3][O][C]=[O]),$([CH3][S]=[O])]", 75.9);
		SMARTSnEnergiesTable.put("[CX4H3]", 89.6);
		SMARTSnEnergiesTable.put("[$([CX3H2]);$([C]=[*^2]-[*^2])]", 40.1);
		SMARTSnEnergiesTable.put("[$([CX3H1]);$([C]=[*^2]-[*^2]);!$([C](-[*^2,a])=[*^2]-[*^2])]", 52.4);
		SMARTSnEnergiesTable.put("[$([ch1r5]:[#8]),$([ch1r5](:[c]):[nH1])]", 52.9);
		SMARTSnEnergiesTable.put("[$([ch1r5](:[nX3]):[nX2])]", 57.9);
		SMARTSnEnergiesTable.put("[$([ch1]1:[c](-[N^3]([C^3])[C^3]):[c]:[c]:[c]:[c]1),$([ch1]1:[c]:[c]:[c](-[N^3]([C^3])[C^3]):[c]:[c]1),$([ch1]1:[c](-[N^3H1][C^3]):[c]:[c]:[c]:[c]1),$([ch1]1:[c]:[c]:[c](-[N^3H1][C^3]):[c]:[c]1),$([ch1]1:[c](-[N^3H2]):[c]:[c]:[c]:[c]1),$([ch1]1:[c]:[c]:[c](-[N^3H2]):[c]:[c]1)]", 59.5);
		SMARTSnEnergiesTable.put("[$([CX3]);$([CX3]=[CX3]);!$([CH0]);!$([CX3](-[*^2])=[CX3]);!$([CX3]=[CX3]-[*^2])]", 65.6);
		SMARTSnEnergiesTable.put("[$([ch1]1:[c]:[c]:[cr6]([#7]~[#6^2]):[c]:[c]1),$([ch1r6]:[cr5]:[nX3r5]);!$([c]1:[c]:[c]:[c](-[N]-[C]=[O]):[c]:[c]1)]", 68.2);
		SMARTSnEnergiesTable.put("[$([ch1r5]:[#16X2]:[c]),$([ch1r5]:[c]:[nX3])]", 69.4);
		SMARTSnEnergiesTable.put("[$([ch1]);!$([c]1:[c]:[c]:[c](-[O]-[C^2]~[O]):[c]:[c]1);$([c]1:[c]:[c]:[c](-[NH]-[C]=[O]):[c]:[c]1),$([c]1:[c]:[c]:[c](-[O,SX2]):[c]:[c]1),$([ch1r6]:[cr6]:[cr5]:[nX3r5]),$([ch1r6]:[cr5]:[cr5]:[nX3r5,oX2r5,sX2r5])]", 74.1);
		SMARTSnEnergiesTable.put("[$([ch1]);$([ch1]1:[c](~[#7X2]~[#6^2]):[c]:[c]:[c]:[c]1),$([ch1r6]:[cr6]-[O,SX2]),$([ch1r6]:[cr5]:[sX2r5]),$([ch1r6]:[cr6]:[cr5]:[or5,sX2r5]),$([ch1r6]:[cr6]:[cr6]:[cr5]:[nX3r5]);!$([ch1r6]:[cr6]-[O]-[C^2]~[O])]", 77.2);
		SMARTSnEnergiesTable.put("[$([ch1r5]);!$([ch1r5]:[nX2]:[#16X2])]", 78.1);
		SMARTSnEnergiesTable.put("[$([ch1]1:[c]([#6^2,#6^1,O]):[c]:[c]:[c]:[c]1),$([ch1]1:[c]:[c]:[c]([#6^2,#6^1,O]):[c]:[c]1),$([ch1]1:[c]([NX3](~[O])~[O]):[c]:[c]:[c]:[c]1),$([ch1]1:[c]:[c]:[c]([NX3](~[O])~[O]):[c]:[c]1),$([C^2h1](-[C^2])=[C^2]),$([ch1r6]:[cr5]:[or5]),$([ch1r6]:[cr6]:[cr6]:[cr5]:[or5,sX2r5])]", 80.8);
		SMARTSnEnergiesTable.put("[$([ch1]1:[c]([F,Cl,I,Br]):[c]:[c]:[c]:[c]1),$([ch1]1:[c]:[c]:[c]([F,Cl,I,Br]):[c]:[c]1),$([ch1]1:[c]:[c]([NX3](~[O])~[O]):[c]:[c]:[c]1),$([ch1]1:[c]:[c]:[c]([S]=O):[c]:[c]1)]", 84.1);
		SMARTSnEnergiesTable.put("[$([#6^2h1]);!$([ch1r6]:[nr6]);!$([ch1]:[c]-[S](=O)(=O)-[NX3])]", 86.3);
		SMARTSnEnergiesTable.put("[$([ch1r6]:[nr6]),$([ch1]:[c]-[S](=O)(=O)-[NX3])]", 92.0);
		SMARTSnEnergiesTable.put("[$([N^3H0]);!$([N^3][*^2]);!$([NX3][#16X4](=[OX1])(=[OX1]));!$([N]1([CX4])[C][C][N]([*^3])[C][C]1);!$([N]1([CX4])[C][C][NH1][C][C]1);!$([NR1]1([CX4])[C][C][C][C][C]1);!$([NR2]1([CX4])[C]2[C][C][C][C]1[C][C]2);!$([NR2r6](@[C])(@[C])@[C])]", 42.6); //modified in 2.3
		SMARTSnEnergiesTable.put("[$([N^3H0]);$([N]1([CX4])[C][C][C,N][C][C]1);!$([NR2r6](@[C])(@[C])@[C])]", 50.3); // new in 2.3
		SMARTSnEnergiesTable.put("[$([N^3]);$([H1,H2]);!$([NX3][#16X4](=[OX1])(=[OX1]));!$([N^3H1]([*^2])[*^2])]", 54.1); 
		SMARTSnEnergiesTable.put("[$([NX3H0]([#6^2]1)[#6^2]=[#6^2][#6^3][#6^2]=1)]", 61.9);
		SMARTSnEnergiesTable.put("[$([N]([C^3])=[C^2])]", 61.9);
		SMARTSnEnergiesTable.put("[$([N^3H0]);$([N^3][*^2]);!$([N^3]([*^2])[*^2]);!$([NX3][#16X4](=[OX1])(=[OX1]))]", 63.9);
		SMARTSnEnergiesTable.put("[$([N^3H0]);$([NR2r6](@[C])(@[C])@[C])]", 68.4); //new in 2.3
		SMARTSnEnergiesTable.put("[$([NX3H1]([*^2])[*^2]);!$([nr5H0]);!$([NX3H1][C]=[O])]", 72.0);
		SMARTSnEnergiesTable.put("[$([nr6]),$([N](-[#6^2])=[#6^2]);!$([nr5H0])]", 75.6);
		SMARTSnEnergiesTable.put("[$([N]);$([NX3H0]([*^2])[*^2]),$([N^2][C]=[O])]", 89.6);
		SMARTSnEnergiesTable.put("[nr5H0]", 92.1);
		SMARTSnEnergiesTable.put("[$([NX3]);$([NX3][#16X4](=[OX1])(=[OX1]))]", 94.4);
		SMARTSnEnergiesTable.put("[$([NX3H1]([#6^2]1)[#6^2]=[#6^2][#6^3][#6^2]=1)]", 10.4);
	}




	public HashMap<String, Double> getSMARTSnEnergiesTable(){
		return SMARTSnEnergiesTable;
	}

}

