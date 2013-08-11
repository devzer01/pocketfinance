package com.gems.pocketfinance.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.gems.pocketfinance.model.Expense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;

public class ExpenseDataSource implements Parcelable {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_TIMESTAMP, MySQLiteHelper.COLUMN_CATEGORY, 
			MySQLiteHelper.COLUMN_AMOUNT};
	
	private String[] aggr = { "SUM(" + MySQLiteHelper.COLUMN_AMOUNT + ")", "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ")" };

	public ExpenseDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public Expense saveExpense(Expense expense) throws SQLException
	{
		ContentValues contentValues = new ContentValues(4);
		
		contentValues.put(MySQLiteHelper.COLUMN_AMOUNT, expense.getAmount());
		contentValues.put(MySQLiteHelper.COLUMN_CATEGORY, expense.getCategory());
		contentValues.put(MySQLiteHelper.COLUMN_TIMESTAMP, expense.getTimestamp() / 1000);
		
		Time createdTime = new Time();
		createdTime.set(expense.getTimestamp());
		contentValues.put(MySQLiteHelper.COLUMN_TIMESTAMP_TEXT, createdTime.format("%Y-%m-%d %H:%M:%S"));
		
		long lastId = database.insertOrThrow(MySQLiteHelper.TABLE_EXPENSE, null, contentValues);
				
		expense.setId(lastId);
		
		return expense;
	}
	
	public Cursor getCategories()
	{
		String[] cols = { "*" };
		return database.query(MySQLiteHelper.TABLE_CATEGORY, cols, null, null, null, null, null);
	}
	
	public void rollback()
	{
		
	}

	public float getTodaysExpenses()
	{
		Time todaysTime = new Time();
		todaysTime.setToNow();
		String[] params = { todaysTime.format("%Y-%m-%d") };
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EXPENSE,
				aggr, "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ") = ?", params, "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ")", null, "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ") ASC");
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			float amount = cursor.getFloat(0);
			return amount;
		}
		return 0;
	}
	
	public float getDailyAvgExpenses()
	{
		Time todaysTime = new Time();
		todaysTime.setToNow();
		String[] params = { todaysTime.format("%Y-%m") };
		
		String[] aggr2 = { "SUM(" + MySQLiteHelper.COLUMN_AMOUNT + ")", "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ")" };
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EXPENSE,
				aggr2, "strftime('%Y-%m', " + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ") = ?", params, "strftime('%Y-%m-%d'," + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ")", null, "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ") ASC");
		cursor.moveToFirst();
		int days = 0;
		float amount = 0;
		while (!cursor.isAfterLast()) {
			amount += cursor.getFloat(0);
			days++;
			cursor.moveToNext();
		}
		return (amount / days);
	}
	
	public float getMonthExpenses()
	{
		Time todaysTime = new Time();
		todaysTime.setToNow();
		String[] params = { todaysTime.format("%Y-%m") };
		
		String[] aggr2 = { "SUM(" + MySQLiteHelper.COLUMN_AMOUNT + ")", "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ")" };
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EXPENSE,
				aggr2, "strftime('%Y-%m', " + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ") = ?", params, "strftime('%Y-%m'," + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ")", null, "date(" + MySQLiteHelper.COLUMN_TIMESTAMP_TEXT + ") ASC");
		cursor.moveToFirst();
		float amount = 0;
		if (!cursor.isAfterLast()) {
			amount += cursor.getFloat(0);
		}
		return amount;
	}
	
	public List<Integer> getLastFiveDaySum()
	{
		List<Integer> counts = new ArrayList<Integer>();
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_EXPENSE,
				aggr, null, null, "date(" + MySQLiteHelper.COLUMN_TIMESTAMP + ")", null, "date(" + MySQLiteHelper.COLUMN_TIMESTAMP + ") ASC");

		int i=0;
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Integer cnt = cursor.getInt(0);
			counts.add(i, cnt);			
			Log.d("Count", cnt.toString() + "   " + cursor.getString(1));
			cursor.moveToNext();
			if (i==5) break;
			i++;
		}
		// Make sure to close the cursor
		cursor.close();

		
		return counts;
	}
	
	public long addCategory(String category)
	{
		ContentValues contentValues = new ContentValues(1);
		contentValues.put("category", category);
		
		return database.insert(MySQLiteHelper.TABLE_CATEGORY, null, contentValues);
	}
	

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}