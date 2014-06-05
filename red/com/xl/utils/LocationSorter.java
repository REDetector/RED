package com.xl.utils;

import com.xl.datatypes.sequence.SequenceRead;

public class LocationSorter {

	/*
	 * This is an optimised sort to merge together two or more lists of longs
	 * which are individually sorted. Up until now we've been putting these into
	 * a big array and doing a quicksort but hopefully this approach will be
	 * quicker since we can make assumptions about the sorting order of the
	 * reads in each sublist
	 */

	public static SequenceRead[] sortLocationSets(SequenceRead[][] sets) {

		if (sets.length == 0)
			return new SequenceRead[0];
		if (sets.length == 1)
			return sets[0];

		int totalLength = 0;

		for (int i = 0; i < sets.length; i++) {
			totalLength += sets[i].length;
		}

		SequenceRead[] returnArray = new SequenceRead[totalLength];

		int[] currentIndices = new int[sets.length];

		int lowestIndex = 0;

		SequenceRead lowestValue = null;

		// Need to do something when we reach the end of a sublist

		for (int i = 0; i < returnArray.length; i++) {
			// Add the lowest read to the full set
			lowestIndex = -1;
			lowestValue = null;
			for (int j = 0; j < currentIndices.length; j++) {
				if (currentIndices[j] == sets[j].length)
					continue; // Skip datasets we've already emptied
				if (lowestValue == null
						|| SequenceReadUtils.compare(
								sets[j][currentIndices[j]], lowestValue) < 0) {
					lowestIndex = j;
					lowestValue = sets[j][currentIndices[j]];
				}
			}

			returnArray[i] = lowestValue;
			currentIndices[lowestIndex]++;

		}

		return returnArray;

	}

	public static void sortInts(int[] values, HiCLocationComparator comparator) {

		if (values == null || values.length == 0)
			return;

		quicksort(values, 0, values.length - 1, comparator);
	}

	/*
	 * Quicksort implementation adapted from
	 * http://www.inf.fh-flensburg.de/lang/
	 * algorithmen/sortieren/quick/quicken.htm
	 */

	private static void quicksort(int[] a, int lo, int hi,
			HiCLocationComparator comparator) {
		// lo is the lower index, hi is the upper index
		// of the region of array a that is to be sorted
		int i = lo, j = hi;
		int h;

		// comparison element x
		int x = a[(lo + hi) / 2];

		// partition
		do {
			while (comparator.compare(a[i], x) < 0)
				i++;
			while (comparator.compare(a[j], x) > 0)
				j--;
			if (i <= j) {
				h = a[i];
				a[i] = a[j];
				a[j] = h;
				i++;
				j--;
			}
		} while (i <= j);

		// recursion
		if (lo < j)
			quicksort(a, lo, j, comparator);
		if (i < hi)
			quicksort(a, i, hi, comparator);
	}

}
