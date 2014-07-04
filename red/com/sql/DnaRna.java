package com.sql;

public class DnaRna {
    BasicFilter bf = new BasicFilter();
    Dbcon db = new Dbcon();
    private String[] sql = new String[3];

    public void drfilter() {
        sql[0] = "select * from sam where (select * from specifictemp where ";
    }
}
