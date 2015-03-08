package com.PopCorp.Purchases.Controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.PopCorp.Purchases.Activities.MainActivity;
import com.PopCorp.Purchases.Adapters.ListAdapter;
import com.PopCorp.Purchases.Comparators.ListComparator;
import com.PopCorp.Purchases.Data.List;
import com.PopCorp.Purchases.Data.ListItem;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Fragments.ListFragment;
import com.PopCorp.Purchases.Loaders.ListLoader;
import com.PopCorp.Purchases.Loaders.LoaderItemsFromSMS;
import com.PopCorp.Purchases.Loaders.LoaderItemsFromSMS.CallbackForLoadingSMS;
import com.PopCorp.Purchases.Utilites.ListWriter;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

public class ListController implements LoaderCallbacks<Cursor>, CallbackForLoadingSMS{

	public static final int ID_FOR_CREATE_LOADER_FROM_DB = 1;
	
	private MainActivity context;
	private DB db;
	private ListFragment fragment;
	private SharedPreferences sPref;

	private LoaderItemsFromSMS loadingSms;
	
	private List currentList;
	private ListAdapter adapter;

	private ViewGroup layoutForSnackBar;
	
	private ArrayList<String> shopsForFilter = new ArrayList<String>();
	private String filterShop = "";
	
	private ArrayList<ListItem> itemsForRemove = new ArrayList<ListItem>();
	private ListItem editedItem;
	
	public ListController(ListFragment fragment, String title, String datelist, RecyclerView listView, ViewGroup layoutForSnackBar){
		this.fragment = fragment;
		this.layoutForSnackBar = layoutForSnackBar;
		context = (MainActivity) fragment.getActivity();
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		db = new DB(context);
		openDB();
		
		openList(datelist);
		Collections.sort(currentList.getItems(), new ListComparator());
		adapter = new ListAdapter(context, currentList.getItems(), this, currentList.getCurrency(), listView);
	}
	
	public ListController(ListFragment fragment, String json, RecyclerView listView, ViewGroup layoutForSnackBar){
		this.fragment = fragment;
		this.layoutForSnackBar = layoutForSnackBar;
		context = (MainActivity) fragment.getActivity();
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		db = new DB(context);
		openDB();
		
		createListFromJSON(json);
		Collections.sort(currentList.getItems(), new ListComparator());
		adapter = new ListAdapter(context, currentList.getItems(), this, currentList.getCurrency(), listView);
	}
	
	private void openList(String datelist){
		Cursor cursor = db.getdata(DB.TABLE_LISTS, DB.COLUMNS_LISTS_WITH_ID, DB.KEY_LISTS_DATELIST + "='" + datelist + "'", null, null, null, null);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				currentList = new List(null, cursor);
			}
			cursor.close();
		}
		if (currentList==null){
			fragment.backToLists();
		}
	}
	
	private void createListFromJSON(String json) {
		currentList = new List(db, json);
	}
	
	
	////////////////////////////////////////////////////////////// OPERATIONS WITH LIST ///////////////////////////////////////////////////
	public void renameCurrentList(String newName) {
		currentList.rename(db, newName);
		fragment.setTitle(newName);
	}	

	public void putAlarm(Date dateAlarm) {
		currentList.setAlarm(db, context, dateAlarm);
	}
	
	public void cancelAlarm() {
		currentList.cancelAlarm(db, context);
	}
	
	public void removeCurrentList() {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		builder.setTitle(R.string.dialog_title_remove_list);
		builder.setMessage(R.string.dialog_are_you_sure_to_remove_list);
		builder.setPositiveButton(R.string.dialog_remove, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				currentList.remove(db);
				fragment.backToLists();
			}
		});
		builder.setNegativeButton(R.string.dialog_cancel, null);
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	////////////////////////////////////////////////////////////// OPERATIONS WITH LIST ///////////////////////////////////////////////////

	
	/////////////////////////////////////////////////////////////// OPERATIONS WITH ITEMS //////////////////////////////////////////////////
	public void changeItemBuyed(ListItem item){
		item.changeBuyed(db);
		adapter.notifyItemChanged(adapter.getPublishItems().indexOf(item));
		recoastTotals();
	}
	
	public void startEditingItem(ListItem listItem) {
		editedItem = listItem;
		adapter.notifyItemChanged(adapter.getPublishItems().indexOf(listItem));
		fragment.putItemInFields(editedItem);
		fragment.getFloatingButton().setImageResource(R.drawable.ic_create_white_24dp);
		fragment.showFloatingButton();
	}

	public void removeItems(ArrayList<ListItem> selectedItems) {
		removeItemsFromTmpArray();
		itemsForRemove.addAll(selectedItems);
		currentList.getItems().removeAll(itemsForRemove);
		refreshAll();
		
		String text = "";
		if (itemsForRemove.size()==1){
			text = context.getString(R.string.string_removed) + " " + itemsForRemove.get(0).getName();
		} else{
			text = context.getString(R.string.string_removed_elements) + " " + itemsForRemove.size();
		}
		SnackbarManager.show(Snackbar.with(context.getApplicationContext())
				.text(text)
				.actionLabel(R.string.string_undo)
				.actionColor(context.getResources().getColor(R.color.accent))
				.actionListener(new ActionClickListener() {
					@Override
					public void onActionClicked(Snackbar snackbar) {
						if (itemsForRemove!=null){
							currentList.getItems().addAll(itemsForRemove);
							refreshAll();
							itemsForRemove.clear();
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
						removeItemsFromTmpArray();
					}
				}), layoutForSnackBar);
	}

	private void removeItemsFromTmpArray() {
		if (itemsForRemove!=null){
			currentList.removeItems(db, itemsForRemove);
			itemsForRemove.clear();
		}
	}
	
	
	public void addNewListItem(String name, String count, String edizm, String coast, String category, String shop, String comment, String important){
		if (editedItem!=null){
			editItem(name, count, edizm, coast, category, shop, comment, important);
			return;
		}
		ListItem newItem = currentList.addNewItem(db, name, count, edizm, coast, category, shop, comment, "false", important);
		if (newItem==null){
			// TODO Auto-generated method stub
			//product all exists
		} else{
			refreshAll();
		}
	}

	private void editItem(String name, String count, String edizm, String coast, String category, String shop, String comment, String important) {
		int oldPosition = adapter.getPublishItems().indexOf(editedItem);
		adapter.notifyItemChanged(oldPosition);
		editedItem.update(db, name, count, edizm, coast, category, shop, comment, important);
		refreshAll();
		adapter.setUpdatedItem(oldPosition, editedItem);
		editedItem = null;
	}

	public void sendItems(ArrayList<ListItem> selectedItems) {
		// TODO Auto-generated method stub
		
	}
	/////////////////////////////////////////////////////////////// OPERATIONS WITH ITEMS //////////////////////////////////////////////////
	
	public void refreshAll(){
		recoastTotals();
		refreshFilterShops();
	}
	
	private void recoastTotals(){
		currentList.recoastTotals();
		fragment.showTotals(currentList.getTotalBuyed(), currentList.getTotal(), String.valueOf(currentList.getItems().size()));
	}
	
	public void refreshFilterShops(){
		shopsForFilter.clear();
		shopsForFilter.addAll(currentList.refreshFilterShops());
		if (shopsForFilter.size()==0){
			filterShop = context.getString(R.string.string_all_shops);
			fragment.hideFilterMenuItem();
		} else {
			shopsForFilter.add(0, context.getString(R.string.string_all_shops));
			if (!shopsForFilter.contains(filterShop)){
				filterShop = context.getString(R.string.string_all_shops);
			}
			fragment.showFilterMenuItem(shopsForFilter, filterShop);
		}
		selectFilter(filterShop);
	}
	
	public void selectFilter(String filter){
		adapter.getFilter().filter(filter);
	}
	
	
	public void updateArray(ArrayList<ListItem> newArray){
		currentList.updateItems(db, newArray);
		refreshAll();
	}
	
	
	
	
	
	
	
	
	
	
	//////////////////////////////////////////////////////////// LOADER ITEMS FROM DB //////////////////////////////////////////////////
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_LOADER_FROM_DB){
			result = new ListLoader(context, db, currentList.getDatelist());
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor!=null){
			if (cursor.moveToFirst()){
				currentList.addListItem(cursor);
				while (cursor.moveToNext()){
					currentList.addListItem(cursor);
				}
			}
			cursor.close();
			Collections.sort(currentList.getItems(), new ListComparator());
			recoastTotals();
			refreshFilterShops();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
	////////////////////////////////////////////////////////////LOADER ITEMS FROM DB //////////////////////////////////////////////////
	
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
	
	
	
	public void showDialogForSendingList() {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		builder.setTitle(R.string.dialog_title_send_list);
		builder.setItems(context.getResources().getStringArray(R.array.types_of_sending_list), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				currentList.send(context, List.TYPES_OF_SENDING_LIST[which]);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	public void showDialogForAlarm() {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_set_alert, null);
		
		final TimePicker timePicker = (TimePicker) layout.findViewById(R.id.dialog_set_alarm_timepicker);
		timePicker.setIs24HourView(true);
		final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.dialog_set_alarm_datepicker);
		Calendar date = Calendar.getInstance();
		if (!currentList.getAlarm().equals("")){
			SimpleDateFormat formatter = new SimpleDateFormat(List.FORMAT_FOR_DATE_ALARM);
			try {
				date.setTime(formatter.parse(currentList.getAlarm()));
			} catch (ParseException e) {
				date = Calendar.getInstance();
			}
			
			builder.setNeutralButton(R.string.dialog_alarm_remove, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					currentList.cancelAlarm(db, context);
				}
			});
		}
		timePicker.setCurrentHour(date.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(date.get(Calendar.MINUTE));
		
		datePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		
		builder.setTitle(R.string.dialog_title_alarm);
		builder.setView(layout);
		builder.setPositiveButton(R.string.dialog_alarm_set, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
				currentList.setAlarm(db, context, calendar.getTime());
			}
		});
		
		builder.setNegativeButton(R.string.dialog_cancel, null);

		final AlertDialog dialog = builder.create();

		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	public void showDialogForEditingList() {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_new_list, null);
		final EditText edittextForName = (EditText) layout.findViewById(R.id.dialog_new_list_edittext_for_name);
		edittextForName.setText(currentList.getName());
		
		final Spinner spinnerForCurrency = (Spinner) layout.findViewById(R.id.dialog_new_list_spinner_for_currency);
		Set<String> currencys = sPref.getStringSet(SD.PREFS_CURRENCYS, new HashSet<String>());
		ArrayAdapter<String> adapterForSpinnerCurrency = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, currencys.toArray(new String[] {}));
		adapterForSpinnerCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerForCurrency.setAdapter(adapterForSpinnerCurrency);
		if (currencys.contains(currentList.getCurrency())){
			spinnerForCurrency.setSelection(adapterForSpinnerCurrency.getPosition(currentList.getCurrency()));
		} else {
			spinnerForCurrency.setSelection(adapterForSpinnerCurrency.getPosition(sPref.getString(SD.PREFS_DEF_CURRENCY, context.getString(R.string.prefs_default_currency))));
		}
		
		builder.setTitle(R.string.dialog_title_edit_list);
		builder.setView(layout);
		builder.setPositiveButton(context.getResources().getString(R.string.dialog_change), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (checkNameAndChangeList(edittextForName.getText().toString(), (String) spinnerForCurrency.getSelectedItem())){
					dialog.dismiss();
				}
			}
		});
		builder.setNegativeButton(context.getResources().getString(R.string.dialog_cancel), null);
		final AlertDialog dialog =builder.create();

		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
		
		edittextForName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (checkNameAndChangeList(edittextForName.getText().toString(), (String) spinnerForCurrency.getSelectedItem())){
					dialog.dismiss();
				}
				return true;
			}
		});
	}
	
	public boolean checkNameAndChangeList(String name, String currency){
		if (name.isEmpty()){
			// TODO Auto-generated method stub
			//plaese input name
			return false;
		}
		if (!currentList.getName().equals(name)){
			renameCurrentList(name);
		}
		if (!currentList.getCurrency().equals(currency)){
			currentList.changeCurrency(db, currency);
			adapter.setCurrency(currency);
			adapter.notifyDataSetChanged();
		}
		refreshAll();
		return true;
	}

	public void LoadListFromClipboard(){
		try{
			ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			Item clipItem = clip.getPrimaryClip().getItemAt(0);
			new ListWriter(context).read(db, context, clipItem.coerceToText(context).toString(), currentList, false);
			refreshAll();
		} catch(Exception e){
			// TODO Auto-generated method stub
			//eroor loading
		}
	}

	public boolean closeActionMode() {
		if (adapter.getActionMode()!=null){
			adapter.getActionMode().finish();
			return true;
		}
		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		for (String shop : shopsForFilter){
			if (itemId == shop.hashCode()){
				if (!shop.equals(filterShop)){
					filterShop = shop;
					selectFilter(filterShop);
					return true;
				}
			}
		}
		return false;
	}
	
	//////////////////////////////////////////////////////////////// LOADING FROM SMS /////////////////////////////////////////////////////////////
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
	public void onSMSLoaded(ArrayList<HashMap<String, String>> loadedSms) {
		showDialogWithSMS(loadedSms);
	}

	private void showDialogWithSMS(final ArrayList<HashMap<String, String>> mapsSMS){
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		builder.setTitle(R.string.dialog_select_sms);
		String[] smsArray = new String[mapsSMS.size()];
		for (int i=0; i<mapsSMS.size();i++){
			smsArray[i] = loadingSms.getContactDisplayNameByNumber(mapsSMS.get(i).get(SD.SMS_KEY_ADDRESS)) + "\n" + mapsSMS.get(i).get(SD.SMS_KEY_DATE);
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
				String sms = loadingSms.getSelectedSMS(mapsSMS);
				new ListWriter(context).read(db,context, sms, currentList, false);
			}
		});
		builder.setNegativeButton(R.string.dialog_cancel, null);
		final AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	////////////////////////////////////////////////////////////////LOADING FROM SMS /////////////////////////////////////////////////////////////
	
	
	/////////////////////////////////////////////////////// SETTERS AND GETTERS ////////////////////////////////////
	public ListAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(ListAdapter adapter) {
		this.adapter = adapter;
	}
	
	public List getCurrentList() {
		return currentList;
	}
	
	public ListItem getEditedItem() {
		return editedItem;
	}

	public ArrayList<String> getCategories() {
		ArrayList<String> result = new ArrayList<String>();
		Cursor cursor = db.getAllData(DB.TABLE_CATEGORIES);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				result.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGS_NAME)));
				while (cursor.moveToNext()){
					result.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGS_NAME)));
				}
			}
			cursor.close();
		}
		result.add(context.getString(R.string.string_no_category));
		return result;
	}

	public ArrayList<Integer> getColors() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		Cursor cursor = db.getAllData(DB.TABLE_CATEGORIES);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				result.add(cursor.getInt(cursor.getColumnIndex(DB.KEY_CATEGS_COLOR)));
				while (cursor.moveToNext()){
					result.add(cursor.getInt(cursor.getColumnIndex(DB.KEY_CATEGS_COLOR)));
				}
			}
			cursor.close();
		}
		result.add(context.getResources().getColor(android.R.color.transparent));
		return result;
	}
}
