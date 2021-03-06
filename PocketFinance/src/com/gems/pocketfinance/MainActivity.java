package com.gems.pocketfinance;


import java.text.NumberFormat;
import java.util.Calendar;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.gems.pocketfinance.db.ExpenseDataSource;
import com.gems.pocketfinance.model.Expense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TextView.OnEditorActionListener;
import android.database.Cursor;
import android.database.SQLException;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;

public class MainActivity extends SherlockFragmentActivity implements Parcelable {

	protected ExpenseDataSource expenseDs = null;
	
	protected TextView today = null;
	protected TextView dailyAvg = null;
	protected TextView monthly = null;
	
	protected Button buttonUndo = null;
	
	protected EditText date = null;
	protected EditText time = null;
	
	protected Spinner spinner = null;
	
	protected SimpleCursorAdapter adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (expenseDs == null) expenseDs = new ExpenseDataSource(this);
		expenseDs.open();
		
		Cursor cursor = expenseDs.getCategories();
		
		spinner = (Spinner) findViewById(R.id.s_category);
		
		String[] from =  { "category" };
		int[] to = { android.R.id.text1 };
		
	
		
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				Cursor cursor = (Cursor)arg0.getSelectedItem();
				
				if (cursor.getString(cursor.getColumnIndex("category")).contains("New..")) {
					NewCategoryDialog newCategoryDialog = new NewCategoryDialog();
					Bundle bundle = new Bundle();
					bundle.putParcelable("ds", expenseDs);
					bundle.putParcelable("main", MainActivity.this);
					newCategoryDialog.setArguments(bundle);
	        		newCategoryDialog.show(getSupportFragmentManager(), "new_category");
	        		
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	
		LinearLayout ll_summary = (LinearLayout)findViewById(R.id.ll_summary);
		
		ll_summary.measure(0, 0);

		final EditText expenseAmount = (EditText)findViewById(R.id.expence_amount);
		
		today = (TextView)findViewById(R.id.tv_daily_amount);
		dailyAvg = (TextView)findViewById(R.id.tv_daily_avg);
		monthly = (TextView)findViewById(R.id.tv_monthly_amount);
		
		LayoutParams layoutParams = today.getLayoutParams();
		layoutParams.width = (int) ((ll_summary.getMeasuredWidth() / 2) * 0.83);
		today.setLayoutParams(layoutParams);
		
		date = (EditText)findViewById(R.id.expence_date);
		date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				 DialogFragment newFragment = new DatePickerFragment();
				 newFragment.show(getSupportFragmentManager(), "datePicker");
			}
			
		});
		
		date.setRawInputType(InputType.TYPE_NULL);
		
		time = (EditText)findViewById(R.id.expence_time);
		time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DialogFragment newFragment = new TimePickerFragment();
				newFragment.show(getSupportFragmentManager(), "timePicker");
			}
			
		});
		
		time.setRawInputType(InputType.TYPE_NULL);
				
		updateDashboard();
		
		buttonUndo = (Button)findViewById(R.id.btn_undo);
		buttonUndo.setVisibility(View.GONE);
		
		Button buttonSave = (Button)findViewById(R.id.btn_save);
		buttonSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Expense expense = new Expense();
				expense.setCategory(spinner.getSelectedItem().toString());
				expense.setAmount(Float.parseFloat(expenseAmount.getText().toString()));
				expenseAmount.setText("");
				try {
					expenseDs.saveExpense(expense);
					MainActivity.this.updateDashboard();
					buttonUndo.setVisibility(View.VISIBLE);
				} catch (SQLException e) {
					Log.d("saveButton", e.getMessage());
				}
			}
		});
		
		buttonUndo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				expenseDs.rollback();
				buttonUndo.setVisibility(View.GONE);
			}
			
		});
		
	}
	
	public void setNewCategoryAndUpdate(int category)
	{
		Cursor cursor = expenseDs.getCategories();
		
		String[] from =  { "category" };
		int[] to = { android.R.id.text1 };
				
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(category);
	}
	
	public void setDate(String date)
	{
		this.date.setText(date);
	}
	
	public void setTime(String time)
	{
		this.time.setText(time);
	}

	
	protected void updateDashboard()
	{
		NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
		float amount = expenseDs.getTodaysExpenses();
		today.setText(numberFormat.format(amount));
		
		amount = expenseDs.getDailyAvgExpenses();
		dailyAvg.setText(numberFormat.format(amount));
		
		amount = expenseDs.getMonthExpenses();
		monthly.setText(numberFormat.format(amount));
		
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	protected void openExpence()
	{
		Intent intent = new Intent(this, InputActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_expence:
	            openExpence();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DATE);
			
			// Create a new instance of TimePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			MainActivity mainActivity = (MainActivity) this.getActivity();
			mainActivity.setDate(year + "-" + monthOfYear + "-" + dayOfMonth);
		}
	}
	
	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR);
			int minute = c.get(Calendar.MINUTE);
			
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute, false);
		}

		@Override
		public void onTimeSet(TimePicker arg0, int hour, int minute) {
			MainActivity mainActivity = (MainActivity) this.getActivity();
			mainActivity.setTime(hour + ":" + minute + ":00");
		}
	}
	
	public static class NewCategoryDialog extends DialogFragment implements OnEditorActionListener {

	    private EditText mEditText;
	    private ExpenseDataSource expenseDs;
	    private MainActivity mainActivity;

	    public NewCategoryDialog()
	    {
	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	    	
	    	Bundle bundle = this.getArguments();
	    	expenseDs = (ExpenseDataSource) bundle.getParcelable("ds");
	    	mainActivity = (MainActivity) bundle.getParcelable("main");
	    	
	        View view = inflater.inflate(R.layout.new_category_dialog, container);
	        mEditText = (EditText) view.findViewById(R.id.txt_category);
	        getDialog().setTitle("Enter Expense Category");
	        
	     // Show soft keyboard automatically
	        mEditText.requestFocus();
	        mEditText.setOnEditorActionListener(this);
	        
	        Button saveCategory = (Button)view.findViewById(R.id.save_category);
	        saveCategory.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String category = mEditText.getText().toString();
					int category_id = (int) expenseDs.addCategory(category);
					mainActivity.setNewCategoryAndUpdate(category_id);
					NewCategoryDialog.this.dismiss();
				}
	        	
	        });
	        
	        return view;
	    }
	    
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	        this.dismiss();
	        return true;
	    }
}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
}
