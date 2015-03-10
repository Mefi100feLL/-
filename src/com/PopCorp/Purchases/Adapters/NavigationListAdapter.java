package com.PopCorp.Purchases.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;

public class NavigationListAdapter extends BaseAdapter{

	private Context context;
	private String[] items;
	private int selected = -1;
	private int[] icons = new int[] {R.drawable.ic_sale, R.drawable.ic_dashboard, R.drawable.ic_pin_drop, R.drawable.ic_settings};
	
	public NavigationListAdapter(Context context){
		super();
		this.context = context;
		items = context.getResources().getStringArray(R.array.navigation_menu_items);
	}
	
	public void setSelected(int position){
		selected = position;
	}
	
	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public Object getItem(int position) {
		return items[position];
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
			view = inflater.inflate(R.layout.item_navigation_list, parent, false);
		}
		
		if (selected==position){
			view.setBackgroundResource(R.drawable.abc_list_pressed_holo_light);
		} else {
			view.setBackgroundResource(R.drawable.abc_list_selector_holo_light);
		}
		
		TextView text = (TextView) view.findViewById(R.id.item_navigation_list_text);
		text.setText(items[position]);
		
		ImageView image = (ImageView) view.findViewById(R.id.item_navigation_list_image);
		image.setImageResource(icons[position]);
		return view;
	}

}
