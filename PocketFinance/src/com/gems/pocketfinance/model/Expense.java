package com.gems.pocketfinance.model;

import android.text.format.Time;

public class Expense {
	
	private long timestamp;
	
	private String category;
	
	private float amount;
	
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Expense()
	{
		Time time = new Time();
		time.setToNow();
		this.timestamp = time.toMillis(false);
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

}
