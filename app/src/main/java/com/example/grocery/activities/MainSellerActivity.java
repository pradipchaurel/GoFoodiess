package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.grocery.adapters.AdapterOrderShop;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.Constants;
import com.example.grocery.models.ModelOrderShop;
import com.example.grocery.models.ModelProduct;
import com.example.grocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainSellerActivity extends AppCompatActivity {

    private TextView sellerName,shopName,sellerEmail,tabProductTv,tabOrderTv,filteredProductTv;
    private TextView filteredOrderTv;
    private EditText searchProductEt;
    private ImageButton filterProductBtn, filterOrderBtn;
    private ImageButton sellerLogout,editBtn,addProduct,reviewsBtn,settingBtn;
    private RelativeLayout productsRL,ordersRL;
    private RecyclerView productsRV, ordersRv;
    private ImageView profilPic;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> modelOrderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_main_seller);

        sellerName = (TextView) findViewById(R.id.sellername);
        shopName = (TextView) findViewById(R.id.shopname) ;
        sellerEmail = (TextView) findViewById(R.id.selleremailid);
        sellerLogout = (ImageButton) findViewById(R.id.selLogout);
        editBtn = (ImageButton) findViewById(R.id.sellerEdit);
        searchProductEt = (EditText) findViewById(R.id.searchProductEt);
        filterProductBtn = (ImageButton) findViewById(R.id.filterProductBtn);
        filteredProductTv = (TextView) findViewById(R.id.filteredProductTv);
        productsRV = (RecyclerView) findViewById(R.id.productsRV);
        tabProductTv = (TextView) findViewById(R.id.tabProductTv);
        tabOrderTv = (TextView) findViewById(R.id.tabOrdersTv);
        addProduct = (ImageButton) findViewById(R.id.sellAdd);
        profilPic = (ImageView) findViewById(R.id.sellerProfileIV);
        productsRL = (RelativeLayout) findViewById(R.id.productsRL);
        ordersRL = (RelativeLayout) findViewById(R.id.ordersRL);
        filteredOrderTv = (TextView) findViewById(R.id.filteredOrderTv);
        filterOrderBtn = (ImageButton) findViewById(R.id.filterOrderBtn);
        ordersRv = (RecyclerView) findViewById(R.id.ordersRv);
        reviewsBtn = (ImageButton) findViewById(R.id.reviewsBtn);
        settingBtn = (ImageButton) findViewById(R.id.settingBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrderes();
        showProductUI();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterProductSeller.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this, ProfileSellerEdit.class));
            }
        });

        sellerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open add product activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));

            }
        });

        tabProductTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load product view
//                showProductUI();
                showOrdersUI();
            }
        });

        tabOrderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders view
//                showOrdersUI();
                showProductUI();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose category:")
                        .setItems(Constants.options1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.options1[which];
                                filteredProductTv.setText(selected);
                                if(selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }
                                else{
                                    //load filtered products
                                    loadSelectedProducts(selected);
                                }
                            }
                        }).show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //options to display in dialog
                String[] options = {"All","In Progress","Completed","Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if(which==0){
                                    //when clicked on all
                                    filteredProductTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter(""); //all item selected
                                }
                                else{
                                    String optionClicked = options[which];
                                    filteredProductTv.setText("Showing "+ optionClicked+ "Orders"); //e.g showing completed orders
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open shop review activity
                Intent intent = new Intent(MainSellerActivity.this,ShopReviewActivity.class);
                intent.putExtra("ShopUid",firebaseAuth.getUid());
                startActivity(intent);
            }
        });

        //when clicked on setting button, open setting screen
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this,SettingActivity.class));
            }
        });

    }

    private void loadAllOrderes() {
//        init array list
        modelOrderShopArrayList = new ArrayList<>();

        //load orders of shop
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding new data into it
                        modelOrderShopArrayList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            modelOrderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(modelOrderShopArrayList,MainSellerActivity.this);
                        //set adapter to recyclerview
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadSelectedProducts(String selected) {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();
                            //if selected category matches in product then add in the list
                            if(selected.equals(productCategory)){

                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRV.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadAllProducts() {

        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){

                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRV.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showOrdersUI() {
        //show products ui and hide orders ui
        productsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);
        tabProductTv.setTextColor(getResources().getColor(R.color.black));
        tabProductTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrderTv.setTextColor(getResources().getColor(R.color.white));
        tabOrderTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showProductUI() {
        //show orders ui and hide products ui
        ordersRL.setVisibility(View.VISIBLE);
        productsRL.setVisibility(View.GONE);
        tabOrderTv.setTextColor(getResources().getColor(R.color.black));
        tabOrderTv.setBackgroundResource(R.drawable.shape_rect04);

        tabProductTv.setTextColor(getResources().getColor(R.color.white));
        tabProductTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));


    }

    private void checkUser() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
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
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            //get data from database
                            String name = "" + ds.child("Name").getValue();
                            String accountType = "" + ds.child("AccountType").getValue();
                            String email = "" + ds.child("Email").getValue();
                            String shopname = "" + ds.child("ShopName").getValue();
                            String sellerProfileImage = "" + ds.child("ProfileImg").getValue();

                            //set data to ui
                            sellerName.setText(""+name);
                            sellerEmail.setText(shopname);
                            shopName.setText(shopname);
                            try {
                                Picasso.get().load(sellerProfileImage).placeholder(R.drawable.ic_person).into(profilPic);
                            }
                            catch (Exception e){
                                profilPic.setImageResource(R.drawable.ic_store);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}