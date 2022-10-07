package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelShops;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop>{

    private Context context;
    public ArrayList<ModelShops> shopsArrayList;

    public AdapterShop(Context context,ArrayList<ModelShops> shopsArrayList){
        this.context = context;
        this.shopsArrayList = shopsArrayList;
    }

    public AdapterShop(@NonNull View itemView, Context context, ArrayList<ModelShops> shopsArrayList) {
        this.context = context;
        this.shopsArrayList = shopsArrayList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //get data
        ModelShops modelShops = shopsArrayList.get(position);
        String accountType = modelShops.getAccountType();
        String country = modelShops.getCountry();
        String address = modelShops.getAddress();
        String city = modelShops.getCity();
        String deliveryFee = modelShops.getDeliveryFee();
        String email = modelShops.getEmail();
        String latitude = modelShops.getLatitude();
        String longitude = modelShops.getLongitude();
        String online = modelShops.getOnline();
        String name = modelShops.getName();
        String phone = modelShops.getPhone();
        String uid = modelShops.getUid();
        String timestamp = modelShops.getTimestamp();
        String shopOpen = modelShops.getShopOpen();
        String state = modelShops.getState();
        String profileImg = modelShops.getProfileImg();
        String shopName = modelShops.getShopName();

        loadReviews(modelShops,holder);


        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTV.setText(phone);
//        holder.addressTV.setText(address);
        if(online.equals("true")){
            //shop owner is online
            holder.onlineIV.setVisibility(View.VISIBLE);
        }
        else {
            //shop owner is offline
            holder.onlineIV.setVisibility(View.GONE);
        }
        //check if shop is open
        if(shopOpen.equals("true")){
            //shop is opened
            holder.shopClosedTV.setVisibility(View.GONE);
        }
        else {
            //shop is closed
            holder.shopClosedTV.setVisibility(View.VISIBLE);
        }
        try {
            Picasso.get().load(profileImg).placeholder(R.drawable.ic_store).into(holder.shopIV);
        }
        catch (Exception e){

            holder.shopIV.setImageResource(R.drawable.ic_store);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("ShopUid",uid);
                context.startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews(ModelShops modelShops, HolderShop holder) {

        String shopUid = modelShops.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it

                        ratingSum = 0;
                        for(DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum+=rating;
                        }
                        long numberOfReviewsCount = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviewsCount;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    @Override
    public int getItemCount() {
        return shopsArrayList.size();  //return number of records
    }

    //view holder
    class HolderShop extends RecyclerView.ViewHolder{


        //ui views of row_shop.xml
        private ImageView shopIV,onlineIV;
        private TextView shopClosedTV,shopNameTv,phoneTV,addressTV;
        private RatingBar ratingBar;



        public HolderShop(@NonNull View itemView){
            super(itemView);

            //init uid views
            shopIV = itemView.findViewById(R.id.shopIV);
            onlineIV = itemView.findViewById(R.id.onlineIV);
            shopClosedTV = itemView.findViewById(R.id.shopClosedTV);
            shopNameTv = itemView.findViewById(R.id.shopNameTV);
            phoneTV = itemView.findViewById(R.id.phoneTV);
//            addressTV = itemView.findViewById(R.id.addressTV);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }

}
