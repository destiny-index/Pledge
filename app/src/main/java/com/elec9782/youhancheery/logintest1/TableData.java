package com.elec9782.youhancheery.logintest1;

import android.provider.BaseColumns;

/**
 * Created by youhancheery on 2/05/2016.
 */

public class TableData {
    //default constructor to avoid accidental object declaration of this class
    public TableData() {

    }
    //inner class
    public static abstract class TableInfo implements BaseColumns {
        //column names
        public static final String USER_NAME = "user_name"; //why static?
        public static final String USER_PASS = "user_pass";
        public static final String USER_EMAIL = "user_email";
        public static final String DATABASE_NAME = "user_info";
        public static final String TABLE_NAME = "reg_info";
    }
}
