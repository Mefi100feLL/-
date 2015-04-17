package com.PopCorp.Purchases.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;

public class PreferencesMainFragment extends Fragment{
	
	public static final String TAG = PreferencesMainFragment.class.getSimpleName();
	
	private ActionBarActivity context;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_prefs, container, false);
		context = (ActionBarActivity) getActivity();
		
		final String[] array = context.getResources().getStringArray(R.array.main_prefs);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item_preference, array){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				if (view == null) {
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = inflater.inflate(R.layout.item_preference, parent, false);
				}
				TextView text = (TextView) view.findViewById(R.id.item_preference_text);
				text.setText(array[position]);
				return view;
			}
		};
		
		ListView listView = (ListView) rootView.findViewById(R.id.fragment_main_prefs_listview);
		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Fragment fragment = new PreferencesFragment();
				Bundle args = new Bundle();
				switch (position){
				case 0:{
					args.putInt(PreferencesFragment.INTEGER_RESOURCE, R.xml.prefs_view_of_list);
					break;
				}
				case 1:{
					args.putInt(PreferencesFragment.INTEGER_RESOURCE, R.xml.prefs_functions_of_list);
					break;
				}
				case 2:{
					args.putInt(PreferencesFragment.INTEGER_RESOURCE, R.xml.prefs_sales);
					break;
				}
				case 3:{
					args.putInt(PreferencesFragment.INTEGER_RESOURCE, R.xml.prefs_about);
					break;
				}
				}
				fragment.setArguments(args);
				FragmentManager fragmentManager = context.getFragmentManager();
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				transaction.replace(R.id.content_frame, fragment, PreferencesFragment.TAG).commit();
			}
		});
		
		return rootView;
	}

}
