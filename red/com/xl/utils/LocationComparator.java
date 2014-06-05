package com.xl.utils;

import com.xl.datatypes.sequence.Location;

public class LocationComparator{

	public static int compare(Location o1, Location o2) {
		if (o1.getStart() > o2.getStart()) {
			return 1;
		} else if (o1.getEnd() > o2.getEnd()) {
			return -1;
		} else {
			return 0;
		}
	}
}
