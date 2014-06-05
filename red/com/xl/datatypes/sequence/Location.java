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

import java.io.Serializable;

/**
 * A simple class to represent a simple location in the genome. For complex
 * positions containing sublocations you should use the SplitLocation class
 * instead.
 */
public interface Location extends Serializable, Comparable<Location> {

	public String getChr();
	/**
	 * The start position of this location. Guaranteed to not be higher than the
	 * end position
	 * 
	 * @return The start position
	 */
	public int getStart();

	/**
	 * The end position of this location. Guaranteed to be the same or higher
	 * than the start position.
	 * 
	 * @return The end position
	 */
	public int getEnd();

	/**
	 * The sequence length.
	 * @return
	 */
	public int length();
}
