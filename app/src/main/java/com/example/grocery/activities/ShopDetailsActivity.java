package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.grocery.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterCartItem;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelCart;
import com.example.grocery.models.ModelProduct;
import com.example.grocery.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare ui views
    private ImageView shopIv;
    private TextView shopNameTV,phoneTV,emailTV,openCloseTV,deliveryFeeTv,addressTV,filteredProducts,cartCount;
    private ImageButton callBtn,mapBtn,backBtn,filterBtn,cartBtn,reviewBtn;
    private EditText searchBox;
    private RecyclerView productsRV;
    private RatingBar ratingsba;

    //progrress dialog
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private String shopUid,userId;
    private String myLatitude,myLongitude,myPhone;
    private String shopLatitude,shopLongitude,shopname,shopEmail,shopPhone;
    public String deliveryFee;
    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCart> cartItemList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopIv = findViewById(R.id.shopIV);
        shopNameTV = findViewById(R.id.shopNameTVV);
        phoneTV = findViewById(R.id.phoneTVV);
        emailTV = findViewById(R.id.emailTVV);
        openCloseTV = findViewById(R.id.openCloseTV);
//        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
//        addressTV = findViewById(R.id.addressTVV);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartaddingBtn);
        cartCount = findViewById(R.id.cartCount);
        ratingsba = findViewById(R.id.ratingsba);

        backBtn = findViewById(R.id.backBtnnn);
        searchBox = findViewById(R.id.serachProductEttt);
        filterBtn = findViewById(R.id.filterBtnn);
        filteredProducts = findViewById(R.id.filteredProducts);
        productsRV = findViewById(R.id.prdouctsRVV);
        reviewBtn = findViewById(R.id.reviewBtn);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //getting uid of the shop from intent
        shopUid = getIntent().getStringExtra("ShopUid");


        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getUid();
        loadMyInfo();
        loadProducts();
        loadShopDetails();
        loadReviews();

         easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        //each shop has its own products and orders so if the user add items to cart and go back and open cart in different shop then cart should be different
        //so delete cart data whenever user open this activity
//        deleteCartItems();
        cartCounts();


        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCartDialog();
            }
        });
        //when back btn is pressed
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //when logout btn is clicked

        //when call btn is clicked
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose category:")
                        .setItems(Constants.options1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.options1[which];
                                filteredProducts.setText(selected);
                                if(selected.equals("All")){
                                    //load all
                                    loadProducts();
                                }
                                else{
                                    //load filtered products
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });

        //when clicked on review button
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopDetailsActivity.this,ShopReviewActivity.class);
                intent.putExtra("ShopUid",shopUid);
                startActivity(intent);
            }
        });


    }

    private float ratingSum = 0;
    private void loadReviews() {

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

                        ratingsba.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartItems(){
        //declare it to class level and init on create
        easyDB.deleteAllDataFromTable();  //delete all records from cart
    }
    public void cartCounts(){
        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if(count<=0){
            //no item in cart, hide cartcount view
            cartCount.setVisibility(View.GONE);
        }
        else{
            //there is item in cart show it
            cartCount.setVisibility(View.VISIBLE);
            cartCount.setText(""+count); //concatenate because we cannot set integer int textView
        }
    }

    public  double allTotalPrice=0.0;
    //need to access these views in adapter so making public
    public TextView sTotalTv,dFeeTV,totalTV;
    private void showCartDialog() {

        //init list
        cartItemList = new ArrayList<>();
        final String timestamp = ""+System.currentTimeMillis();


        //load inflater
        View view  = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init views
         TextView shopName = view.findViewById(R.id.shopnamea);
        RecyclerView cartItemsRV = view.findViewById(R.id.cartItemsRV);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        totalTV = view.findViewById(R.id.totalTV);
        Button checkOutBtn = view.findViewById(R.id.checkOutBtn);
         shopname = shopName.getText().toString();

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);
        shopName.setText(shopname);


        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCart modelCart = new ModelCart(""+id,""+pId,""+name,""+price,""+cost,""+quantity);
            cartItemList.add(modelCart);
        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this,cartItemList);
        //set to recyclerview
        cartItemsRV.setAdapter(adapterCartItem);

//        dFeeTV.setText("Rs. "+deliveryFee);
        sTotalTv.setText("Rs."+String.format("%.2f",allTotalPrice));
//        totalTV.setText("Rs."+(allTotalPrice+Double.parseDouble(deliveryFee.replace("Rs.",""))));

        //reset total price on dialog dismiss
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;

            }
        });

        //place order
        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if(myPhone.equals("") || myPhone.equals("null")){
                    Toast.makeText(ShopDetailsActivity.this, "Please update phone number in your profile...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartItemList.isEmpty()){
                    //cart is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart...", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
            }


        });
    }

    private void submitOrder() {
        //show dialog
        progressDialog.setTitle("Placing order...");
        progressDialog.show();

        //for order id and time of order
        String timestamp = ""+System.currentTimeMillis();

        String cost = totalTV.getText().toString().trim().replace("Rs.","");



        //setup order data
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress");  //inProgress,completed,cancelled
        hashMap.put("orderCost",cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("Latitude",""+myLatitude);
        hashMap.put("Longitude",""+myLongitude);

        //add to db
       final DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        db.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        for(int i=0;i<cartItemList.size();i++){
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String,String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("id",id);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            db.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed successfully...", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https//:maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr"+shopLatitude+","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get shop data
                String name = ""+snapshot.child("Name").getValue();
                shopname = ""+snapshot.child("ShopName").getValue();
                shopEmail = ""+snapshot.child("Email").getValue();
                shopPhone = ""+snapshot.child("Phone").getValue();
                shopLatitude  = ""+snapshot.child("Latitude").getValue();
                shopLongitude = ""+snapshot.child("Longitude").getValue();
//                deliveryFee = ""+snapshot.child("DeliveryFee").getValue();
                String profileImg = ""+snapshot.child("ProfileImg").getValue();
//                String shopAddress = ""+snapshot.child("Address").getValue();
                String shopOpen = ""+snapshot.child("ShopOpen").getValue();

                //set shop data

                shopNameTV.setText(shopname);
                emailTV.setText(shopEmail);
//                deliveryFeeTv.setText("Delivery Fee:Rs."+deliveryFee);
//                addressTV.setText(shopAddress);
                phoneTV.setText(shopPhone);
                if(shopOpen.equals("true")){
                    openCloseTV.setText("Open");
                }
                else{
                    openCloseTV.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImg).into(shopIv);
                }
                catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadProducts() {

        //init lists
        productsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        productsList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productsList);

                        //set adapter
                        productsRV.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMyInfo() {
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
                            myPhone = ""+ds.child("Phone").getValue();
                            String profileImg = ""+ds.child("ProfileImg").getValue();
                            String city = ""+ds.child("City").getValue();
                            myLatitude = ""+ds.child("Latitude").getValue();
                            myLongitude = ""+ds.child("Longitude").getValue();


                            //set user data


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void prepareNotificationMessage(String orderId){
        //when user places order send notification to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topic/" + Constants.FCM_TOPIC;  //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order"+orderId;
        String NOTIFICATION_MESSAGE = "Congratulations!!! There is a new order";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json what to send and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid()); //since we have logged in as user so the current user uid is buyer uid
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);  //to all who subscribed to this topic
            notificationJo.put("data",notificationBodyJo);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);  //get these values through intent in orderdetails activity
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed still start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);  //get these values through intent in orderdetails activity

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-type","applicatioin/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);
                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }
}