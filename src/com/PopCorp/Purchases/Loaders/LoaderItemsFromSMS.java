package com.PopCorp.Purchases.Loaders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.afollestad.materialdialogs.MaterialDialog;

public class LoaderItemsFromSMS extends AsyncTask<Void, Void, Boolean>{
	
	public interface CallbackForLoadingSMS{
		public void onSMSLoaded(ArrayList<HashMap<String, String>> loadedSms);
		public void showToast(int text);
	}
	
	private MaterialDialog prdialog;
	private ArrayList<HashMap<String, String>> mapsSMS;
	private Context context;
	private CallbackForLoadingSMS callback;
	
	public LoaderItemsFromSMS(Context context, CallbackForLoadingSMS callback){
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		mapsSMS = new ArrayList<HashMap<String, String>>();
		final Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		if (cursor!=null){
			if (cursor.moveToFirst()) {
				publishProgress();
				addSmsMap(cursor);
				while (cursor.moveToNext()) {
					addSmsMap(cursor);
				}
				cursor.close();
				return true;
			}
			cursor.close();
		}
		return false;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		prdialog = new MaterialDialog.Builder(context)
        .content(R.string.dialog_reading_sms)
        .progress(true, 0)
        .show();
		prdialog.setCancelable(false);
		prdialog.setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		try{
			prdialog.cancel();
		}catch(Exception e){}
		if (result){
			callback.onSMSLoaded(mapsSMS);
		} else{
			callback.showToast(R.string.toast_no_sms);
		}
	}

	private void addSmsMap(final Cursor cursor) {
		HashMap<String, String> smsMap = new HashMap<String, String>();
		smsMap.put(SD.SMS_KEY_ADDRESS, cursor.getString(cursor.getColumnIndex(SD.SMS_KEY_ADDRESS)));
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(Long.valueOf(cursor.getString(cursor.getColumnIndex(SD.SMS_KEY_DATE))));
		smsMap.put(SD.SMS_KEY_DATE, new SimpleDateFormat("dd.MM.yy HH:mm").format(date.getTime()));
		smsMap.put(SD.SMS_KEY_BODY, cursor.getString(cursor.getColumnIndex(SD.SMS_KEY_BODY)));
		mapsSMS.add(smsMap);
	}
	
	public String getSelectedSMS(ArrayList<HashMap<String, String>> mapsSMS) {
		String sms = "";
		for (int i=0; i<mapsSMS.size(); i++) {
			if (mapsSMS.get(i).get(SD.SMS_KEY_CHECKED)!=null) {
				if (mapsSMS.get(i).get(SD.SMS_KEY_CHECKED).equals("true")) {
					if (mapsSMS.get(i).get(SD.SMS_KEY_CHECKED).contains(" по ")) {
						sms += mapsSMS.get(i).get(SD.SMS_KEY_BODY);
					}
				}
			}
		}
		return sms;
	}
	
	public String getContactDisplayNameByNumber(String number) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] { BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				return contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}
		return number;
	}
}
