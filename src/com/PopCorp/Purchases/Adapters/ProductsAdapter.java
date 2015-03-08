package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Data.ListItem;

public class ProductsAdapter extends BaseAdapter{

	private Context context;
	private ArrayList<ListItem> items;
	private ArrayList<String> categories;
	private ArrayList<Integer> colors;
	
	public ProductsAdapter(Context context, ArrayList<ListItem> items, ArrayList<String> categories, ArrayList<Integer> colors){
		super();
		this.context = context;
		this.items = items;
		this.categories = categories;
		this.colors = colors;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.item_product_in_products, parent, false);
		}
		ListItem item = items.get(position);
		CheckBox name = (CheckBox) view.findViewById(R.id.item_product_in_products_checkbox);
		name.setText(item.getName());
		name.setChecked(item.isSelected());
		if (categories.contains(item.getCategory())){
			int pos = categories.indexOf(item.getCategory());
			name.setBackgroundTintList(ColorStateList.valueOf(colors.get(pos)));
			//name.getBackground().setColorFilter(colors.get(pos), PorterDuff.Mode.MULTIPLY);
		}
		return view;
	}

}
