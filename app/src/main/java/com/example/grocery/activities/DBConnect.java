package com.example.grocery.activities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBConnect extends SQLiteOpenHelper {
    public static String TABLE_NAME = "ITEMS_TABLE";
    public static String DATABASE = "ITEMS_DB";
    public static String COL_1 = "Item_Id";
    public static String COL_2 = "Item_PID";
    public static String COL_3 = "Item_Name";
    public static String COL_4 = "Item_Price_Each";
    public static String COL_5 = "Item_Price";
    public static String COL_6 = "Item_Quantity";
    private Context context;

    public DBConnect(@Nullable Context context){
        super(context,DATABASE,null,1);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+TABLE_NAME+
                "("+COL_1+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +COL_2+" TEXT,"
        +COL_3+" TEXT,"
        +COL_4+" TEXT,"
        +COL_5+" TEXT,"
        +COL_6 +" TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
    public boolean adddata(String s1, String s2,String s3,String s4,String s5){
//        close();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues c1 = new ContentValues();

        c1.put(COL_2,s1);
        c1.put(COL_3,s2);
        c1.put(COL_4,s3);
        c1.put(COL_5,s4);
        c1.put(COL_6,s5);
        long res = db.insert(TABLE_NAME,null,c1);
        if(res==-1){
            return false;
        }
        else{
            return true;
        }
    }
    public Cursor selectData(){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME,null,null,null,null,null,null);
    }

}
