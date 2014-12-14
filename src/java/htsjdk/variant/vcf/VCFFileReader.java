package htsjdk.variant.vcf;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalList;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.FeatureCodec;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.bcf2.BCF2Codec;
import htsjdk.variant.variantcontext.VariantContext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Simplified interface for reading from VCF/BCF files.
 */
public class VCFFileReader implements Closeable, Iterable<VariantContext> {

	private final FeatureReader<VariantContext> reader;

	/**
	 * Returns true if the given file appears to be a BCF file.
	 */
	public static boolean isBCF(final File file) {
		return file.getAbsolutePath().endsWith(".bcf");
	}

	/**
	 * Returns the SAMSequenceDictionary from the provided VCF file.
	 */
	public static SAMSequenceDictionary getSequenceDictionary(final File file) {
		final SAMSequenceDictionary dict = new VCFFileReader(file, false).getFileHeader().getSequenceDictionary();
		CloserUtil.close(file);
		return dict;
	}

    /** Constructs a VCFFileReader that requires the index to be present. */
	public VCFFileReader(final File file) {
		this(file, true);
	}

    /** Constructs a VCFFileReader with a specified index. */
    public VCFFileReader(final File file, final File indexFile) {
        this(file, indexFile, true);
    }

    /** Allows construction of a VCFFileReader that will or will not assert the presence of an index as desired. */
	public VCFFileReader(final File file, final boolean requireIndex) {
		this.reader = AbstractFeatureReader.getFeatureReader(
						file.getAbsolutePath(),
						isBCF(file) ? (FeatureCodec) new BCF2Codec() : new VCFCodec(),
						requireIndex);
	}

    /** Allows construction of a VCFFileReader with a specified index file. */
    public VCFFileReader(final File file, final File indexFile, final boolean requireIndex) {
        this.reader = AbstractFeatureReader.getFeatureReader(
                file.getAbsolutePath(),
                indexFile.getAbsolutePath(),
                isBCF(file) ? (FeatureCodec) new BCF2Codec() : new VCFCodec(),
                requireIndex);
    }

    /**
     * Parse a VCF file and convert to an IntervalList The name field of the IntervalList is taken from the ID field of the variant, if it exists. if not,
     * creates a name of the format interval-n where n is a running number that increments only on un-named intervals
     * @param file
     * @return
     */
    public static IntervalList fromVcf(final File file){
        final VCFFileReader vcfFileReader = new VCFFileReader(file, false);
        final IntervalList intervalList = fromVcf(vcfFileReader);
        vcfFileReader.close();
        return intervalList;
    }

    /**
     * Converts a vcf to an IntervalList. The name field of the IntervalList is taken from the ID field of the variant, if it exists. if not,
     * creates a name of the format interval-n where n is a running number that increments only on un-named intervals
     * @param vcf the vcfReader to be used for the conversion
     * @return an IntervalList constructed from input vcf
     */
    public static IntervalList fromVcf(final VCFFileReader vcf){

        //grab the dictionary from the VCF and use it in the IntervalList
        final SAMSequenceDictionary dict = vcf.getFileHeader().getSequenceDictionary();
        final SAMFileHeader samFileHeader = new SAMFileHeader();
        samFileHeader.setSequenceDictionary(dict);
        final IntervalList list = new IntervalList( samFileHeader);

        int intervals=0;
        for(final VariantContext vc : vcf){
            if(!vc.isFiltered()){
                String name = vc.getID();
                if(".".equals(name) || name == null)
                    name = "interval-" + (++intervals);
                list.add(new Interval(vc.getChr(), vc.getStart(), vc.getEnd(), false, name));
            }
        }

        return list;
    }

    /** Returns the VCFHeader associated with this VCF/BCF file. */
	public VCFHeader getFileHeader() {
		return (VCFHeader) reader.getHeader();
	}

    /** Returns an iterator over all records in this VCF/BCF file. */
	public CloseableIterator<VariantContext> iterator() {
		try { return reader.iterator(); }
        catch (final IOException ioe) {
			throw new TribbleException("Could not create an iterator from a feature reader.", ioe);
		}
	}

    /** Queries for records within the region specified. */
    public CloseableIterator<VariantContext> query(final String chrom, final int start, final int end) {
        try { return reader.query(chrom, start, end); }
        catch (final IOException ioe) {
            throw new TribbleException("Could not create an iterator from a feature reader.", ioe);
        }
    }

	public void close() {
		try { this.reader.close(); }
        catch (final IOException ioe) {
			throw new TribbleException("Could not close a variant context feature reader.", ioe);
		}
	}
}
