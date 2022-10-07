package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderedItems;
import com.example.grocery.models.ModelOrderedItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    private String orderTo,orderId;
    private ImageButton backB,writeReview;
    private TextView orderidTv,datetvv,orderstatusTv,shopnamee,totalItemsTv,amountt;
    private TextView deliveryaddress;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelOrderedItems> orderedItemsArrayList;
    private AdapterOrderedItems adapterOrderedItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        //init views
        backB = findViewById(R.id.backB);
        orderidTv = findViewById(R.id.orderidTv);
        datetvv = findViewById(R.id.datetvv);
        orderstatusTv = findViewById(R.id.orderstatusTv);
        shopnamee = findViewById(R.id.shopnamee);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountt = findViewById(R.id.amountt);
        deliveryaddress = findViewById(R.id.deliveryaddress);
        itemsRv = findViewById(R.id.itemsRv);
        writeReview = findViewById(R.id.writeReview);

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");
        orderId = intent.getStringExtra("orderId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();

        backB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //start review button click event
        writeReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(OrderDetailsActivity.this,WriteReviewActivity.class);
                intent1.putExtra("ShopUid",orderTo);
                startActivity(intent1);
            }
        });

    }

    private void loadOrderedItems() {
        //init lists
        orderedItemsArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemsArrayList.clear();  //before loading items clear list
                        for (DataSnapshot ds:snapshot.getChildren())
                        {
                            ModelOrderedItems modelOrderedItems = ds.getValue(ModelOrderedItems.class);
                            //add to list
                            orderedItemsArrayList.add(modelOrderedItems);
                        }

                        //all items added to list
                        //setup adapter

                        adapterOrderedItems = new AdapterOrderedItems(orderedItemsArrayList,OrderDetailsActivity.this);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderedItems);

                        //set item count
                        amountt.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load order details
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderBy").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("DeliveryFee").getValue();
                        String latitude = ""+snapshot.child("Latitude").getValue();
                        String longitude = ""+snapshot.child("Longitude").getValue();

                        //convert timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatDate  = DateFormat.format("dd/MM/yyyy hh:mm: a",calendar).toString(); //e.g 26/09/2022 12:45 PM

                        if(orderStatus.equals("In Progress")){
                           orderstatusTv.setTextColor(getResources().getColor(R.color.purple_500));
                        }
                        else if(orderStatus.equals("Completed")){
                            orderstatusTv.setTextColor(getResources().getColor(R.color.green));
                        }
                        else if(orderStatus.equals("Cancelled")){
                            orderstatusTv.setTextColor(getResources().getColor(R.color.red));
                        }

                        //set data
                        orderidTv.setText(orderId);
                        orderstatusTv.setText(orderStatus);
                        amountt.setText("Rs."+orderCost+"[inluding taxes Rs. "+deliveryFee+"]");
                        datetvv.setText(formatDate);
//                        findAddress(latitude,longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo() {
        //get shop info

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("ShopName").getValue();
                        shopnamee.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

//    private void findAddress(String latitude, String longitude) {
//        double lat = Double.parseDouble(latitude);
//        double lon = Double.parseDouble(longitude);
//
//        //find address, country, state, city
//        Geocoder geocoder;
//        List<Address> addresses;
//        geocoder = new Geocoder(this, Locale.getDefault());
//
//        try {
//            addresses = geocoder.getFromLocation(lat,lon,1);
//            String address = addresses.get(0).getAddressLine(0); //complete address
//            deliveryaddress.setText(address);
//        }
//        catch (Exception e){
//
//        }
//    }
}