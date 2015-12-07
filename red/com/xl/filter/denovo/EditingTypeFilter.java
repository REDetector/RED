/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.filter.denovo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.xl.database.Query;
import com.xl.datatypes.sites.SiteBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xl.database.DatabaseManager;
import com.xl.filter.Filter;
import com.xl.utils.NegativeType;
import com.xl.utils.Timer;

/**
 * Created by Xing Li on 2014/9/29.
 * <p>
 * The Class EditingTypeFilter is a rule-based filter that user enables to select the type of RNA editing as his
 * preference.
 */
public class EditingTypeFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(EditingTypeFilter.class);
    public static final String PARAMS_REF = "ref";

    /**
     * The database manager.
     */
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Perform editing type filter as user's preference. We just keep the type 'ref'->'alt' and the gene type is
     * homozygous at the same time.
     *
     * @param previousTable The table name of previous filter stored in the database.
     * @param currentTable The table name of this filter stored in the database.
     * @param params The reference base
     */
    @Override
    public void performFilter(String previousTable, String currentTable, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return;
        }

        logger.info("Start executing Editing Type Filter..." + Timer.getCurrentTime());
        String refSeqTable = DatabaseManager.SPLICE_JUNCTION_TABLE_NAME;
        try {
            List<SiteBean> editingTypeSites = new ArrayList<SiteBean>();
            String refAlt = params.get(PARAMS_REF);
            int count = 0;
            if (refAlt.equalsIgnoreCase("all")) {
                Vector<SiteBean> sites = Query.queryAllEditingInfo(previousTable);
                for (SiteBean site : sites) {
                    if (!inPositiveStrand(site, refSeqTable)) {
                        site.setStrand('-');
                    }
                    editingTypeSites.add(site);
                }
                databaseManager.setAutoCommit(false);
                for (SiteBean site : editingTypeSites) {
                    databaseManager.insertSiteBean(currentTable, site);
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            } else {
                char[] refAlts = refAlt.toCharArray();
                char[] refAlts2 = NegativeType.getNegativeStrandEditingType(refAlt).toCharArray();
                Vector<SiteBean> sites =
                    Query.queryAllEditingInfo(previousTable, null, "(REF=? and ALT=?) or (REF=? and ALT=?)",
                        new String[] { refAlts[0] + "", refAlts[1] + "", refAlts2[0] + "", refAlts2[1] + "" });
                for (SiteBean site : sites) {
                    if (inPositiveStrand(site, refSeqTable)) {
                        if (site.getRef() == refAlts[0] && site.getAlt() == refAlts[1]) {
                            editingTypeSites.add(site);
                        }
                    } else {
                        if (site.getRef() == refAlts2[0] && site.getAlt() == refAlts2[1]) {
                            site.setStrand('-');
                            editingTypeSites.add(site);
                        }
                    }
                }
                databaseManager.setAutoCommit(false);
                for (SiteBean site : editingTypeSites) {
                    databaseManager.insertSiteBean(currentTable, site);
                    if (++count % DatabaseManager.COMMIT_COUNTS_PER_ONCE == 0)
                        databaseManager.commit();
                }
                databaseManager.commit();
                databaseManager.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("There is a syntax error for SQL clause", e);
        }
        logger.info("End executing Editing Type Filter..." + Timer.getCurrentTime());
    }

    private boolean inPositiveStrand(SiteBean site, String refSeqTable) {
        ResultSet rs = databaseManager.query("select strand from " + refSeqTable + " where chrom='" + site.getChr()
            + "' and begin<=" + site.getPos() + " and end>=" + site.getPos());
        try {
            if (rs != null && rs.next()) {
                String strand = rs.getString(1);
                if (strand.equals("-")) {
                    return false;
                }
            }
        } catch (SQLException e) {
            return true;
        }
        return true;
    }

    @Override
    public String getName() {
        return DatabaseManager.EDITING_TYPE_FILTER_RESULT_TABLE_NAME;
    }
}
