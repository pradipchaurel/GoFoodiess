package com.example.grocery;

import android.widget.Filter;

import com.example.grocery.adapters.AdapterOrderShop;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.models.ModelOrderShop;
import com.example.grocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterlist;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterlist) {
        this.adapter = adapter;
        this.filterlist = filterlist;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results  = new FilterResults();
        //validate data for search query
        if(constraint!=null && constraint.length()>0){
            //search filled not empty, searching something, perform search

            //change to upper case to make it case insensitive
            constraint = constraint.toString().toUpperCase();

            //store out filtered list
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for(int i=0;i<filterlist.size();i++){
                //check, search by title and catefory
                if(filterlist.get(i).getOrderStatus().toUpperCase().contains(constraint)){

                    //add filteredModels
                    filteredModels.add(filterlist.get(i));

                }

            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            //search filled empty, not searching ,return original/all/complete list

            results.count = filterlist.size();
            results.values = filterlist;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.modelOrderShopArrayList = (ArrayList<ModelOrderShop>)results.values;

        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
