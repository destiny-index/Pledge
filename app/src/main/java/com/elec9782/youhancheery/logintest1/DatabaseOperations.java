package com.elec9782.youhancheery.logintest1;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by youhancheery on 2/05/2016.
 */
public class DatabaseOperations extends SQLiteOpenHelper {

    public static final int database_version = 1; //initialise
    //define a table with the column names and type
    public String CREATE_QUERY = "CREATE TABLE " + TableData.TableInfo.TABLE_NAME +
                                "(" + TableData.TableInfo.USER_NAME + " TEXT," +
                                TableData.TableInfo.USER_PASS + " TEXT," +
                                TableData.TableInfo.USER_EMAIL + " TEXT);";

    public DatabaseOperations(Context context) {
        super(context, TableData.TableInfo.DATABASE_NAME, null, database_version);
        Log.d("Database operations", "Database created");
    }


    @Override
    public void onCreate(SQLiteDatabase sdb) {
        sdb.execSQL(CREATE_QUERY); //create the table
        Log.d("Database operations", "Table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        //not sure what this does, but needed for extension
    }

    //method to insert information into the database
    public void putInformation(DatabaseOperations dop, String name, String pass, String email) {
        SQLiteDatabase SQ = dop.getWritableDatabase(); //to write to database
        ContentValues CV = new ContentValues();//object of content values
        //value of each column into object ContentValues
        CV.put(TableData.TableInfo.USER_NAME, name);
        CV.put(TableData.TableInfo.USER_PASS, pass);
        CV.put(TableData.TableInfo.USER_EMAIL, email);
        long k = SQ.insert(TableData.TableInfo.TABLE_NAME, null, CV); //this will put the data into the table
                                                                      //returns a long if successful
        Log.d("Database Operations", "One row inserted");
    }

    //method to retrieve data from SQLite database
    public Cursor getInformation (DatabaseOperations dop) {
        SQLiteDatabase SQ = dop.getReadableDatabase(); //to read from database
        String[] columns  = {TableData.TableInfo.USER_NAME, TableData.TableInfo.USER_PASS, TableData.TableInfo.USER_EMAIL};
        Cursor CR = SQ.query(TableData.TableInfo.TABLE_NAME, columns, null, null,
                             null, null, null);
        return CR;
    }
}
