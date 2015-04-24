package com.PopCorp.Purchases.Controllers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Fragments.PreferencesFragment;
import com.PopCorp.Purchases.Views.ColorPickerView;
import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

public class PreferencesController {

	private AppCompatActivity context;
	private SharedPreferences sPref;
	private SharedPreferences.Editor editor;
	private PreferencesFragment fragment;
	private DB db;
	
	private ArrayList<String> currencies;
	private String selectedCurrency;
	
	private ArrayList<String> units;
	private String selectedUnit;

	private ArrayList<String> shopes;
	
	public PreferencesController(AppCompatActivity context, PreferencesFragment fragment, SharedPreferences sPref, SharedPreferences.Editor editor){
		this.context = context;
		this.sPref = sPref;
		this.editor = editor;
		this.fragment = fragment;
		db = new DB(context);
		openDB();
	}
	

	public void showDialogWithCities() {
		ArrayList<String> cities = new ArrayList<String>();
		final ArrayList<String> citiesIds = new ArrayList<String>();
		Cursor cursor = db.getAllData(DB.TABLE_CITIES);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				cities.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_NAME)));
				citiesIds.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_ID)));
				while (cursor.moveToNext()){
					cities.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_NAME)));
					citiesIds.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_ID)));
				}
			}
			cursor.close();
		}
		Dialog dialog = new MaterialDialog.Builder(context)
        .title(R.string.prefs_region)
        .items(cities.toArray(new String[] {}))
        .itemsCallbackSingleChoice(citiesIds.indexOf(sPref.getString(SD.PREFS_CITY, "1")), new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                editor.putString(SD.PREFS_CITY, citiesIds.get(which)).commit();
                fragment.selectCity(text.toString());
                return true;
            }
        })
        .positiveText(R.string.dialog_select)
        .negativeText(R.string.dialog_cancel)
        .build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public void showDialogWithShopes() {
		shopes = getShopes();
		Dialog dialog = new MaterialDialog.Builder(context)
        .title(R.string.prefs_shops)
        .items(shopes.toArray(new String[] {}))
        .itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                showDialogForEditingShop(text.toString());
            }
        })
        .positiveText(R.string.dialog_add)
        .negativeText(R.string.dialog_cancel)
        .callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
            	showDialogForNewShop();
            }
        })
        .build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	private void showDialogForNewShop() {
		Dialog dialog = new MaterialDialog.Builder(context)
        .title(R.string.string_new_shop)
        .positiveText(R.string.dialog_add)
        .negativeText(R.string.dialog_cancel)
        .input(R.string.string_shop_name, 0, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
            	String newShop = dialog.getInputEditText().getText().toString();
            	if (!shopes.contains(newShop)){
            		shopes.add(newShop);
                	Set<String> setToPrefs = new LinkedHashSet<String>(shopes);
                	editor.putStringSet(SD.PREFS_SHOPES, setToPrefs).commit();
            	}
            }
        })
        .build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	private void showDialogForEditingShop(final String shop) {
		Dialog dialog = new MaterialDialog.Builder(context)
        .title(R.string.string_editing_shop)
        .positiveText(R.string.dialog_save)
        .negativeText(R.string.dialog_remove)
        .neutralText(R.string.dialog_cancel)
        .input(context.getString(R.string.string_shop_name), shop, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
            	String newShop = dialog.getInputEditText().getText().toString();
            	if (!shop.equals(newShop)){
            		shopes.add(newShop);
            		shopes.remove(shop);
                	Set<String> setToPrefs = new LinkedHashSet<String>(shopes);
                	editor.putStringSet(SD.PREFS_SHOPES, setToPrefs).commit();
            	}
            }
        })
        .callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onNegative(MaterialDialog dialog) {
            	shopes.remove(shop);
            	Set<String> setToPrefs = new LinkedHashSet<String>(shopes);
            	editor.putStringSet(SD.PREFS_SHOPES, setToPrefs).commit();
            }
        })
        .build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public void showDialogWithUnits() {
		units = getUnits();
		final String currentUnit = sPref.getString(SD.PREFS_DEF_EDIZM, context.getString(R.string.default_unit_one));
		selectedUnit = currentUnit;
		MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
		builder.title(R.string.prefs_unit);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
            	if (selectedUnit==units.get(0)){
            		showDialogForAddUnit();
            		return;
            	}
            	if (!selectedUnit.equals(currentUnit)){
            		editor.putString(SD.PREFS_DEF_EDIZM, selectedUnit).commit();
            		fragment.selectUnit(selectedUnit);
            	}
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
            	if (selectedUnit==units.get(0) || units.size()<3 || selectedUnit.equals(currentUnit)){
            		return;
            	}
            	units.remove(selectedUnit);
            	units.remove(context.getString(R.string.string_add_unit));
            	Set<String> setToPrefs = new LinkedHashSet<String>(units);
            	editor.putStringSet(SD.PREFS_EDIZMS, setToPrefs).commit();
            }
        });
		builder.items(units.toArray(new String[]{}));
		builder.alwaysCallSingleChoiceCallback();
		builder.itemsCallbackSingleChoice(units.indexOf(currentUnit), new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
            	selectedUnit = units.get(which);
                return true;
            }
        });
        builder.positiveText(R.string.dialog_select);
        builder.neutralText(R.string.dialog_cancel);
        builder.negativeText(R.string.dialog_remove);
        Dialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
	}
	
	private void showDialogForAddUnit() {
		Dialog dialog = new MaterialDialog.Builder(context)
        .title(R.string.string_new_unit)
        .positiveText(R.string.dialog_add)
        .negativeText(R.string.dialog_cancel)
        .input(R.string.string_unit_name, 0, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
            	String newUnit = dialog.getInputEditText().getText().toString();
            	if (!units.contains(newUnit)){
            		units.add(newUnit);
            		units.remove(context.getString(R.string.string_add_unit));
                	Set<String> setToPrefs = new LinkedHashSet<String>(units);
                	editor.putStringSet(SD.PREFS_EDIZMS, setToPrefs).commit();
            	}
            }
        })
        .build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	public void showDialogWithCurrencies() {
		currencies = getCurrencies();
		final String currentCurrency = sPref.getString(SD.PREFS_DEF_CURRENCY, context.getString(R.string.default_one_currency));
		selectedCurrency = currentCurrency;
		MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
		builder.title(R.string.prefs_currency);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
            	if (selectedCurrency==currencies.get(0)){
            		showDialogForAddCurrency();
            		return;
            	}
            	if (!selectedCurrency.equals(currentCurrency)){
            		editor.putString(SD.PREFS_DEF_CURRENCY, selectedCurrency).commit();
            		fragment.selectCurrency(selectedCurrency);
            	}
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
            	if (selectedCurrency==currencies.get(0) || currencies.size()<3 || selectedCurrency.equals(currentCurrency)){
            		return;
            	}
            	currencies.remove(selectedCurrency);
        		currencies.remove(context.getString(R.string.string_add_currency));
            	Set<String> setToPrefs = new LinkedHashSet<String>(currencies);
            	editor.putStringSet(SD.PREFS_CURRENCYS, setToPrefs).commit();
            }
        });
		builder.items(currencies.toArray(new String[]{}));
		builder.alwaysCallSingleChoiceCallback();
		builder.itemsCallbackSingleChoice(currencies.indexOf(currentCurrency), new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
            	selectedCurrency = currencies.get(which);
                return true;
            }
        });
        builder.positiveText(R.string.dialog_select);
        builder.neutralText(R.string.dialog_cancel);
        builder.negativeText(R.string.dialog_remove);
        Dialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
	}

	private void showDialogForAddCurrency() {
		Dialog dialog = new MaterialDialog.Builder(context)
        .title(R.string.string_new_currency)
        .positiveText(R.string.dialog_add)
        .negativeText(R.string.dialog_cancel)
        .input(R.string.string_currency_name, 0, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
            	String newCurrency = dialog.getInputEditText().getText().toString();
            	if (!currencies.contains(newCurrency)){
            		currencies.add(newCurrency);
            		currencies.remove(context.getString(R.string.string_add_currency));
                	Set<String> setToPrefs = new LinkedHashSet<String>(currencies);
                	editor.putStringSet(SD.PREFS_CURRENCYS, setToPrefs).commit();
            	}
            }
        })
        .build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private ArrayList<String> getCurrencies() {
		ArrayList<String> result = new ArrayList<String>(sPref.getStringSet(SD.PREFS_CURRENCYS, new LinkedHashSet<String>()));
		result.add(0, context.getString(R.string.string_add_currency));
		return result;
	}
	
	private ArrayList<String> getUnits() {
		ArrayList<String> result = new ArrayList<String>(sPref.getStringSet(SD.PREFS_EDIZMS, new LinkedHashSet<String>()));
		result.add(0, context.getString(R.string.string_add_unit));
		return result;
	}
	
	private ArrayList<String> getShopes() {
		ArrayList<String> result = new ArrayList<String>(sPref.getStringSet(SD.PREFS_SHOPES, new LinkedHashSet<String>()));
		return result;
	}

	public void showDialogWithCategories() {
		MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
		final ArrayList<String> categories = getCategories();
		final ArrayList<Integer> colors = getColors();

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item_list_categories, categories){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				if (view == null) {
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = inflater.inflate(R.layout.item_list_categories, parent, false);
				}
				((ImageView) view.findViewById(R.id.categ_list_item_imageview)).setBackgroundColor(colors.get(position));
				((TextView) view.findViewById(R.id.categ_list_item_textview)).setText(categories.get(position));
				return view;
			}
		};
		builder.adapter(adapter, new MaterialDialog.ListCallback() {
			@Override
			public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
				showDialogForCategoryChange(categories, colors, adapter, which);
			}
		});
		builder.title(R.string.prefs_categories_of_products);
		builder.negativeText(R.string.dialog_cancel);
		builder.positiveText(R.string.dialog_add);
		builder.callback(new MaterialDialog.ButtonCallback() {
			@Override
			public void onPositive(MaterialDialog dialog) {
				showDialogForNewCategory(categories, colors);
			}
		});
		Dialog dialog = builder.build();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}



	private void showDialogForCategoryChange(final ArrayList<String> categories, final ArrayList<Integer> colors, final ArrayAdapter<String> categoriesAdapter, final int which) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_color_picker, null);
		builder.setView(layout);

		final EditText edtext = (EditText)layout.findViewById(R.id.editText);
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(50);
		edtext.setFilters(FilterArray);
		edtext.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		edtext.setText(categories.get(which));
		final ColorPickerView colorPicker = (ColorPickerView)layout.findViewById(R.id.color_picker_view);
		colorPicker.setColor(colors.get(which));

		builder.setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which1) {
				dialog.cancel();
			}
		});
		builder.setNeutralButton(R.string.dialog_remove, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which1) {
				Set<String> categs = sPref.getStringSet("categories", new LinkedHashSet<String>());
				categs.remove(String.valueOf(colors.get(which)) + "!" + categories.get(which));
				categories.remove(which);
				colors.remove(which);
				dialog.cancel();
			}
		});
		builder.setPositiveButton(R.string.dialog_save, null);
		final Dialog dialog1 = builder.create();
		dialog1.setCanceledOnTouchOutside(false);
		dialog1.show();
		dialog1.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				categoriesAdapter.notifyDataSetChanged();
			}
		});
		edtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (edtext.getText().toString().length()>0){
					Set<String> categs = sPref.getStringSet("categories", new LinkedHashSet<String>());
					ArrayList<String> categor = new ArrayList<String>(categs);
					categor.set(categor.indexOf(String.valueOf(colors.get(which))+"!"+categories.get(which)), colorPicker.getColor()+"!"+edtext.getText().toString());
					categs.clear();
					categs.addAll(categor);
					colors.set(which, colorPicker.getColor());
					categories.set(which, edtext.getText().toString());
				}else{
					//Toast.makeText(context, R.string.enter_name_of_category, Toast.LENGTH_SHORT).toast.show();
					return true;
				}
				dialog1.cancel();
				return true;
			}});
		Button load = (Button) ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
		load.setOnClickListener(new View.OnClickListener(){
			public void onClick(View p1)
			{
				if (edtext.getText().toString().length()>0){
					Set<String> categs = sPref.getStringSet("categories", new LinkedHashSet<String>());
					ArrayList<String> categor = new ArrayList<String>(categs);
					categor.set(categor.indexOf(String.valueOf(colors.get(which))+"!"+categories.get(which)), colorPicker.getColor()+"!"+edtext.getText().toString());
					categs.clear();
					categs.addAll(categor);
					colors.set(which, colorPicker.getColor());
					categories.set(which, edtext.getText().toString());
				}else{
					//Toast.makeText(context, R.string.enter_name_of_category, Toast.LENGTH_SHORT).toast.show();
					return;
				}
				dialog1.cancel();
			}
		});
	}

	private void showDialogForNewCategory(final ArrayList<String> categories, final ArrayList<Integer> colors) {
		AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_color_picker, null);
		builder.setView(layout);

		final EditText editForCategoryName = (EditText)layout.findViewById(R.id.editText);
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(50);
		editForCategoryName.setFilters(FilterArray);
		editForCategoryName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		final ColorPickerView colorPicker = (ColorPickerView)layout.findViewById(R.id.color_picker_view);
		colorPicker.setColor(Color.BLUE);
		builder.setNegativeButton(R.string.dialog_cancel, null);
		builder.setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (editForCategoryName.getText().toString().length()>0){
					String name = editForCategoryName.getText().toString();
					if (!categories.contains(name)){
						db.addRec(DB.TABLE_CATEGORIES, new String[] {DB.KEY_CATEGS_NAME, DB.KEY_CATEGS_COLOR}, new String[] {name, String.valueOf(colorPicker.getColor())});
					}
				}
			}
		});

		final Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
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
		return result;
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



	public String getCityForId(String id) {
		String result = "";
		Cursor cursor = db.getdata(DB.TABLE_CITIES, new String[] {DB.KEY_CITY_NAME}, DB.KEY_CITY_ID + "='" + id + "'", null, null, null, null);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				result = cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_NAME));
			}
			cursor.close();
		}
		return result;
	}



	
}
