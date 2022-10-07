package com.example.grocery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelCart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCart> cartItems;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    public AdapterCartItem(Context context, ArrayList<ModelCart> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cart items
        View view = LayoutInflater.from(context).inflate(R.layout.row_cart_item,parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, @SuppressLint("RecyclerView") int position) {
        final String timestamp = ""+System.currentTimeMillis();

        //get item
        ModelCart modelCart = cartItems.get(position);
        String id = modelCart.getId();
        String pId = modelCart.getpId();
        String title = modelCart.getName();
        String cost = modelCart.getCost();
        String price = modelCart.getPrice();
        String quantity = modelCart.getQuantity();


        //set item

        holder.itemTitleTv.setText(""+title);
        holder.itemPriceEach.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]"); //e.g. [3]
        holder.itemPriceEach.setText(""+price);

        //handle remove click listener , delete items from cart
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //will create table if not exists, but in that case it will must exists
                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Removed from cart...", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble(((ShopDetailsActivity)context).totalTV.getText().toString().trim().replace("Rs.",""));
                double totalPrice = tx-Double.parseDouble(cost.replace("Rs.",""));
//                double deliveryFee = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("Rs.",""));
                double sTotalPrice = Double.parseDouble(String.format("%.2f",totalPrice));
                ((ShopDetailsActivity)context).allTotalPrice = 0.00;
                ((ShopDetailsActivity)context).sTotalTv.setText("Rs."+String.format("%.2f",sTotalPrice));
                ((ShopDetailsActivity)context).totalTV.setText("Rs."+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));

                //after removing item from cart, update cart
                ((ShopDetailsActivity)context).cartCounts();
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size(); //return number of records
    }

    //view holder class
    class HolderCartItem extends RecyclerView.ViewHolder{

        //ui views of row_cart

        private TextView itemTitleTv,itemPriceTv,itemPriceEach,itemQuantityTv
                ,itemRemoveTv;

        public HolderCartItem(@NonNull View itemView){
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEach = itemView.findViewById(R.id.itemPriceEach);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);



        }
    }
}
