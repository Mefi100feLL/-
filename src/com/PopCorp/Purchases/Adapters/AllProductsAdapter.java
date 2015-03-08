package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Data.Product;

public class AllProductsAdapter extends BaseAdapter{

	private Context context;
	private ArrayList<Product> items;
	
	public AllProductsAdapter(Context context, ArrayList<Product> items){
		super();
		this.context = context;
		this.items = items;
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
			view = inflater.inflate(R.layout.item_product_in_all_products, parent, false);
		}
		Product item = items.get(position);
		TextView name = (TextView) view.findViewById(R.id.item_product_in_all_products_textview_name);
		name.setText(item.getName());
		
		return view;
	}

}
