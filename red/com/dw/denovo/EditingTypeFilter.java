package com.dw.denovo;

import com.dw.dbutils.DatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2014/9/29.
 */
public class EditingTypeFilter {
    private DatabaseManager databaseManager;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public EditingTypeFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void establishSpecificTable(String specificTable) {
        databaseManager.deleteTable(specificTable);
        databaseManager.createFilterTable(specificTable);
    }

    public void executeSpecificFilter(String specificTable, String rnaVcfTable, String ref, String alt) {
        System.out.println("Start executing specific filter..." + df.format(new Date()));

        databaseManager.insertClause("insert into " + specificTable + "  select * from " + rnaVcfTable + " where " +
                "REF='" + ref + "' AND ALT='" + alt + "' AND GT!='0/0'");

        System.out.println("End executing specific filter..." + df.format(new Date()));
    }

}
