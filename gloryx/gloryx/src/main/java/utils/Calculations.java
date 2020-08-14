/* Copyright (C) 2020  Christina de Bruyn Kops <christinadebk@gmail.com>
 
    This file is part of GLORYx.

    GLORYx is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    All we ask is that proper credit is given for our work, which includes 
    - but is not limited to - adding the above copyright notice to the beginning 
    of your source code files, and to any copyright notice that you may distribute 
    with programs based on this work.

    GLORYx is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GLORYx.  If not, see <https://www.gnu.org/licenses/>.
*/

package main.java.utils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to do some basic calculations.
 * 
 * @author Christina de Bruyn Kops
 *
 */
public class Calculations {
	
	private static final String AVERAGE_CANNOT_BE_CALCULATED = "Average cannot be calculated for this type. Only integers, longs, and doubles are allowed. Please try again.";
	
	private static final Logger logger = LoggerFactory.getLogger(Calculations.class.getName());

	private Calculations() {
		throw new IllegalStateException("Utility class");
	}
	

	public static Double calculateAverage(List<?> numbers) {

		double sum = 0d;

		if (numbers.isEmpty()) {
			logger.error(AVERAGE_CANNOT_BE_CALCULATED);
			return Double.NaN;
			
		} else if (numbers.get(0) instanceof Double) {
			for (Object s : numbers) {
				sum += (double) s;
			}
		} else if (numbers.get(0) instanceof Integer) {
			for (Object s : numbers) {
				sum += ((Integer) s).doubleValue();
			}
		} else if (numbers.get(0) instanceof Long) {
			for (Object s : numbers) {
				sum += ((Long) s).doubleValue();
			}
		} else {
			logger.error(AVERAGE_CANNOT_BE_CALCULATED);
		}

		return sum / (numbers.size());
	}
	
	
	public static int calculateSum(List<Integer> numbers) {
		
		int sum = 0;
		for (int i : numbers) {
			sum += i;
		}
		return sum;
	}
	
	
	public static Double calculateRecoveryRate(int numKnownMetabolites, int numTruePositivePredictions) {
		
		return (double) numTruePositivePredictions / (double) numKnownMetabolites;
	}


}
