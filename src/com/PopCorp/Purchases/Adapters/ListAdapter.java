package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.PopCorp.Purchases.Activities.MainActivity;
import com.PopCorp.Purchases.Comparators.ListComparator;
import com.PopCorp.Purchases.Controllers.ListController;
import com.PopCorp.Purchases.Data.List;
import com.PopCorp.Purchases.Data.ListItem;
import com.PopCorp.Purchases.DataBase.DB;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements Filterable{

	private MainActivity context;
	private ArrayList<ListItem> items;
	private SortedList<ListItem> publishItems;
	private ArrayList<ListItem> selectedItems = new ArrayList<ListItem>();
	private ListController controller;
	private HashMap<String, Integer> categories;

	private String currency;
	private RecyclerView listView;
	private ActionMode actionMode;
	private SharedPreferences sPref;

	public ListAdapter(MainActivity ctx, ArrayList<ListItem> items, ListController controller, String currency, RecyclerView listView){
		super();
		this.context = ctx;
		this.items = items;
		publishItems = new SortedList<ListItem>(ListItem.class, new SortedList.Callback<ListItem>() {
			@Override
			public boolean areContentsTheSame(ListItem oneItem, ListItem twoItem) {
				if (oneItem.getFields().equals(twoItem.getFields())){
					return true;
				}
				return false;
			}

			@Override
			public boolean areItemsTheSame(ListItem oneItem, ListItem twoItem) {
				if (oneItem==twoItem){
					return true;
				}
				return false;
			}

			@Override
			public int compare(ListItem oneItem, ListItem twoItem) {
				return new ListComparator(context).compare(oneItem, twoItem);
			}

			@Override
			public void onChanged(int position, int count) {
				notifyItemRangeChanged(position, count);
			}

			@Override
			public void onInserted(int position, int count) {
				notifyItemRangeInserted(position, count);
			}

			@Override
			public void onMoved(int fromPosition, int toPosition) {
				notifyItemMoved(fromPosition, toPosition);
			}

			@Override
			public void onRemoved(int position, int count) {
				notifyItemRangeRemoved(position, count);
			}
		});
		this.controller = controller;
		this.currency = currency;
		this.listView = listView;
		sPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		categories = getCategories();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
		public ViewGroup view;
		public TextView name;
		public TextView count;
		public TextView edizm;
		public TextView coast;
		public TextView shop;
		public TextView comment;
		public TextView totalOne;
		public TextView totalTwo;
		public TextView po;
		public ImageView important;
		public ImageView color;
		private ClickListener clickListener;

		public ViewHolder(ViewGroup view) {
			super(view);
			this.view = view;
			name = (TextView) view.findViewById(R.id.item_listitem_name);
			count = (TextView) view.findViewById(R.id.item_listitem_count);
			edizm = (TextView) view.findViewById(R.id.item_listitem_edizm);
			coast = (TextView) view.findViewById(R.id.item_listitem_coast);
			shop = (TextView) view.findViewById(R.id.item_listitem_shop);
			comment = (TextView) view.findViewById(R.id.item_listitem_comment);
			totalOne = (TextView) view.findViewById(R.id.item_listitem_total_one);
			totalTwo = (TextView) view.findViewById(R.id.item_listitem_total_two);
			po = (TextView) view.findViewById(R.id.item_listitem_po);
			color = (ImageView) view.findViewById(R.id.item_listitem_image_color);
			important = (ImageView) view.findViewById(R.id.item_listitem_image_important);
			view.getChildAt(0).setOnClickListener(this);
			view.getChildAt(0).setOnLongClickListener(this);
		}

		public interface ClickListener {
			public void onClick(View v, int position, boolean isLongClick);
		}

		public void setClickListener(ClickListener clickListener) {
			this.clickListener = clickListener;
		}

		@Override
		public boolean onLongClick(View v) {
			clickListener.onClick(v, getAdapterPosition(), true);
			return true;
		}

		@Override
		public void onClick(View v) {
			clickListener.onClick(v, getAdapterPosition(), false);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ListItem getItem(int position){
		return publishItems.get(position);
	}

	@Override
	public int getItemCount() {
		return publishItems.size();
	}

	ActionMode.Callback callback = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode currentActionMode, Menu menu) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode currentActionMode) {
			actionMode = null;
			controller.showActionButton();
			selectedItems.clear();
			notifyItemRangeChanged(0, publishItems.size()-1);
		}

		@Override
		public boolean onCreateActionMode(ActionMode currentActionMode, Menu menu) {
			actionMode = currentActionMode;
			currentActionMode.getMenuInflater().inflate(R.menu.popup_menu_for_item, menu);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode currentActionMode, MenuItem item) {
			switch (item.getItemId()){
			case R.id.action_edit_item : {
				controller.startEditingItem(selectedItems.get(0));
				break;
			}
			case R.id.action_remove_item : {
				controller.removeItems(selectedItems);
				break;
			}
			case R.id.action_send_as_sms : {
				controller.sendItems(List.TYPE_OF_SENDING_LIST_TO_SMS, selectedItems);
				break;
			}
			case R.id.action_send_as_email : {
				controller.sendItems(List.TYPE_OF_SENDING_LIST_TO_EMAIL, selectedItems);
				break;
			}
			case R.id.action_send_as_text : {
				controller.sendItems(List.TYPE_OF_SENDING_LIST_AS_TEXT, selectedItems);
				break;
			}
			default : {
				return true;
			}
			}
			actionMode.finish();
			return false;
		}
	};

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		ListItem item = publishItems.get(position);

		holder.name.setText(item.getName());

		holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.valueOf(sPref.getString(SD.PREFS_LIST_ITEM_FONT_SIZE, context.getString(R.string.prefs_default_size_text))));
		Float smallTextSize = Float.valueOf(sPref.getString(SD.PREFS_LIST_ITEM_FONT_SIZE_SMALL, context.getString(R.string.prefs_default_size_text_small)));

		holder.count.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
		holder.edizm.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
		holder.coast.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
		holder.shop.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
		holder.comment.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
		holder.totalTwo.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
		holder.totalOne.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
		holder.po.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);

		holder.count.setText(item.getCountInString());
		holder.edizm.setText(item.getEdizm());
		holder.coast.setText(item.getCoastInString() + " " + currency);

		String total = item.getCount().multiply(item.getCoast()).toString();
		if (item.getComment().isEmpty()){
			holder.shop.setVisibility(View.GONE);

			if (item.getShop().isEmpty()){
				holder.comment.setVisibility(View.GONE);
				holder.totalTwo.setVisibility(View.GONE);
				holder.totalOne.setVisibility(View.VISIBLE);
				holder.totalOne.setText(total + " " + currency);
			} else{
				holder.comment.setVisibility(View.VISIBLE);
				holder.totalTwo.setVisibility(View.VISIBLE);
				holder.totalOne.setVisibility(View.GONE);
				holder.comment.setText(context.getString(R.string.string_in) + " " + item.getShop());
				holder.totalTwo.setText(total + " " + currency);
			}
		} else{
			holder.comment.setVisibility(View.VISIBLE);
			holder.comment.setText(item.getComment());
			holder.totalTwo.setVisibility(View.VISIBLE);
			holder.totalOne.setVisibility(View.GONE);
			holder.totalTwo.setText(total + " " + currency);

			if (item.getShop().isEmpty()){
				holder.shop.setVisibility(View.GONE);
			} else{
				holder.shop.setVisibility(View.VISIBLE);
				holder.shop.setText(context.getString(R.string.string_in) + " " + item.getShop());
			}
		}

		if (item.isImportant()){
			holder.important.setVisibility(View.VISIBLE);
		} else {
			holder.important.setVisibility(View.GONE);
		}

		holder.setClickListener(new ViewHolder.ClickListener() {
			@Override
			public void onClick(View view, int position, boolean isLongClick) {
				try{
					if (isLongClick) {
						if (actionMode!=null){
							changeItemInActionMode(position);
						} else{
							listView.startActionMode(callback);
							controller.hideActionButton();
							selectedItems.add(publishItems.get(position));
							actionMode.setTitle(String.valueOf(selectedItems.size()));
							notifyItemChanged(position);
						}
					} else {
						if (actionMode!=null){
							changeItemInActionMode(position);
						} else{
							controller.changeItemBuyed(position, publishItems.get(position));
						}
					}
				} catch(Exception e){

				}
			}

			private void changeItemInActionMode(int position) {
				if (selectedItems.contains(publishItems.get(position))){
					selectedItems.remove(publishItems.get(position));
				} else{
					selectedItems.add(publishItems.get(position));
				}
				notifyItemChanged(position);
				if (selectedItems.size()==0){
					actionMode.finish();
					actionMode = null;
					return;
				}
				actionMode.setTitle(String.valueOf(selectedItems.size()));
				if (selectedItems.size()==1){
					actionMode.getMenu().findItem(R.id.action_edit_item).setVisible(true);
				} else{
					actionMode.getMenu().findItem(R.id.action_edit_item).setVisible(false);
				}
				
			}
		});
		if (item.isBuyed()){
			holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG| Paint.FAKE_BOLD_TEXT_FLAG);
			((ViewGroup) holder.view.getChildAt(0)).getChildAt(1).setAlpha(0.3f);
			holder.color.setAlpha(0.3f);
		} else {
			holder.name.setPaintFlags(Paint.LINEAR_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
			((ViewGroup) holder.view.getChildAt(0)).getChildAt(1).setAlpha(1f);
			holder.color.setAlpha(1f);
		}

		if (sPref.getBoolean(SD.PREFS_SHOW_CATEGORIES, true)){
			holder.color.setVisibility(View.VISIBLE);
			if (selectedItems.contains(item)){
				holder.color.setImageResource(R.drawable.ic_done_white_24dp);
			} else{
				holder.color.setImageResource(android.R.color.transparent);
			}
			if (categories.containsKey(item.getCategory())){
				holder.color.setBackgroundColor(categories.get(item.getCategory()));
			} else{
				holder.color.setBackgroundColor(context.getResources().getColor(R.color.md_blue_grey_500));
			}
		} else{
			holder.color.setVisibility(View.GONE);
			if (selectedItems.contains(item)){
				((ViewGroup) holder.view).getChildAt(0).setActivated(true);
			} else{
				((ViewGroup) holder.view).getChildAt(0).setActivated(false);
			}
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listitem, parent, false);

		ViewHolder viewHolder = new ViewHolder((ViewGroup) v);
		return viewHolder;
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				ArrayList<ListItem> newItems = (ArrayList<ListItem>) results.values;
				ArrayList<ListItem> itemsForRemove = new ArrayList<ListItem>();
				for (int i=0; i<publishItems.size(); i++){
					if (!newItems.contains(publishItems.get(i))){
						itemsForRemove.add(publishItems.get(i));
					}
				}
				for (ListItem item : itemsForRemove){
					publishItems.remove(item);
				}
				for (ListItem item : newItems){
					if (publishItems.indexOf(item)==-1){
						publishItems.add(item);
					}
				}
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				ArrayList<ListItem> FilteredArrayNames = new ArrayList<ListItem>();

				if (constraint.equals("") || constraint.equals(context.getString(R.string.string_all_shops))){
					results.count = items.size();
					results.values = items;
					return results;
				}

				for (int i = 0; i < items.size(); i++) {
					ListItem item = items.get(i);
					if (item.getShop().equals(constraint)){
						FilteredArrayNames.add(item);
					}
				}

				results.count = FilteredArrayNames.size();
				results.values = FilteredArrayNames;
				return results;
			}
		};

		return filter;
	}

	public ActionMode getActionMode() {
		return actionMode;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public SortedList<ListItem> getPublishItems() {
		return publishItems;
	}

	public HashMap<String, Integer> getCategories() {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		DB db = new DB(context);
		db.open();
		Cursor cursor = db.getAllData(DB.TABLE_CATEGORIES);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				result.put(cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGS_NAME)), cursor.getInt(cursor.getColumnIndex(DB.KEY_CATEGS_COLOR)));
				while (cursor.moveToNext()){
					result.put(cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGS_NAME)), cursor.getInt(cursor.getColumnIndex(DB.KEY_CATEGS_COLOR)));
				}
			}
			cursor.close();
		}
		return result;
	}
}
