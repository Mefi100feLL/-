package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Activities.ProductsActivity;
import com.PopCorp.Purchases.Controllers.ProductsController;
import com.PopCorp.Purchases.Data.Product;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> implements Filterable{

	public static final String FILTER_TYPE_NAMES = "NAMES";
	public static final String FILTER_TYPE_CATEGORIES = "CATEGORIES";
	public static final String FILTER_TYPE_FAVORITE = "FAVORITE";

	private Context context;
	private ArrayList<Product> items;
	private ArrayList<Product> publishItems;
	private ArrayList<String> categories;
	private ArrayList<Integer> colors;
	private TextView textViewEmpty;
	private int textOfEmpty = R.string.string_list_of_products_clear;

	public ProductsAdapter(ProductsActivity context, ProductsController controller, ArrayList<Product> items, ArrayList<String> categories, ArrayList<Integer> colors){
		super();
		this.context = context;
		this.items = items;
		this.categories = categories;
		this.colors = colors;
		publishItems = new ArrayList<Product>();
		publishItems.addAll(items);
		textViewEmpty = (TextView) context.findViewById(R.id.activity_products_textview_empty);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		public View view;
		public CheckBox checkbox;
		private ClickListener clickListener;

		public ViewHolder(View view) {
			super(view);
			this.view = view;
			checkbox = (CheckBox) view.findViewById(R.id.item_product_in_products_checkbox);
			view.setOnClickListener(this);
		}

		public interface ClickListener {
			public void onClick(View v, int position);
		}

		public void setClickListener(ClickListener clickListener) {
			this.clickListener = clickListener;
		}

		@Override
		public void onClick(View v) {
			clickListener.onClick(v, getPosition());
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		if (publishItems.size()==0){
			textViewEmpty.setVisibility(View.VISIBLE);
		} else{
			textViewEmpty.setVisibility(View.GONE);
		}
		return publishItems.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Product item = publishItems.get(position);

		holder.checkbox.setText(item.getName());
		holder.checkbox.setChecked(item.isSelected());
		if (categories.contains(item.getCategory())){
			int pos = categories.indexOf(item.getCategory());
			Drawable drawableCheck = context.getResources().getDrawable(R.drawable.abc_btn_check_material);
			drawableCheck.setColorFilter(colors.get(pos), PorterDuff.Mode.SRC_IN);
			holder.checkbox.setButtonDrawable(drawableCheck);
		}
		holder.setClickListener(new ViewHolder.ClickListener() {
			@Override
			public void onClick(View view, int position) {
				boolean selected = publishItems.get(position).isSelected();
				publishItems.get(position).setSelected(!selected);
				((CheckBox) view.findViewById(R.id.item_product_in_products_checkbox)).setChecked(!selected);
			}
		});
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_in_products, parent, false);

		ViewHolder viewHolder = new ViewHolder(v);
		return viewHolder;
	}


	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				ArrayList<Product> newItems = (ArrayList<Product>) results.values;
				ListIterator<Product> iterator = publishItems.listIterator();
				while (iterator.hasNext()){
					Product item = iterator.next();
					if (!newItems.contains(item)){
						int position = publishItems.indexOf(item);
						iterator.remove();
						notifyItemRemoved(position);
					}
				}
				HashMap<Product, Integer> oldPositions = new HashMap<Product, Integer>();
				for (Product product : publishItems){
					oldPositions.put(product, publishItems.indexOf(product));
				}
				ArrayList<Product> tmpItems = new ArrayList<Product>(newItems);
				tmpItems.removeAll(publishItems);
				publishItems.addAll(tmpItems);

				if (constraint.equals(FILTER_TYPE_CATEGORIES)){
					Collections.sort(publishItems, new SortOnlyCategories());
				} else{
					Collections.sort(publishItems, new SortOnlyNames());
				}

				for (Product item : publishItems){
					int position = publishItems.indexOf(item);
					if (position!=-1){
						if (tmpItems.contains(item)){
							notifyItemInserted(position);
						} else{
							if (oldPositions.get(item)!=position){
								notifyItemMoved(oldPositions.get(item), position);
							}
						}
					}
				}
				textViewEmpty.setText(textOfEmpty);
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				ArrayList<Product> FilteredArrayNames = new ArrayList<Product>();

				if (!constraint.equals(FILTER_TYPE_FAVORITE)){
					results.count = items.size();
					results.values = items;
					textOfEmpty = R.string.string_list_of_products_clear;
					return results;
				}

				for (int i = 0; i < items.size(); i++) {
					Product item = items.get(i);
					if (item.isFavorite()){
						FilteredArrayNames.add(item);
					}
				}

				results.count = FilteredArrayNames.size();
				results.values = FilteredArrayNames;
				textOfEmpty = R.string.string_list_of_favorite_clear;
				return results;
			}
		};
		return filter;
	}

	private class SortOnlyNames implements Comparator<Product>{
		public int compare(Product p1, Product p2){
			return p1.getName().compareToIgnoreCase(p2.getName());
		}
	}

	private class SortOnlyCategories implements Comparator<Product>{
		public int compare(Product p1, Product p2){
			if ((p1.getCategory().equals(p2.getCategory()))){
				return p1.getName().compareToIgnoreCase(p2.getName());
			} else{
				if (categories.contains(p1.getCategory()) && categories.contains(p2.getCategory())){
					if (categories.indexOf(p1.getCategory()) > categories.indexOf(p2.getCategory())){
						return 1;
					}else{
						return -1;
					}
				} else if (categories.contains(p1.getCategory())){
					return 1;
				} else if (categories.contains(p2.getCategory())){
					return -1;
				} else{
					return p1.getCategory().compareToIgnoreCase(p2.getCategory());
				}
			}
		}
	}
}
