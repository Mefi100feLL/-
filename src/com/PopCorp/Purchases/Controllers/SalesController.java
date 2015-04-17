package com.PopCorp.Purchases.Controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.PopCorp.Purchases.Adapters.CategoriesAdapter;
import com.PopCorp.Purchases.Adapters.SalesAdapter;
import com.PopCorp.Purchases.Comparators.MenuComparator;
import com.PopCorp.Purchases.Data.List;
import com.PopCorp.Purchases.Data.Sale;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Fragments.SalesFragment;
import com.PopCorp.Purchases.Loaders.SalesLoader;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

public class SalesController implements LoaderCallbacks<Cursor>{

	public static final int ID_FOR_CREATE_SALES_LOADER_FROM_DB = 1;
	public static final int ID_FOR_CREATE_SALES_LOADER_FROM_NET = 2;

	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_DB = 3;
	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_NET = 4;

	private SalesFragment fragment;
	private Context context;
	private DB db;
	private SharedPreferences sPref;
	private SharedPreferences.Editor editor;

	private SalesAdapter adapter;
	private ArrayList<Sale> sales = new ArrayList<Sale>();

	private String currentShopId;
	private String currentShopName;
	
	private ArrayList<List> lists = new ArrayList<List>();
	private ArrayList<List> selectedLists = new ArrayList<List>();
	private ArrayList<String> names = new ArrayList<String>();
	private Sale selectedSale;
	private ArrayAdapter<String> adapterForSpinnerShop;
	private ArrayList<String> shopesForSpinner;
	private ArrayList<String> edizmsForSpinner;
	private ArrayAdapter<String> adapterForSpinnerEdizm;
	
	public SalesController(SalesFragment fragment, Context context, String currentShopId, String currentShopName){
		this.fragment = fragment;
		this.context = context;
		this.currentShopId = currentShopId;
		this.currentShopName = currentShopName;
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		editor = sPref.edit();
		
		db = new DB(context);
		openDB();

		adapter = new SalesAdapter(context, sales);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_SALES_LOADER_FROM_DB){
			result = new SalesLoader(context, db, currentShopId);
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor==null){
			//no sales
			return;
		}
		if (cursor.moveToFirst()){
			addSaleFromCusror(cursor);
			while (cursor.moveToNext()){
				addSaleFromCusror(cursor);
			}
		}
		cursor.close();
		if (sales.size()>0){
			fragment.showProgress(false);
		} else{
			fragment.showProgress(true);
		}
		refreshAll();
	}
	
	private void addSaleFromCusror(Cursor cursor){
		Sale newSale = new Sale(cursor);
		if (isSaleActual(newSale)){
			if (!sales.contains(newSale)){
				sales.add(newSale);
			}
		}
	}

	public void updateSales(ArrayList<Sale> data) {
		for (Sale sale : data){
			if (!sales.contains(sale)){
				sale.putInDB(db);
				sales.add(sale);
			}
		}
		if (sales.size()>0){
			fragment.showProgress(false);
		}
		refreshAll();
	}

    public void addListsAndShowDialogForSelect(Sale clickedSale) {
    	lists.clear();
    	names.clear();
    	selectedLists.clear();
    	selectedSale = clickedSale;
    	Cursor cursor = db.getAllData(DB.TABLE_LISTS);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				addListFromCursor(cursor);
				while (cursor.moveToNext()){
					addListFromCursor(cursor);
				}
			}
			cursor.close();
		}
		Collections.sort(lists, new MenuComparator());
		if (lists.size()>0){
			showDialogWithLists();
		} else{
			showDialogQuestionForCreateList();
		}
	}


	private void showDialogQuestionForCreateList() {
		MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
		builder.title(R.string.dialog_no_lists);
		builder.content(R.string.dialog_are_create_new_list);
		builder.positiveText(R.string.dialog_create);
		builder.negativeText(R.string.dialog_cancel);
		builder.callback(new MaterialDialog.ButtonCallback() {
		    @Override
		    public void onPositive(MaterialDialog dialog) {
		    	showDialogForNewList();
		    	dialog.dismiss();
		    }
		});
		Dialog dialog = builder.build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}


	private void showDialogWithLists() {
		new MaterialDialog.Builder(context)
		.title(R.string.dialog_select_list)
		.items(names.toArray(new String[] {}))
		.itemsCallbackMultiChoice(new Integer[]{}, new MaterialDialog.ListCallbackMultiChoice() {
		    @Override
		    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
		    	for (int i : which){
		    		selectedLists.add(lists.get(i));
		    	}
		        return true;
		    }
		})
		.alwaysCallMultiChoiceCallback()
		.positiveText(R.string.dialog_select)
		.negativeText(R.string.dialog_cancel)
		.callback(new MaterialDialog.ButtonCallback() {
		    @Override
		    public void onPositive(MaterialDialog dialog) {
		    	showDialogForSending();
		    }
		})
		.show();
	}


	private void addListFromCursor(Cursor cursor) {
		List newList = new List(db, cursor);
		lists.add(newList);
		names.add(newList.getName());
	}
    
    protected void showDialogForSending() {
    	MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
		builder.customView(R.layout.content_list_fields, true);
		builder.positiveText(R.string.dialog_send);
		builder.negativeText(R.string.dialog_cancel);
		builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                
            }
        });
		Dialog dialog = builder.build();
		
		AutoCompleteTextView editName = (AutoCompleteTextView) dialog.findViewById(R.id.fragment_list_edittext_for_name);
		editName.setText(selectedSale.getTitle());
		
		ImageView buttonForVoice = (ImageView) dialog.findViewById(R.id.fragment_list_button_for_voice);
		buttonForVoice.setVisibility(View.GONE);
		
		final EditText editCount = (EditText) dialog.findViewById(R.id.fragment_list_edittext_for_count);
		editCount.setText("1");
		
		final EditText editCoast = (EditText) dialog.findViewById(R.id.fragment_list_edittext_for_coast);
		editCoast.setText(selectedSale.getCoast().split(" ")[0]);
		
		EditText editComment = (EditText) dialog.findViewById(R.id.fragment_list_edittext_for_comment);
		editComment.setText(selectedSale.getSubTitle());
		
		ImageView buttonCountPlus = (ImageView) dialog.findViewById(R.id.fragment_list_count_plus);
		ImageView buttonCountMinus = (ImageView) dialog.findViewById(R.id.fragment_list_count_minus);
		buttonCountPlus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					BigDecimal count = new BigDecimal(editCount.getText().toString());
					count = count.add(new BigDecimal("1"));
					editCount.setText(count.toString());
				} catch(Exception e){

				}
			}
		});

		buttonCountMinus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					BigDecimal count = new BigDecimal(editCount.getText().toString());
					if (count.doubleValue()>=1){
						count = count.subtract(new BigDecimal("1"));
						editCount.setText(count.toString());
					}
				} catch (Exception e){

				}
			}
		});
		
		String edizm;
		if (selectedSale.getCount().isEmpty()){
			edizm = sPref.getString(SD.PREFS_DEF_EDIZM, context.getResources().getString(R.string.default_unit_one));
		} else{
			edizm = selectedSale.getCount().split(" ")[1];
		}
		edizmsForSpinner = new ArrayList<String>(sPref.getStringSet(SD.PREFS_EDIZMS, new LinkedHashSet<String>()));

		adapterForSpinnerEdizm = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, edizmsForSpinner);
		adapterForSpinnerEdizm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final Spinner spinnerForEdizm = (Spinner) dialog.findViewById(R.id.fragment_list_spinner_for_edizm);
		spinnerForEdizm.setAdapter(adapterForSpinnerEdizm);
		spinnerForEdizm.setSelection(getPositionForEdizm(edizm));

		spinnerForEdizm.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				editCoast.setHint(context.getResources().getString(R.string.string_coast_za_ed) + " " + spinnerForEdizm.getItemAtPosition(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		ArrayList<String> categories = getCategories();
		ArrayList<Integer> colors = getColors();

		ArrayList<String> prodCategs = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.prod_categories)));
		ArrayList<String> prodCategsIds = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.prod_categories_ids)));
		ArrayList<String> promCategs = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.prom_categories)));
		ArrayList<String> promCategsIds = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.prom_categories_ids)));
		
		String category;
		if (prodCategsIds.contains(selectedSale.getCategory())){
			category = prodCategs.get(prodCategsIds.indexOf(selectedSale.getCategory()));
		}
		if (promCategsIds.contains(selectedSale.getCategory())){
			category = promCategs.get(promCategsIds.indexOf(selectedSale.getCategory()));
		}
		
		CategoriesAdapter adapterForSpinnerCategory = new CategoriesAdapter(context, categories, colors);
		adapterForSpinnerCategory.setDropDownViewResource(R.layout.item_list_categories);
		Spinner spinnerForCategory = (Spinner) dialog.findViewById(R.id.fragment_list_spinner_for_category);
		spinnerForCategory.setAdapter(adapterForSpinnerCategory);
		spinnerForCategory.setSelection(adapterForSpinnerCategory.getCount()-1);
		
		shopesForSpinner = new ArrayList<String>(sPref.getStringSet(SD.PREFS_SHOPES, new LinkedHashSet<String>()));
		shopesForSpinner.add(context.getResources().getString(R.string.string_no_shop));//add item for no shop

		adapterForSpinnerShop = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, shopesForSpinner);
		adapterForSpinnerShop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinnerForShop = (Spinner) dialog.findViewById(R.id.fragment_list_spinner_for_shop);
		spinnerForShop.setAdapter(adapterForSpinnerShop);
		spinnerForShop.setSelection(getPositionForShop(currentShopName));
		
		dialog.show();
	}

	private int getPositionForEdizm(String edizm){
		if (edizm==null){
			return adapterForSpinnerEdizm.getCount()-1;
		}
		if (!edizmsForSpinner.contains(edizm)){
			if (edizm.equals("")){
				return adapterForSpinnerEdizm.getCount()-1;
			}
			edizmsForSpinner.add(0, edizm);
			addNewEdizmToPrefs(edizm);
		}
		return adapterForSpinnerEdizm.getPosition(edizm);
	}

	private void addNewEdizmToPrefs(final String newEdizm) {
		Set<String> edizmsFromPrefs = sPref.getStringSet(SD.PREFS_EDIZMS, new LinkedHashSet<String>());
		edizmsFromPrefs.add(newEdizm);
		editor.putStringSet(SD.PREFS_EDIZMS, edizmsFromPrefs);
		editor.commit();
	}
    
    private int getPositionForShop(String shop){
		if (shop==null){
			return adapterForSpinnerShop.getCount()-1;
		}
		if (!shopesForSpinner.contains(shop)){
			if (shop.equals("")){
				return adapterForSpinnerShop.getCount()-1;
			}
			shopesForSpinner.add(0, shop);
			addNewShopToPrefs(shop);
		}
		return adapterForSpinnerShop.getPosition(shop);
	}

	private void addNewShopToPrefs(final String newShop) {
		Set<String> shopesFromPrefs = sPref.getStringSet(SD.PREFS_SHOPES, new LinkedHashSet<String>());
		shopesFromPrefs.add(newShop);
		editor.putStringSet(SD.PREFS_SHOPES, shopesFromPrefs);
		editor.commit();
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
    
    public boolean checkNameAndCreateNewList(String name, String currency){
		if (name.isEmpty()){
			// please enter name of list
			return false;
		}
		addNewList(name, currency);
		return true;
	}
    
    public void addNewList(String newName, String currency){
		String datelist = String.valueOf(Calendar.getInstance().getTimeInMillis());
		long id = db.addRec(DB.TABLE_LISTS, DB.COLUMNS_LISTS, new String[] {newName, datelist, "", currency});
		List newList = new List(db, id, newName, datelist, "", currency);
		lists.add(newList);
		Collections.sort(lists, new MenuComparator());
		selectedLists.add(newList);
		showDialogForSending();
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

	

	/////////////////////////////////////////////////////////////////////////
	private void refreshAll() {
		adapter.notifyDataSetChanged();
	}

	private boolean isSaleActual(Sale newSale) {
		Calendar dateOfSaleFinish = Calendar.getInstance();
		dateOfSaleFinish.setTime(newSale.getPeriodFinish());
		dateOfSaleFinish.add(Calendar.DAY_OF_MONTH, 1);
		if (dateOfSaleFinish.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
			newSale.remove(db);
			return false;
		} else {
			return true;
		}
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

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	/////////////////////////////////////////////// SETTERS AND GETTERS //////////////////////////////////////////////
	public String getCurrentShop() {
		return currentShopId;
	}

	public SalesAdapter getAdapter() {
		return adapter;
	}


	public Sale getSale(int position) {
		return sales.get(position);
	}
}
