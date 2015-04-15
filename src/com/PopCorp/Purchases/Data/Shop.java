package com.PopCorp.Purchases.Data;

import com.PopCorp.Purchases.DataBase.DB;

import android.database.Cursor;

public class Shop {
	
	private String name;
	private String id;
	private String imageUrl;
	
	
	public Shop(String id, String name, String imageUrl){
		setId(id);
		setName(name);
		setImageUrl(imageUrl);
	}

	public Shop(Cursor cursor) {
		this(cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_ID)), cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_NAME)), cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_IMAGE_URL)));
	}
	
	
	public void putInDB(DB db, String cityId){
		db.addRec(DB.TABLE_SHOPES, DB.COLUMNS_SHOPES_WITH_CITY_ID, new String[] {cityId, id, name, imageUrl});
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

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
