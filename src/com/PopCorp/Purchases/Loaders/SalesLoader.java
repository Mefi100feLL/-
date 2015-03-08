package com.PopCorp.Purchases.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.PopCorp.Purchases.Data.Shop;
import com.PopCorp.Purchases.DataBase.DB;

public class SalesLoader extends CursorLoader{

	private DB db;
	private Shop shop;

	public SalesLoader(Context context, DB db, Shop shop) {
		super(context);
		this.db = db;
		this.shop = shop;
	}

	@Override
	public Cursor loadInBackground() {
		if (shop!=null){
			return db.getdata(DB.TABLE_SALES, DB.COLUMNS_SALES_WITH_ID, DB.KEY_SALES_SHOP + "='" + shop.getId() + "'", null, null, null, null);
		}
		return null;
	}
}
