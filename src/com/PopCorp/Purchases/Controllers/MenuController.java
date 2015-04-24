package com.PopCorp.Purchases.Controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.PopCorp.Purchases.Adapters.MenuAdapter;
import com.PopCorp.Purchases.Comparators.MenuComparator;
import com.PopCorp.Purchases.Data.List;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Fragments.ListFragment;
import com.PopCorp.Purchases.Fragments.MenuFragment;
import com.PopCorp.Purchases.Loaders.LoaderItemsFromSMS;
import com.PopCorp.Purchases.Loaders.LoaderItemsFromSMS.CallbackForLoadingSMS;
import com.PopCorp.Purchases.Loaders.MenuLoader;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class MenuController implements LoaderCallbacks<Cursor>, CallbackForLoadingSMS{

	public static final int TYPE_OF_LOADING_LIST_FROM_SMS = 1;
	public static final int TYPE_OF_LOADING_LIST_FROM_CLIPBOARD = 2;
	public static final int TYPE_OF_LOADING_LIST_FROM_MAIL = 3;

	public static final int ID_FOR_CREATE_LOADER_FROM_DB = 1;
	
	private SharedPreferences sPref;
	private SharedPreferences.Editor editor;

	private AppCompatActivity context;
	private DB db;

	private ArrayList<List> lists;
	private MenuAdapter adapter;

	private LoaderItemsFromSMS loadingSms;
	private ViewGroup layoutForSnackBar;
	private List removedList;
	private StaggeredGridLayoutManager mLayoutManager;
	private MenuFragment fragment;

	public MenuController(AppCompatActivity activity, ViewGroup layoutForSnackBar, StaggeredGridLayoutManager mLayoutManager, MenuFragment fragment){
		this.context = activity;
		this.layoutForSnackBar = layoutForSnackBar;
		this.mLayoutManager = mLayoutManager;
		this.fragment = fragment;
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		editor = sPref.edit();

		db = new DB(activity);
		openDB();
		lists = new ArrayList<List>();
		setAdapter(new MenuAdapter(activity, lists, this));
	}


	public void addNewList(String newName, String currency){
		String datelist = String.valueOf(Calendar.getInstance().getTimeInMillis());
		long id = db.addRec(DB.TABLE_LISTS, DB.COLUMNS_LISTS, new String[] {newName, datelist, "", currency});
		List newList = new List(db, id, newName, datelist, "", currency);
		addListAndOpen(newList);
	}


	private void addListAndOpen(List newList) {
		lists.add(newList);
		Collections.sort(lists, new MenuComparator());
		adapter.notifyItemInserted(lists.indexOf(newList));
		openList(lists.indexOf(newList));
	}
	
	public void addNewListFromJSON(String json){
		List newList = new List(db, json);
		addListAndOpen(newList);
	}

	public void openList(int position) {
		openList(lists.get(position).getName(), lists.get(position).getDatelist());
	}
	
	public void openList(String title, String datelist) {
		if (title==null || datelist==null){
			return;
		}
		Fragment fragment = new ListFragment();
		Bundle args = new Bundle();
		args.putString(ListFragment.INTENT_TO_LIST_TITLE, title);
		args.putString(ListFragment.INTENT_TO_LIST_DATELIST, datelist);
		fragment.setArguments(args);
		FragmentManager fragmentManager = context.getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.content_frame, fragment, ListFragment.TAG).commit();
	}

	public void renameList(int position, String newName){
		int oldPosition = position;
		List editedList = lists.get(position);
		lists.get(position).rename(db, newName);
		adapter.notifyItemChanged(oldPosition);

		Collections.sort(lists, new MenuComparator());
		int newPosition = lists.indexOf(editedList);
		if (oldPosition != newPosition){
			adapter.notifyItemMoved(oldPosition, newPosition);
			mLayoutManager.scrollToPosition(0);
		}
	}

	public void removeList(int position){
		if (removedList!=null){
			removedList.remove(db);
			removedList=null;
		}
		removedList = lists.get(position);
		adapter.notifyItemRemoved(position);
		
		lists.remove(position);
		SnackbarManager.show(Snackbar.with(context.getApplicationContext())
				.text(context.getString(R.string.string_removed_list))
				.actionLabel(R.string.string_undo)
				.actionColor(context.getResources().getColor(R.color.accent))
				.actionListener(new ActionClickListener() {
					@Override
					public void onActionClicked(Snackbar snackbar) {
						if (removedList!=null){
							lists.add(removedList);
							Collections.sort(lists, new MenuComparator());
							adapter.notifyItemInserted(lists.indexOf(removedList));
							mLayoutManager.scrollToPosition(0);
							removedList = null;
						}
					}
				})
				.eventListener(new EventListener() {
					@Override
					public void onShow(Snackbar snackbar) {
						
					}
					@Override
					public void onShowByReplace(Snackbar snackbar) {
						
					}
					@Override
					public void onShown(Snackbar snackbar) {
						
					}
					@Override
					public void onDismiss(Snackbar snackbar) {
						
					}
					@Override
					public void onDismissByReplace(Snackbar snackbar) {
						
					}
					@Override
					public void onDismissed(Snackbar snackbar) {
						if (removedList!=null){
							removedList.remove(db);
							removedList = null;
						}
					}
				}), layoutForSnackBar);
	}

	public void loadListFromSMS(String sms){
		try{
			List newList = new List(db, context, sms);
			lists.add(newList);

			Collections.sort(lists, new MenuComparator());
			adapter.notifyItemInserted(lists.indexOf(newList));
		} catch(Exception e){
			//error
		}
	}

	public void LoadListFromClipboard(){
		try{
			ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			Item clipItem = clip.getPrimaryClip().getItemAt(0);
			List newList = new List(db, context, clipItem.coerceToText(context).toString());
			lists.add(newList);

			Collections.sort(lists, new MenuComparator());
			adapter.notifyDataSetChanged();
		} catch(Exception e){
			//error
		}
	}

	public void updateLists() {

	}

	public void closeDB(){
		if (db!=null){
			if (!db.isClosed()){
				db.close();
			}
		}
	}

	public void openDB(){
		if (db!=null){
			if (db.isClosed()){
				db.open();
			}
		}
	}


	public void showPopupMenu(View view, final int position) {
		PopupMenu popupMenu = new PopupMenu(context, view);
		popupMenu.inflate(R.menu.popup_menu_for_list);
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.action_change_list :{
					showDialogForEditingList(position);
					return true;
				}
				case R.id.action_remove_list:{
					removeList(position);
					return true;
				}
				case R.id.action_send_list :{
					showDialogForSendingList(position);
					return true;
				}
				case R.id.action_put_alarm:{
					showDialogForAlarm(position);
					return true;
				}
				default:
					return false;
				}
			}
		});

		popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu menu) {

			}
		});
		popupMenu.show();
	}

	private void showDialogForAlarm(final int position) {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_set_alert, null);
		
		final Calendar date = Calendar.getInstance();
		
		final Button dateText = (Button) layout.findViewById(R.id.dialog_alert_date);
		final Button time = (Button) layout.findViewById(R.id.dialog_alert_time);
		time.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener(){
					@Override
					public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
						date.set(Calendar.HOUR_OF_DAY, hourOfDay);
						date.set(Calendar.MINUTE, minute);
						date.set(Calendar.SECOND, 0);
						time.setText(new SimpleDateFormat("HH:mm").format(date.getTime()));
					}
				};
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                		timeListener,
                		date.get(Calendar.HOUR_OF_DAY),
                		date.get(Calendar.MINUTE),
                        true
                );
                tpd.setThemeDark(false);
                tpd.show(context.getFragmentManager(), "Timepickerdialog");
			}
		});
		dateText.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Calendar now = Calendar.getInstance();
				OnDateSetListener timeListener = new OnDateSetListener(){
					@Override
					public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
						date.set(year, monthOfYear, dayOfMonth);
						dateText.setText(new SimpleDateFormat("dd.MM.yyyy").format(date.getTime()));
					}
				};
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                		timeListener,
                		now.get(Calendar.YEAR),
                		now.get(Calendar.MONTH),
                		now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(context.getFragmentManager(), "Datepickerdialog");
			}
		});
		
		
		if (!lists.get(position).getAlarm().isEmpty()){
			SimpleDateFormat formatter = new SimpleDateFormat(List.FORMAT_FOR_DATE_ALARM);
			try {
				date.setTime(formatter.parse(lists.get(position).getAlarm()));
			} catch (ParseException e) {
				
			}
			
			builder.setNeutralButton(R.string.dialog_alarm_remove, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					lists.get(position).cancelAlarm(db, context);
				}
			});
		}
		time.setText(new SimpleDateFormat("HH:mm").format(date.getTime()));
		dateText.setText(new SimpleDateFormat("dd.MM.yyyy").format(date.getTime()));
		
		builder.setTitle(R.string.dialog_title_alarm);
		builder.setView(layout);
		builder.setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				lists.get(position).setAlarm(db, context, date.getTime());
			}
		});
		
		builder.setNegativeButton(R.string.dialog_cancel, null);

		final Dialog dialog = builder.create();

		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_LOADER_FROM_DB){
			result = new MenuLoader(context, args, db);
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		lists.clear();
		if (cursor==null){
			//no lists
		}
		if (cursor.moveToFirst()){
			addListFromCursor(cursor);
			while (cursor.moveToNext()){
				addListFromCursor(cursor);
			}
		}
		cursor.close();
		Collections.sort(lists, new MenuComparator());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}	

	private void addListFromCursor(Cursor cursor){
		List newList = new List(db, cursor);
		newList.sort(context);
		lists.add(newList);
	}

	///////////////////////////////////////// setters and getters
	public DB getDb() {
		return db;
	}

	public ArrayList<List> getLists() {
		return lists;
	}

	public void setLists(ArrayList<List> lists) {
		this.lists = lists;
	}

	public MenuAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(MenuAdapter adapter) {
		this.adapter = adapter;
	}

	public void showDialogForNewList() {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_new_list, null);
		final EditText edittextForName = (EditText) layout.findViewById(R.id.dialog_new_list_edittext_for_name);

		final Spinner spinnerForCurrency = (Spinner) layout.findViewById(R.id.dialog_new_list_spinner_for_currency);
		Set<String> currencys = sPref.getStringSet(SD.PREFS_CURRENCYS, new HashSet<String>());
		ArrayAdapter<String> adapterForSpinnerCurrency = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, currencys.toArray(new String[] {}));
		adapterForSpinnerCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerForCurrency.setAdapter(adapterForSpinnerCurrency);
		spinnerForCurrency.setSelection(adapterForSpinnerCurrency.getPosition(sPref.getString(SD.PREFS_DEF_CURRENCY, context.getString(R.string.prefs_default_currency))));

		builder.setTitle(R.string.dialog_title_new_list);
		builder.setView(layout);
		builder.setPositiveButton(context.getResources().getString(R.string.dialog_create), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (checkNameAndCreateNewList(edittextForName.getText().toString(), (String) spinnerForCurrency.getSelectedItem())){
					dialog.dismiss();
				}
			}
		});
		builder.setNegativeButton(context.getResources().getString(R.string.dialog_cancel), null);

		final Dialog dialog = builder.create();

		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();

		edittextForName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (checkNameAndCreateNewList(edittextForName.getText().toString(), (String) spinnerForCurrency.getSelectedItem())){
					dialog.dismiss();
				}
				return true;
			}
		});
	}

	public void showDialogForEditingList(final int position) {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_new_list, null);
		final EditText edittextForName = (EditText) layout.findViewById(R.id.dialog_new_list_edittext_for_name);
		edittextForName.setText(lists.get(position).getName());

		final Spinner spinnerForCurrency = (Spinner) layout.findViewById(R.id.dialog_new_list_spinner_for_currency);
		Set<String> currencys = sPref.getStringSet(SD.PREFS_CURRENCYS, new HashSet<String>());
		ArrayAdapter<String> adapterForSpinnerCurrency = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, currencys.toArray(new String[] {}));
		adapterForSpinnerCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerForCurrency.setAdapter(adapterForSpinnerCurrency);
		if (currencys.contains(lists.get(position).getCurrency())){
			spinnerForCurrency.setSelection(adapterForSpinnerCurrency.getPosition(lists.get(position).getCurrency()));
		} else {
			spinnerForCurrency.setSelection(adapterForSpinnerCurrency.getPosition(sPref.getString(SD.PREFS_DEF_CURRENCY, context.getString(R.string.prefs_default_currency))));
		}

		builder.setTitle(R.string.dialog_title_edit_list);
		builder.setView(layout);
		builder.setPositiveButton(context.getResources().getString(R.string.dialog_change), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (checkNameAndChangeList(edittextForName.getText().toString(), (String) spinnerForCurrency.getSelectedItem(), position)){
					dialog.dismiss();
				}
			}
		});
		builder.setNegativeButton(context.getResources().getString(R.string.dialog_cancel), null);

		final Dialog dialog = builder.create();

		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
		
		edittextForName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (checkNameAndChangeList(edittextForName.getText().toString(), (String) spinnerForCurrency.getSelectedItem(), position)){
					dialog.dismiss();
				}
				return true;
			}
		});
	}

	public boolean checkNameAndChangeList(String name, String currency, int position){
		if (name.isEmpty()){
			// please enter name of list
			return false;
		}
		lists.get(position).changeCurrency(db, currency);
		renameList(position, name);
		return true;
	}


	public boolean checkNameAndCreateNewList(String name, String currency){
		if (name.isEmpty()){
			// please enter name of list
			return false;
		}
		addNewList(name, currency);
		return true;
	}

	public void showDialogForSendingList(final int position) {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		builder.setTitle(R.string.dialog_title_send_list);
		builder.setItems(context.getResources().getStringArray(R.array.types_of_sending_list), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				lists.get(position).send(context, List.TYPES_OF_SENDING_LIST[which]);
			}
		});
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}


	public void firstStart(){
		Set<String> currencys = sPref.getStringSet(SD.PREFS_CURRENCYS, new HashSet<String>());
		if (!currencys.contains(context.getString(R.string.default_one_currency))){
			currencys.add(context.getString(R.string.default_one_currency));
		}
		if (!currencys.contains(context.getString(R.string.default_two_currency))){
			currencys.add(context.getString(R.string.default_two_currency));
		}
		if (!currencys.contains(context.getString(R.string.default_three_currency))){
			currencys.add(context.getString(R.string.default_three_currency));
		}
		editor.putStringSet(SD.PREFS_CURRENCYS, currencys);
		editor.commit();
	}



	public void loadFromSMS() {
		if (loadingSms!=null){
			if (loadingSms.getStatus().equals(AsyncTask.Status.RUNNING)){
				if (!loadingSms.isCancelled()){
					return;
				}
			}
		}
		loadingSms = new LoaderItemsFromSMS(context, this);
		loadingSms.execute();
	}
	
	@Override
	public void showToast(int text){
		SnackbarManager.show(Snackbar.with(context.getApplicationContext()).text(text), layoutForSnackBar);
	}


	private void showDialogWithSMS(final ArrayList<HashMap<String, String>> mapsSMS){
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		builder.setTitle(R.string.dialog_select_sms);
		String[] smsArray = new String[mapsSMS.size()];
		for (int i=0; i<mapsSMS.size();i++){
			smsArray[i] = getContactDisplayNameByNumber(mapsSMS.get(i).get(SD.SMS_KEY_ADDRESS)) + "\n" + mapsSMS.get(i).get(SD.SMS_KEY_DATE);
		}

		builder.setMultiChoiceItems(smsArray, null, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				mapsSMS.get(which).put(SD.SMS_KEY_CHECKED, String.valueOf(isChecked));
			}
		});

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				loadFromSMS(mapsSMS);
			}
		});
		builder.setNegativeButton(R.string.dialog_cancel, null);
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
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


	public void loadFromSMS(ArrayList<HashMap<String, String>> mapsSMS) {
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
		loadListFromSMS(sms);
	}

	@Override
	public void onSMSLoaded(ArrayList<HashMap<String, String>> loadedSms) {
		showDialogWithSMS(loadedSms);
	}
}
