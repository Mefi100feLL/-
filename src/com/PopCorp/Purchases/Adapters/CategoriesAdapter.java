package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;

public class CategoriesAdapter extends ArrayAdapter<String> {

	private Context context;
	private ArrayList<String> categories;
	private ArrayList<Integer> colors;

	public CategoriesAdapter(Context context, ArrayList<String> categories, ArrayList<Integer> colors) {
		super(context, R.layout.item_list_categories);
		this.context = context;
		this.categories = categories;
		this.colors = colors;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.item_list_categories, parent, false);
		}
		((ImageView) view.findViewById(R.id.categ_list_item_imageview)).setBackgroundColor(colors.get(position));
		((TextView) view.findViewById(R.id.categ_list_item_textview)).setText(categories.get(position));
		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.item_list_categories, parent, false);
		}
		((ImageView) view.findViewById(R.id.categ_list_item_imageview)).setBackgroundColor(colors.get(position));
		((TextView) view.findViewById(R.id.categ_list_item_textview)).setVisibility(View.GONE);
		return view;
	}
	
	@Override
	public int getCount()
	{
		return categories.size();
	}
	
	@Override
	public String getItem(int position)
	{
		return categories.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	public int getItemPosition(Object obj){
		return categories.indexOf(obj);
	}
}
