package com.example.grocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.EditProductActivity;
import com.example.grocery.FilterProduct;
import com.example.grocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {


    private Context context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterProduct filter;

    public AdapterProductSeller(Context context,ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {

        //get data
       final ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNode  = modelProduct.getDiscountNote();
        String discountPrice  = modelProduct.getDiscountNote();
        String productCategory  = modelProduct.getProductCategory();
        String productDescription  = modelProduct.getProductDescrition();
        String icon  = modelProduct.getProductIcon();
        String quantity  = modelProduct.getProductQuantity();
        String title  = modelProduct.getProductTitle();
        String timestamp  = modelProduct.getTimestamp();
        String originalPrice  = modelProduct.getOriginalPrice();


        //set Data
        holder.titleTV.setText(title);
        holder.quantityTV.setText(quantity);
        holder.discountedNoteTV.setText(discountNode);
        holder.discountedPriceTV.setText("Rs."+discountPrice);
        holder.originalPrice.setText("Rs."+originalPrice);
        if(discountAvailable.equals("true")){
            //product is on discount
            holder.discountedPriceTV.setVisibility(View.VISIBLE);
            holder.discountedNoteTV.setVisibility(View.VISIBLE);
            holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price
        }
        else {
            //product is not on discount
            holder.discountedPriceTV.setVisibility(View.GONE);
            holder.discountedNoteTV.setVisibility(View.GONE);
            holder.originalPrice.setPaintFlags(0);


        }
        try {

            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_blue).into(holder.productIconIV);
        }
        catch (Exception e){
            holder.productIconIV.setImageResource(R.drawable.ic_baseline_add_shopping_blue);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks , show item details
                detailsBottomSheet(modelProduct); //here model product contains details of clicked product
            }
        });


    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view of bottomsheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_products_details_seller,null);

//        set view to bottom sheet
        bottomSheetDialog.setContentView(view);

        //show dialog


        //init view of bottomsheet
        ImageButton backBtnIV = view.findViewById(R.id.backBtnIV);
        ImageButton deleteBtnIV = view.findViewById(R.id.deleteBtnIV);
        ImageButton editBtnIV = view.findViewById(R.id.editBtnIV);
        ImageView productIconIVV = view.findViewById(R.id.productIconIVV);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView titleTVV = view.findViewById(R.id.titleTVV);
        TextView descriptionTVV = view.findViewById(R.id.descriptionTVV);
        TextView categoryTVV = view.findViewById(R.id.categoryTVV);
        TextView quantityTVV = view.findViewById(R.id.quantityTVV);
        TextView discountedPriceTVV = view.findViewById(R.id.discountedPriceTVV);
        TextView originalPriceTVV = view.findViewById(R.id.originalPriceTVV);

        //get data
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNode  = modelProduct.getDiscountNote();
        String discountPrice  = modelProduct.getDiscountNote();
        String productCategory  = modelProduct.getProductCategory();
        String productDescription  = modelProduct.getProductDescrition();
        String icon  = modelProduct.getProductIcon();
        String quantity  = modelProduct.getProductQuantity();
        String title  = modelProduct.getProductTitle();
        String timestamp  = modelProduct.getTimestamp();
        String originalPrice  = modelProduct.getOriginalPrice();

        //set data
        titleTVV.setText(title);
        descriptionTVV.setText(productDescription);
        categoryTVV.setText(productCategory);
        quantityTVV.setText(quantity);
        discountedNoteTv.setText(discountNode);
        discountedPriceTVV.setText("Rs."+discountPrice);
        originalPriceTVV.setText("Rs."+originalPrice);

        if(discountAvailable.equals("true")){
            //product is on discount
            discountedPriceTVV.setVisibility(View.VISIBLE);
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTVV.setPaintFlags(originalPriceTVV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price
        }
        else {
            //product is not on discount
            discountedPriceTVV.setVisibility(View.GONE);
            discountedNoteTv.setVisibility(View.GONE);

        }
        try {

            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_blue).into(productIconIVV);
        }
        catch (Exception e){
            productIconIVV.setImageResource(R.drawable.ic_baseline_add_shopping_blue);
        }
        bottomSheetDialog.show();

        //edit click
        editBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //open edit product activity, pass id of product
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);

            }
        });
        //delete click
        deleteBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure to delete product "+title +"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //delete
                                deleteProduct(id); //id is the product id

                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //cancel,dismiss dialog
                                dialog.dismiss();
                            }
                        }).show();

            }
        });
        //back click
        backBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id) {
        //delete product using its id
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //product deleted
                        Toast.makeText(context, "Product deleted...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        //holds view of recyclerview

        private ImageView productIconIV;
        private TextView discountedNoteTV,titleTV,quantityTV,discountedPriceTV;
        private TextView originalPrice;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIV = itemView.findViewById(R.id.productIconIV);
            discountedNoteTV = itemView.findViewById(R.id.discountedNoteIv);
            titleTV = itemView.findViewById(R.id.titleTV);
            quantityTV = itemView.findViewById(R.id.quantityTV);
            discountedPriceTV = itemView.findViewById(R.id.discountedPriceTV);
            originalPrice = itemView.findViewById(R.id.originalPriceTV);



        }
    }
}
