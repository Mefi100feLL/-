package com.PopCorp.Purchases.Comparators;

import java.util.Comparator;

import com.PopCorp.Purchases.Data.List;

public class MenuComparator implements Comparator<List>{
	@Override
	public int compare(List oneList, List twoList) {
		return oneList.getName().compareToIgnoreCase(twoList.getName());
	}
}