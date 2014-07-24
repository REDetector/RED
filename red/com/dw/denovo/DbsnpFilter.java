package com.dw.denovo;

/**
 * we will filter out base which already be recognized
 */

import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DbsnpFilter {
    private DatabaseManager databaseManager;
    private String line = null;

    private int count = 0;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DbsnpFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean establishDbSNPResultTable(String dbSnpResultTable) {
        System.out.println("establishDbSNPResultTable start" + " " + df.format(new Date()));

        databaseManager.deleteTable(dbSnpResultTable);
        databaseManager.createTable(dbSnpResultTable, "(chrome varchar(15),"
                + Utilities.getInstance().getS2() + "," + "index(chrome,pos))");

        System.out.println("establishDbSNPResultTable end" + " " + df.format(new Date()));
        return true;

    }

    public boolean hasEstablishDbSNPTable(String dbSnpTable) {
        databaseManager.createRefTable(dbSnpTable,
                "(chrome varchar(15),pos int,index(chrome,pos))");
        ResultSet rs = databaseManager.query(dbSnpTable, "count(*)",
                "1 limit 0,100");
        int number = 0;
        try {
            if (rs.next()) {
                number = rs.getInt(1);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return number > 0;
    }

    public void loadDbSNPTable(String dbSNPTable, String dbSNPPath) {
        try {
            System.out.println("loaddbsnp start" + " " + df.format(new Date()));

            if (!hasEstablishDbSNPTable(dbSNPTable)) {
                FileInputStream inputStream = new FileInputStream(dbSNPPath);
                BufferedReader rin = new BufferedReader(new InputStreamReader(
                        inputStream));
                while ((line = rin.readLine()) != null) {
                    if (line.startsWith("#")) {
                        count++;
                    } else {
                        break;
                    }
                }
                rin.close();
                databaseManager.executeSQL("load data local infile '" + dbSNPPath + "' into table " + dbSNPTable + "" +
                        " fields terminated by '\t' lines terminated by '\n' IGNORE " + count + " LINES");
            }

            System.out.println("loaddbsnp end" + " " + df.format(new Date()));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error load file from " + dbSNPPath + " to file stream");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in " + DbsnpFilter.class.getName() + ":loadComprehensiveTable()");
            e.printStackTrace();
        }
    }

    public void executeDbSNPFilter(String dbSnpTable, String dbSnpResultTable, String refTable) {
        System.out.println("dbsnpf start" + " " + df.format(new Date()));
        try {
            databaseManager.executeSQL("insert into " + dbSnpResultTable
                    + " select * from " + refTable
                    + " where not exists (select chrome from " + dbSnpTable
                    + " where (" + dbSnpTable + ".chrome=" + refTable + ".chrome and " + dbSnpTable + ".pos=" + refTable + ".pos))");
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in" + DbsnpFilter.class.getName() + ":executeDbSNPFilter()");
            e.printStackTrace();
        }

        System.out.println("dbsnpf end" + " " + df.format(new Date()));
    }

    // public void snpF(){
    // System.out.println("snpf start"+" "+df.format(new Date()));// new
    // try {
    //
    // System.out.println("snpf end"+" "+df.format(new Date()));// new
    // } catch (SQLException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // }
    // public void post() throws SQLException {
    // sql[0]="insert into snptemp select *from Comphrehensivetemp where not exists (select *FROM dbSnpVcf where (Comphrehensivetemp.chrome=dbSnpVcf.chrome and Comphrehensivetemp.pos=dbSnpVcf.pos))";
    // db.result = db.stmt.executeUpdate(sql[0]);
    // db.con.commit();
    // while (rs.next()) {
    // coordinate.add(rs.getString(1));
    // coordinate.add(rs.getString(2));
    // }
    // for (int i = 0, len = coordinate.size(); i < len; i++) {
    // switch (i % 2) {
    // case 0:
    // chr = coordinate.get(i);
    // break;
    // case 1:
    // ps = coordinate.get(i);
    // rs=databaseManager.query(referencedbSnp, "chrome", "(pos="+ ps+
    // " and chrome='"+ chr + "')");
    // if(!rs.next())
    // {
    // databaseManager.executeSQL("insert into "+dbSnpTable+"  select * from "+comphrehensiveTable+" where chrome='"+chr+"' and pos="+ps+"");
    // count++;
    // if(count%10000==0)
    // databaseManager.commit();
    // }
    // break;
    // }
    // }
    // databaseManager.commit();
    // databaseManager.setAutoCommit(true);
    // }
}
