package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderShop;
import com.example.grocery.adapters.AdapterOrderUser;
import com.example.grocery.adapters.AdapterShop;
import com.example.grocery.models.ModelOrderShop;
import com.example.grocery.models.ModelOrderUser;
import com.example.grocery.models.ModelShops;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainUserActivity extends AppCompatActivity {

    private TextView userName,userPhone,userEmail,tabShopsTV,tabOrdersTV;
    private ImageButton userLogout,userEdit,settingBtn;
    private ImageView userProfile;
    private RelativeLayout shopsRL,ordersRL;
    private RecyclerView shopRV, orderRV;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelShops> shopList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> orderUserArrayList;
    private AdapterOrderUser adapterOrderUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_main_user);

        userName = (TextView) findViewById(R.id.username);
        userLogout = (ImageButton) findViewById(R.id.userLogout);
        userEdit = (ImageButton) findViewById(R.id.userEdit);
        userPhone = (TextView) findViewById(R.id.userPhone);
        userEmail = (TextView) findViewById(R.id.useremailid);
        tabShopsTV = (TextView) findViewById(R.id.tabShopsTv);
        tabOrdersTV = (TextView) findViewById(R.id.usertabOrdersTv);
        userProfile = (ImageView) findViewById(R.id.userProfileIV);
        shopsRL = (RelativeLayout) findViewById(R.id.shopsRL);
        ordersRL = (RelativeLayout) findViewById(R.id.ordersRL);
        shopRV = (RecyclerView) findViewById(R.id.shopRV);
        orderRV = (RecyclerView) findViewById(R.id.orderRv);
        settingBtn = (ImageButton) findViewById(R.id.usersettingBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //at start show ui of shop
        showShopsUI();


        userLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        userEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, ProfileUserEdit.class));
            }
        });

        //when clicked on shop tab
        tabShopsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shows shop ui
//                showShopsUI();
                showOrdersUI();
            }
        });

        //when clicked on orders tab
        tabOrdersTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shows orders ui
//                showOrdersUI();
                showShopsUI();
            }
        });

        //when clicked on setting button
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this,SettingActivity.class));
            }
        });
    }

    private void showShopsUI() {
        //show shops ui and hide orders ui
        shopsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);
        tabShopsTV.setTextColor(getResources().getColor(R.color.black));
        tabShopsTV.setBackgroundColor(R.drawable.shape_rect04);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTV.setBackgroundColor(getResources().getColor(R.color.red));
    }
    private void showOrdersUI() {
        //show shops ui and hide orders ui
        shopsRL.setVisibility(View.GONE);
        ordersRL.setVisibility(View.VISIBLE);
        tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTV.setBackgroundColor(R.drawable.shape_rect04);

        tabShopsTV.setTextColor(getResources().getColor(R.color.white));
        tabShopsTV.setBackgroundColor(getResources().getColor(R.color.red));
    }



    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadInfo();
        }
    }

    private void loadInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            //get user data
                            String name = ""+ds.child("Name").getValue();
                            String accountType = ""+ds.child("AccountType").getValue();
                            String email = ""+ds.child("Email").getValue();
                            String phone = ""+ds.child("Phone").getValue();
                            String profileImg = ""+ds.child("ProfileImg").getValue();
                            String city = ""+ds.child("City").getValue();

                            //set user data

                            userName.setText("Welcome, "+name);
                            userEmail.setText(email);
                            userPhone.setText(phone);

                            try {
                                Picasso.get().load(profileImg).placeholder(R.drawable.ic_person).into(userProfile);
                            }
                            catch (Exception e){
                                userProfile.setImageResource(R.drawable.ic_person);

                            }

                            //load only those shops those are in city of users
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        //init order list
        orderUserArrayList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderUserArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    reference.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            orderUserArrayList.add(modelOrderUser);
                                        }
                                        //setup adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this,orderUserArrayList);
                                        //set to recyclerview
                                        orderRV.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String city) {

        //init shoplist
        shopList = new ArrayList<>();


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("AccountType").equalTo("Seller").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before editing
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelShops modelShops = ds.getValue(ModelShops.class);
                            String shopCity = ""+ds.child("City").getValue();

                            //shows only user city shops
                           /* if(shopCity.equals(city)){
                                shopList.add(modelShops);
                            }*/
                            shopList.add(modelShops);
                        }
                        //set up adapter
                        adapterShop = new AdapterShop(MainUserActivity.this,shopList);

                        //set adapter to recyclerview
                        shopRV.setAdapter(adapterShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}