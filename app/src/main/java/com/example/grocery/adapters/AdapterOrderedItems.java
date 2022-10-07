package com.example.grocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelCart;
import com.example.grocery.models.ModelOrderedItems;

import java.util.ArrayList;

public class AdapterOrderedItems extends RecyclerView.Adapter<AdapterOrderedItems.HolderOrderedItems>{

    //view of row_ordered items
    private ArrayList<ModelOrderedItems> orderedItemList;
    private Context context;


    public AdapterOrderedItems(ArrayList<ModelOrderedItems> orderedItemList, Context context) {
        this.orderedItemList = orderedItemList;
        this.context = context;
    }


    @NonNull
    @Override
    public HolderOrderedItems onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditem,parent,false);
        return new HolderOrderedItems(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItems holder, int position) {
        //get data at position
        ModelOrderedItems modelOrderedItems =orderedItemList.get(position);
        String pId = modelOrderedItems.getpId();
        String name = modelOrderedItems.getName();
        String cost = modelOrderedItems.getCost();
        String price = modelOrderedItems.getPrice();
        String quantity = modelOrderedItems.getQuantity();

        //set data
        holder.itemTitle.setText(name);
        holder.itemPriceEachT.setText("Rs."+price);
        holder.itemPrice.setText("Rs."+cost);
        holder.iteQuantity.setText("["+quantity+"]");
    }

    @Override
    public int getItemCount() {
        return orderedItemList.size();
    }

    //view holder class
    class HolderOrderedItems extends RecyclerView.ViewHolder{

        private TextView itemTitle,itemPrice,itemPriceEachT,iteQuantity;
        public HolderOrderedItems(@NonNull View itemView) {
            super(itemView);

            itemTitle = itemView.findViewById(R.id.itemtitle);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemPriceEachT = itemView.findViewById(R.id.itemPriceEachT);
            iteQuantity = itemView.findViewById(R.id.iteQuantity);
        }
    }
}
