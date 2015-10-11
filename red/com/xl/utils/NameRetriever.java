/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.utils;

import com.xl.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xing Li on 2014/11/19.
 * <p/>
 * The Class NameRetriever provides the name retrieve service.
 */
public class NameRetriever {
    private static final Logger logger = LoggerFactory.getLogger(NameRetriever.class);
    private static final Set<String> chrNameSets = new HashSet<String>();

    static {
        for (int i = 1; i <= 22; i++) {
            chrNameSets.add("chr" + i);
        }
        chrNameSets.add("chrX");
        chrNameSets.add("chrY");
        chrNameSets.add("chrM");
    }

    public static String retrieveParams(String currentTable, String[] sections) {
        int length = sections.length;
        if (currentTable.equals(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME)) {
            try {
                double qual = Double.parseDouble(sections[length - 2]);
                int dp = Integer.parseInt(sections[length - 1]);
                return "Q>=" + qual + " & DP>=" + dp;
            } catch (Exception e) {
                return "Quality Control Filter";
            }
        } else if (currentTable.equals(DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME)) {
            String ref = sections[length - 2];
            String alt = sections[length - 1];
            if (ref.length() != 1 || alt.length() != 1) {
                return "Editing Type Filter";
            } else {
                return "Focus on " + sections[length - 2].toUpperCase() + " to " + sections[length - 1].toUpperCase();
            }
        } else if (currentTable.equals(DatabaseManager.FET_FILTER_RESULT_TABLE_NAME)) {
            return "FET Filter";
        } else if (currentTable.equals(DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME)) {
            return "Known SNP Filter";
        } else if (currentTable.equals(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME)) {
            return "LLR Filter";
        } else if (currentTable.equals(DatabaseManager.REPEAT_FILTER_RESULT_TABLE_NAME)) {
            return "Repeat Regions Filter";
        } else if (currentTable.equals(DatabaseManager.SPLICE_JUNCTION_FILTER_RESULT_TABLE_NAME)) {
            try {
                int spliceJunction = Integer.parseInt(sections[length - 1]);
                return "Splice Junction: " + spliceJunction;
            } catch (Exception e) {
                return "Splice Junction Filter";
            }
        } else if (currentTable.equals(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME)) {
            return "DNA-RNA Filter";
        } else {
            return "Unknown Filter";
        }
    }

    public static String getFilterName(String tableName) {
        String[] sections = tableName.split("_");
        if (sections.length == 1) {
            return tableName;
        } else {
            for (int i = sections.length - 1; i >= 0; i--) {
                if (sections[i].contains(DatabaseManager.FILTER) || sections[i].equals(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME)) {
                    return sections[i];
                }
            }
        }
        return null;
    }
    
    /**
     * Get the sample name from a table name.
     * <p/>
     * Here is an example: If the table name is 'BJ22_qcfilter_etfilter', then the sample name is 'BJ22'
     *
     * @param tableName The table name.
     * @return The sample name.
     */
    public static String getSampleName(String tableName) {
        if (tableName == null) {
            return null;
        }
        String[] sections = tableName.split("_");
        if (sections.length == 1) {
            return tableName;
        } else if (sections.length == 2) {
            return tableName.substring(0, tableName.indexOf("_"));
        } else {
            StringBuilder builder = new StringBuilder();
            for (String section : sections) {
                // Because the table name of RNA/DNA VCF file is 'sample_rnavcf/dnavcf', the filter table name is 'sample_previousFilter_currentFilter_params',
                // so if the section meets 'rnavcf/dnavcf' or 'filter', then we stop finding.
                if (section.contains(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME) || section.contains(DatabaseManager.DNA_RNA_FILTER_RESULT_TABLE_NAME) ||
                        section.contains(DatabaseManager.FILTER)) {
                    break;
                } else {
                    builder.append(section).append("_");
                }
            }
            return builder.substring(0, builder.length() - 1);
        }
    }
    
    public static boolean isStandardChromosomeName(String chr) {
        return chrNameSets.contains(chr);
    }
    
    public static String formatChromosomeName(String chr) {
        if (chr.length() == 1 || chr.length() == 2) {
            return "chr" + chr;
        } else if (chr.startsWith("ch") && !chr.startsWith("chr")) {
            return "chr" + chr.substring(2);
        } else {
            return chr;
        }
    }
    
    public static String getAliasChromosomeName(String chr) {
        if (chr.length() == 1) {
            return chr;
        } else {
            return chr.substring(3);
        }
    }
}
