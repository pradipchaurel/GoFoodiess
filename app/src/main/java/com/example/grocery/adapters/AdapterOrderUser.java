package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.OrderDetailsActivity;
import com.example.grocery.models.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser>{

    private Context context;
    private ArrayList<ModelOrderUser> orderUserArrayList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserArrayList) {
        this.context = context;
        this.orderUserArrayList = orderUserArrayList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {
        //get data
        ModelOrderUser modelOrderUser = orderUserArrayList.get(position);
        String orderId = modelOrderUser.getOrderId();
        String orderBy = modelOrderUser.getOrderBy();
        String orderCost = modelOrderUser.getOrderCost();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderTime = modelOrderUser.getOrderTime();
        String orderTo = modelOrderUser.getOrderTo();

        //get shop info
        loadShopInfo(modelOrderUser,holder);

        //set data
        holder.amountTv.setText("Amount Rs:"+orderCost);
        holder.statusTV.setText(""+orderStatus);
        holder.orderTv.setText("OrderID:"+orderId);
        if(orderStatus.equals("In Progress")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.teal_700));
        }
        else if(orderStatus.equals("Completed")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.green));
        }
        if(orderStatus.equals("Cancelled")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy",calendar).toString();
        holder.dateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details, we need to keys there, orderId , orderTo
                Intent intent = new Intent(context, OrderDetailsActivity.class);
                intent.putExtra("orderTo",orderTo);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent);  //get these values through intent in orderdetails activity
            }
        });
    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, HolderOrderUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("ShopName").getValue();
                        holder.shopTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserArrayList.size();
    }

    //view holder class
    class HolderOrderUser extends RecyclerView.ViewHolder{

        //views of layout
        private TextView orderTv, dateTv, shopTv, amountTv, statusTV;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            orderTv = itemView.findViewById(R.id.orderTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopTv = itemView.findViewById(R.id.shopTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTV = itemView.findViewById(R.id.statusTV);
        }
    }
}
