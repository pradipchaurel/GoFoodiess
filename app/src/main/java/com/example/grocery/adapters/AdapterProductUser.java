package com.example.grocery.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterProductUser;
import com.example.grocery.R;
import com.example.grocery.activities.AddProductActivity;
import com.example.grocery.activities.DBConnect;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {


    private Context context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterProductUser filter;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    public AdapterProductUser(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }
    public DBConnect dbConnect = new DBConnect(this.context);
    class HolderProductUser extends RecyclerView.ViewHolder{

        //ui views
        private ImageView productIconIV,nextIVVV;
        private TextView discountedNoteTV,titleTv,addToCartTV,discountedPriceTVVV,originalPriceTVVV,descriptionTVVV;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            //init ui views
            productIconIV = itemView.findViewById(R.id.iproductIconIVVV);
            discountedNoteTV = itemView.findViewById(R.id.discountedNoteIvvv);
            titleTv = itemView.findViewById(R.id.ititleTVVV);
            addToCartTV = itemView.findViewById(R.id.iaddToCartTV);
            discountedPriceTVVV = itemView.findViewById(R.id.idiscountedPriceTVVV);
            originalPriceTVVV = itemView.findViewById(R.id.ioriginalPriceTVVV);
            nextIVVV = itemView.findViewById(R.id.nextIVVV);
            descriptionTVVV = itemView.findViewById(R.id.idescriptionTVVV);

        }
    }


    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        //get data
        ModelProduct modelProduct = productList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNode = modelProduct.getDiscountNote();
//        String discountPrice = modelProduct.getDiscountNote();
        String productCategory = modelProduct.getDiscountAvailable();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescrition();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();

        //set data
        holder.titleTv.setText(productTitle);
        holder.discountedNoteTV.setText(discountNode);
        holder.descriptionTVVV.setText(productDescription);
        holder.originalPriceTVVV.setText("Rs."+originalPrice);
        holder.discountedPriceTVVV.setText("Rs.0");
        if(discountAvailable.equals("true")){
            //product is on discount
            holder.discountedPriceTVVV.setVisibility(View.VISIBLE);
            holder.discountedNoteTV.setVisibility(View.VISIBLE);
            holder.originalPriceTVVV.setPaintFlags(holder.originalPriceTVVV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price
        }
        else {
            //product is not on discount
            holder.discountedPriceTVVV.setVisibility(View.GONE);
            holder.discountedNoteTV.setVisibility(View.GONE);
            holder.originalPriceTVVV.setPaintFlags(0);
        }
        try {

            Picasso.get().load(productIcon).placeholder(R.drawable.ic_baseline_add_shopping_blue).into(holder.productIconIV);
        }
        catch (Exception e){
            holder.productIconIV.setImageResource(R.drawable.ic_baseline_add_shopping_blue);
        }

        holder.addToCartTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add to cart
                showQuantityDialog(modelProduct);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details
            }
        });


    }


    private double cost=0,finalCost=0,quatity = 0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        //init layout views
        ImageView productIv = view.findViewById(R.id.iproductIV);
        TextView titleTv = view.findViewById(R.id.productTitleTv);
        TextView quatityTvvv = view.findViewById(R.id.quatityTvvv);
        TextView descriptionTvvvv = view.findViewById(R.id.descriptionTvvvv);
        TextView discountNoteTvv = view.findViewById(R.id.discountNoteTvv);
        TextView originalpricetvvvvv = view.findViewById(R.id.originalpricetvvvvv);
        TextView priceDiscounted = view.findViewById(R.id.priceDiscounted);
        TextView finalPrice = view.findViewById(R.id.finalPrice);
        ImageButton decrement= view.findViewById(R.id.decrement);
        ImageButton increment = view.findViewById(R.id.increment);
        TextView quantityselectTv = view.findViewById(R.id.quantityselectTv);
        Button continueBtn = view.findViewById(R.id.cartBtn);

        //get data from model
        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescrition();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();
        String price;
        if(modelProduct.getDiscountAvailable().equals("true")){
            //product have discount
            price = modelProduct.getDiscountNote();
            discountNoteTvv.setVisibility(View.VISIBLE);
            originalpricetvvvvv.setPaintFlags(originalpricetvvvvv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price

        }
        else{
            //product don't have discount
            discountNoteTvv.setVisibility(View.GONE);
            priceDiscounted.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();


        }
        cost = Double.parseDouble(price.replaceAll("Rs.",""));
        finalCost  = Double.parseDouble(price.replaceAll("Rs.",""));
        quatity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_baseline_add_shopping_blue).into(productIv);
        }
        catch (Exception e){
            productIv.setImageResource(R.drawable.ic_baseline_add_shopping_blue);
        }
        titleTv.setText(""+title);
        quatityTvvv.setText(""+productQuantity);
        descriptionTvvvv.setText(""+description);
        discountNoteTvv.setText(""+discountNote);
        originalpricetvvvvv.setText("Rs."+modelProduct.getOriginalPrice());
        priceDiscounted.setText("Rs.0");
        finalPrice.setText("Rs."+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();


        //increase the quantity of product
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quatity++;
                finalPrice.setText("Rs."+finalCost);
                quantityselectTv.setText(""+quatity);
            }
        });


        //decrease the quantity of product
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quatity>1){
                    finalCost = finalCost - cost;
                    quatity--;
                    finalPrice.setText("Rs."+finalCost);
                    quantityselectTv.setText(""+quatity);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTv.getText().toString();
                String priceEach = price;
                String totalPrice = finalPrice.getText().toString().trim().replace("Rs.","");
                String quantity = quantityselectTv.getText().toString().trim();
                //add to db(SQLite)
                addToCart(productId,title,priceEach,totalPrice,quantity);


                dialog.dismiss();
            }
        });
    }

    private int itemId = 1;

    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        Boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();
        Toast.makeText(context, "Added to Cart...", Toast.LENGTH_SHORT).show();

        //update cart count
        ((ShopDetailsActivity)context).cartCounts();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new FilterProductUser(this,filterList);

        }
        return filter;
    }


}
