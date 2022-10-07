package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterOrderShop;
import com.example.grocery.R;
import com.example.grocery.activities.OrderDetailsSellterActivity;
import com.example.grocery.models.ModelOrderShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    public ArrayList<ModelOrderShop> modelOrderShopArrayList,fiterList;
    private Context context;
    private FilterOrderShop filterOrderShop;

    public AdapterOrderShop(ArrayList<ModelOrderShop> modelOrderShopArrayList, Context context) {
        this.modelOrderShopArrayList = modelOrderShopArrayList;
        this.context = context;
        this.fiterList = modelOrderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        //get data at position
        ModelOrderShop modelOrderShop = modelOrderShopArrayList.get(position);
        String orderId = modelOrderShop.getOrderId();
        String orderBy = modelOrderShop.getOrderBy();
        String orderCost = modelOrderShop.getOrderCost();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderTime = modelOrderShop.getOrderTime();
        String orderTo = modelOrderShop.getOrderTo();

        //load user/buyer info
        loadUserInfo(modelOrderShop, holder);

        //set data
        holder.amountTTv.setText("Amount: Rs."+orderCost);
        holder.orderIdt.setText("Order Id:"+orderId);
        holder.orderstatus.setText("Status:"+orderStatus);
        //change order status text color
        if(orderStatus.equals("In Progress")){
            holder.orderstatus.setTextColor(context.getResources().getColor(R.color.purple_500));

        }
        else if(orderStatus.equals("Completed")){
            holder.orderstatus.setTextColor(context.getResources().getColor(R.color.green));
        }
        else if(orderStatus.equals("Cancelled")){
            holder.orderstatus.setTextColor(context.getResources().getColor(R.color.red));
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.orderDateTv.setText(formatedDate);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details
                Intent intent = new Intent(context, OrderDetailsSellterActivity.class);
                intent.putExtra("orderId",orderId); //to load order info
                intent.putExtra("orderBy",orderBy);  //to load info of the user who placed order
                context.startActivity(intent);
            }
        });

    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, HolderOrderShop holder) {
        //to load email of buyer/user | modelOrdershop.getOrderBy() contains uid of buyer/user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("Email").getValue();
                        holder.emailId.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelOrderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filterOrderShop == null){
            //init filter
            filterOrderShop = new FilterOrderShop(this,fiterList);
        }
        return filterOrderShop;
    }

    //view holder class for row_order_seller.xml
    class HolderOrderShop extends RecyclerView.ViewHolder{

        //ui views of row_order_seller.xml
        private TextView orderIdt, orderDateTv, emailId, amountTTv,orderstatus;
        private ImageView nextT;
        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            orderIdt = itemView.findViewById(R.id.orderIdt);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailId = itemView.findViewById(R.id.emailId);
            amountTTv = itemView.findViewById(R.id.amountTTv);
            orderstatus = itemView.findViewById(R.id.orderstatus);
            nextT = itemView.findViewById(R.id.nextT);

        }
    }

}
