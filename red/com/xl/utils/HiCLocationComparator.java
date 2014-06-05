package com.xl.utils;

import com.xl.datatypes.sequence.SequenceRead;

public class HiCLocationComparator {

	private SequenceRead[] sourceReads;
	private SequenceRead[] hitReads;

	public HiCLocationComparator(SequenceRead[] sourceReads, SequenceRead[] hitReads) {
		this.sourceReads = sourceReads;
		this.hitReads = hitReads;
	}

	public int compare(int l1, int l2) {
		if (sourceReads[l1] == sourceReads[l2]) {
			return SequenceReadUtils.compare(hitReads[l1], hitReads[l2]);
		} else {
			return SequenceReadUtils.compare(sourceReads[l1], sourceReads[l2]);
		}
	}
}
