package com.xl.datatypes.sequence;

/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.util.Arrays;

import com.xl.exception.REDException;

/**
 * SplitLocation can be used to represent complex genomic positions built up
 * from several sublocations.
 */
public class SplitLocation{

	private Location[] subLocations = null;

	/**
	 * Instantiates a new split location.
	 * 
	 * @param subLocations
	 *            The set of sublocations from which the whole feature will be
	 *            built
	 * @param strand
	 *            Which strand the feature is on
	 * @throws REDException
	 */
	public SplitLocation(Location[] subLocations) throws REDException {
		
		if (subLocations == null || subLocations.length == 0) {
			throw new IllegalArgumentException(
					"There must be at least one sublocation to define a feature");
		}
		this.subLocations = subLocations;
		Arrays.sort(this.subLocations);
	}


}
