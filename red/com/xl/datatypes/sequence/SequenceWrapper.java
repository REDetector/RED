package com.xl.datatypes.sequence;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * A wrapper class that provides caching for on-disk, queried, and web-service
 * Sequence implementations.
 * 
 * @author jrobinso
 */
public class SequenceWrapper implements Sequence {

	private static boolean cacheSequences = true;
	private static int tileSize = 1000000;

	private Sequence sequence;
	private LinkedHashMap<String, SequenceTile> sequenceCache = new LinkedHashMap<String, SequenceTile>(
			50);

	public SequenceWrapper(Sequence sequence) {
		this.sequence = sequence;
	}

	public byte getBase(String chr, int position) {
		if (cacheSequences) {
			int tileNo = position / tileSize;

			// Get first chunk
			SequenceTile tile = getSequenceTile(chr, tileNo);
			int offset = position - tile.getStart();
			byte[] bytes = tile.bytes;
			if (offset > 0 && offset < bytes.length) {
				return bytes[offset];
			} else {
				return 0;
			}

		} else {
			// TODO -- implement or disable
			return sequence.getBase(chr, position);
		}
	}

	@Override
	public List<String> getChromosomeNames() {
		return sequence.getChromosomeNames();
	}

	@Override
	public int getChromosomeLength(String chrname) {
		return sequence.getChromosomeLength(chrname);
	}

	/**
	 * Return the reference dna sequence for the exact interval specified.
	 * 
	 * @param chr
	 * @param start
	 * @param end
	 * @return
	 */
	public byte[] getSequence(String chr, int start, int end) {
		if (cacheSequences) {
			byte[] seqbytes = new byte[end - start];
			int startTile = start / tileSize;
			int endTile = end / tileSize;

			// Get first chunk
			SequenceTile tile = getSequenceTile(chr, startTile);
			if (tile == null) {
				return null;
			}

			byte[] tileBytes = tile.getBytes();
			if (tileBytes == null) {
				return null;
			}

			int fromOffset = start - tile.getStart();
			int toOffset = 0;

			// A negative offset means the requested start is < the the first
			// tile start. This situation can arise at the
			// left end of chromosomes. In this case we want to copy the first
			// tile to some offset location in the
			// destination sequence array.
			if (fromOffset < 0) {
				toOffset = -fromOffset;
				fromOffset = 0;
			}

			// # of bytes to copy. Note that only one of fromOffset or toOffset
			// is non-zero.
			int nBytes = Math.min(tileBytes.length - Math.abs(fromOffset),
					seqbytes.length - Math.abs(toOffset));

			// Copy first chunk
			System.arraycopy(tileBytes, fromOffset, seqbytes, toOffset, nBytes);

			// If multiple chunks ...
			for (int t = startTile + 1; t <= endTile; t++) {
				tile = getSequenceTile(chr, t);
				if (tile == null) {
					break;
				}

				int nNext = Math.min(seqbytes.length - nBytes, tile.getSize());

				System.arraycopy(tile.getBytes(), 0, seqbytes, nBytes, nNext);
				nBytes += nNext;
			}

			return seqbytes;
		} else {
			return sequence.getSequence(chr, start, end);
		}
	}

	private SequenceTile getSequenceTile(String chr, int tileNo) {
		String key = getKey(chr, tileNo);
		SequenceTile tile = sequenceCache.get(key);

		if (tile == null) {
			int start = tileNo * tileSize;
			int end = start + tileSize; // <= UCSC coordinate conventions (end
										// base not inclusive)

			if (end <= start) {
				return null;
			}

			byte[] seq = sequence.getSequence(chr, start, end);
			tile = new SequenceTile(start, seq);
			sequenceCache.put(key, tile);
		}

		return tile;
	}

	/**
	 * Generate unique key to be used to store/retrieve tiles. We combined the
	 * chr and tileNo, with a delimiter in between to ensure that chr1 12
	 * doesn't clash with chr11 2
	 * 
	 * @param chr
	 * @param tileNo
	 * @return
	 */
	static String getKey(String chr, int tileNo) {
		return chr + "/" + tileNo;
	}

	/**
	 * This accessor provided to support unit tests.
	 * 
	 * @param aChunkSize
	 */
	static void setTileSize(int aChunkSize) {
		tileSize = aChunkSize;
	}

	/**
	 * Accessor to support unit tests.
	 * 
	 * @param aCacheSequences
	 */
	static void setCacheSequences(boolean aCacheSequences) {
		cacheSequences = aCacheSequences;
	}

	public void clearCache() {
		sequenceCache.clear();
	}

	static class SequenceTile {

		private int start;
		private byte[] bytes;

		SequenceTile(int start, byte[] bytes) {
			this.start = start;
			this.bytes = bytes;
		}

		public int getStart() {
			return start;
		}

		public int getSize() {
			return bytes == null ? 0 : bytes.length;
		}

		public byte[] getBytes() {
			return bytes;
		}
	}

}