package com.PopCorp.Purchases.Data;

import java.math.BigDecimal;

import com.PopCorp.Purchases.DataBase.DB;

import android.database.Cursor;

public class Product {
	
	private long id;
	private String name;
	private BigDecimal count;
	private String edizm;
	private BigDecimal coast;
	private String category;
	private String shop;
	private String comment;
	
	public Product(long id, String name, String count, String edizm, String coast, String category, String shop, String comment){
		this.id = id;
		this.name = name;
		setCount(count);
		this.edizm = edizm;
		setCoast(coast);
		this.category = category;
		this.shop = shop;
		this.comment = comment;
	}
	
	public Product(Cursor cursor){
		this(cursor.getLong(cursor.getColumnIndex(DB.KEY_ID)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_NAME)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_COUNT)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_EDIZM)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_COAST)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_CATEGORY)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_SHOP)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_COMMENT)));
	}
	
	public void remove(DB db){
		db.deleteRows(DB.TABLE_ALL_ITEMS, DB.KEY_ID + "=" + getId());
	}
	
	public void update(DB db, String name, String count, String edizm, String coast, String category, String shop, String comment){
		this.name = name;
		setCount(count);
		this.edizm = edizm;
		setCoast(coast);
		this.category = category;
		this.shop = shop;
		this.comment = comment;
		db.update(DB.TABLE_ALL_ITEMS, DB.COLUMNS_ALL_ITEMS, DB.KEY_ID + "=" + getId(), getFields());
	}
	
	public String[] getFields(){
		return new String[] {
				getName(),
				getCount().toString(),
				getEdizm(),
				getCoast().toString(),
				getCategory(),
				getShop(),
				getComment()
		};
	}
	
	
	
	
	
	
	////////////////////////////////////// SETTERS AND GETTERS /////////////////////////////////////////
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getCount() {
		return count;
	}
	public void setCount(String count) {
		try{
			this.count = new BigDecimal(count);
		} catch(Exception e){
			this.count = new BigDecimal("0");
		}
	}
	public String getEdizm() {
		return edizm;
	}
	public void setEdizm(String edizm) {
		this.edizm = edizm;
	}
	public BigDecimal getCoast() {
		return coast;
	}
	public void setCoast(String coast) {
		try{
			this.coast = new BigDecimal(coast);
		} catch(Exception e){
			this.coast = new BigDecimal("0");
		}
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getShop() {
		return shop;
	}
	public void setShop(String shop) {
		this.shop = shop;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
