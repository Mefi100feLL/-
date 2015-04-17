package com.PopCorp.Purchases.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.PopCorp.Purchases.Controllers.PreferencesController;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;

public class PreferencesFragment extends PreferenceFragment {

	public static final String TAG = PreferencesFragment.class.getSimpleName();

	public static final String INTEGER_RESOURCE = "INTEGER_RESOURCE";

	private Toolbar toolBar;
	private ActionBarActivity context;
	private SharedPreferences sPref;
	private PreferencesController controller;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(getArguments().getInt(INTEGER_RESOURCE));
		context = (ActionBarActivity) getActivity();
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		controller = new PreferencesController(context, this, sPref, sPref.edit());

		initializePrefsForViewOfList();
		initializePrefsForFunctionOfList();
		initializePrefsForSales();
	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		toolBar = (Toolbar) getActivity().findViewById(R.id.activity_main_toolbar);
		ListView listView = (ListView) rootView.findViewById (android.R.id.list);
		listView.setSelector(R.drawable.selector_for_normal_list);
		listView.setFooterDividersEnabled(false);
		return rootView;
	}

	public void onBackPressed() {
		toolBar.setTitle(R.string.menu_settings);
		Fragment fragment = new PreferencesMainFragment();
		String tag = PreferencesMainFragment.TAG;

		FragmentManager fragmentManager = context.getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();
	}

	private void initializePrefsForViewOfList() {
		final Preference prefSortListItem = (Preference) findPreference(SD.PREFS_SORT_LIST_ITEM);
		if (prefSortListItem!=null){
			prefSortListItem.setSummary(context.getString(R.string.prefs_default_sort) + " " + sPref.getString(SD.PREFS_SORT_LIST_ITEM, context.getString(R.string.prefs_default_sort_listitem)));
			prefSortListItem.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					prefSortListItem.setSummary(context.getString(R.string.prefs_default_sort) + " " + newValue);
					return true;
				}
			});
		}

		final Preference prefFontSize = (Preference) findPreference(SD.PREFS_LIST_ITEM_FONT_SIZE);
		if (prefFontSize!=null){
			prefFontSize.setSummary(context.getString(R.string.prefs_text_size_summary) + " " + sPref.getString(SD.PREFS_LIST_ITEM_FONT_SIZE, "14"));

			prefFontSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					prefFontSize.setSummary(context.getString(R.string.prefs_text_size_summary) + " " + newValue);
					return true;
				}
			});
		}

		final Preference prefFontSizeSmall = (Preference) findPreference(SD.PREFS_LIST_ITEM_FONT_SIZE_SMALL);
		if (prefFontSizeSmall!=null){
			prefFontSizeSmall.setSummary(context.getString(R.string.prefs_text_size_summary_small) + " " + sPref.getString(SD.PREFS_LIST_ITEM_FONT_SIZE_SMALL, "12"));
			prefFontSizeSmall.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					prefFontSizeSmall.setSummary(context.getString(R.string.prefs_text_size_summary_small) + " " + newValue);
					return true;
				}
			});
		}

		final Preference prefCategories = (Preference) findPreference(SD.PREFS_CATEGORIES);
		if (prefCategories!=null){
			prefCategories.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					controller.showDialogWithCategories();
					return true;
				}
			});
		}
	}
	
	private void initializePrefsForFunctionOfList() {
		Preference prefCurrency = (Preference) findPreference(SD.PREFS_CURRENCY);
		if (prefCurrency!=null){
			prefCurrency.setSummary(context.getString(R.string.prefs_default_currency) + " " + sPref.getString(SD.PREFS_DEF_CURRENCY, context.getString(R.string.default_one_currency)));
			prefCurrency.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					controller.showDialogWithCurrencies();
					return true;
				}
			});
		}
		
		Preference prefUnit = (Preference) findPreference(SD.PREFS_UNIT);
		if (prefUnit!=null){
			prefUnit.setSummary(context.getString(R.string.prefs_default_unit) + " " + sPref.getString(SD.PREFS_DEF_EDIZM, context.getString(R.string.default_unit_one)));
			prefUnit.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					controller.showDialogWithUnits();
					return true;
				}
			});
		}
		
		Preference shopes = (Preference) findPreference(SD.PREFS_SHOPES);
		if (shopes!=null){
			shopes.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					controller.showDialogWithShopes();
					return true;
				}
			});
		}
	}
	
	private void initializePrefsForSales() {
		Preference prefCity = (Preference) findPreference(SD.PREFS_CITY);
		if (prefCity!=null){
			prefCity.setSummary(controller.getCityForId(sPref.getString(SD.PREFS_CITY, "1")));
			prefCity.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					controller.showDialogWithCities();
					return true;
				}
			});
		}
	}



	public void selectCurrency(String selectedCurrency) {
		Preference prefCurrency = (Preference) findPreference(SD.PREFS_CURRENCY);
		if (prefCurrency!=null){
			prefCurrency.setSummary(context.getString(R.string.prefs_default_currency) + " " + selectedCurrency);
		}
	}
	
	public void selectUnit(String selectedUnit) {
		Preference prefUnit = (Preference) findPreference(SD.PREFS_UNIT);
		if (prefUnit!=null){
			prefUnit.setSummary(context.getString(R.string.prefs_default_unit) + " " + selectedUnit);
		}
	}
	
	public void selectCity(String selectedCity) {
		Preference prefCity = (Preference) findPreference(SD.PREFS_CITY);
		if (prefCity!=null){
			prefCity.setSummary(selectedCity);
		}
	}



	

}
