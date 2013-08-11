package com.gems.pocketfinance.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	
	/**
	 * customer table
	 */
	public static final String TABLE_EXPENSE = "expense";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_CATEGORY = "category";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_TIMESTAMP = "time_stamp";
	public static final String COLUMN_TIMESTAMP_TEXT = "time_stamp_text";
	
	
	private static final String DATABASE_NAME = "pocket_finance.db";
	private static final int DATABASE_VERSION = 3;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_EXPENSE + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TIMESTAMP + " integer not null, " 
			+ COLUMN_CATEGORY + " text not null, "
			+ COLUMN_AMOUNT + " real not null, "
			+ COLUMN_TIMESTAMP_TEXT + " text not null);";
	
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
    public synchronized void close() {

        super.close();

    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(DATABASE_CREATE);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);		
		onCreate(db);
	}

}
