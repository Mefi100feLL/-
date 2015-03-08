package com.PopCorp.Purchases.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.PopCorp.Purchases.DataBase.DB;

public class ShopesLoader extends CursorLoader{

	private DB db;
	private String cityId;

	public ShopesLoader(Context context, DB db, String cityId) {
		super(context);
		this.db = db;
		this.cityId = cityId;
	}

	@Override
	public Cursor loadInBackground() {
		return db.getdata(DB.TABLE_SHOPES, DB.COLUMNS_SHOPES, DB.KEY_SHOP_CITY_ID + "=" + cityId, null, null, null, null);
	}
}
