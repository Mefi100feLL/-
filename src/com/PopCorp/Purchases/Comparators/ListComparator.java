package com.PopCorp.Purchases.Comparators;

import java.util.Comparator;

import com.PopCorp.Purchases.Data.ListItem;

public class ListComparator implements Comparator<ListItem>{
	@Override
	public int compare(ListItem oneItem, ListItem twoItem) {
		return oneItem.getName().compareToIgnoreCase(twoItem.getName());
	}
}