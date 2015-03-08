package com.PopCorp.Purchases.Data;

import com.PopCorp.Purchases.DataBase.DB;

import android.database.Cursor;

public class Shop {
	
	private String name;
	private String id;
	
	
	public Shop(String id, String name){
		setId(id);
		setName(name);
	}

	public Shop(Cursor cursor) {
		this(cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_ID)), cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_NAME)));
	}
	
	
	public void putInDB(DB db, String cityId){
		db.addRec(DB.TABLE_SHOPES, DB.COLUMNS_SHOPES_WITH_CITY_ID, new String[] {name, id, cityId});
	}
	
	@Override
	public boolean equals(Object object){
		try{
			Shop shop = (Shop) object;
			if (shop.getId().equals(getId())){
				if (shop.getName().equals(getName())){
					return true;
				}
			}
			return false;
		} catch(Exception e){
			return false;
		}
	}
	
	
	
	
	
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
