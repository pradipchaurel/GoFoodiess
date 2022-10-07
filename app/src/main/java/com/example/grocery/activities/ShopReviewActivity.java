package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewActivity extends AppCompatActivity {

     private String shopUid;
     private ImageButton backBT;
     private ImageView shopProfileV;
     private TextView shopnTv, ratingsTv;
     private RatingBar ratingsbarr;
     private RecyclerView reviewRV;

     private FirebaseAuth firebaseAuth;

     private ArrayList<ModelReview> reviewArrayList; //contains list of all reviews
     private AdapterReview adapterReview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_review);

        shopUid = getIntent().getStringExtra("ShopUid");

        //init views
        backBT = findViewById(R.id.backBT);
        shopProfileV = findViewById(R.id.shopProfileV);
        shopnTv = findViewById(R.id.shopnTv);
        ratingsbarr = findViewById(R.id.ratingsbarr);
        ratingsTv = findViewById(R.id.ratingsTv);
        reviewRV = findViewById(R.id.reviewRV);

        firebaseAuth = FirebaseAuth.getInstance();

        loadDetails();  //for shop name, image
        loadReviews();  //for reviews list and ratings

        backBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private float ratingSum = 0;
    private void loadReviews() {
        //init list
        reviewArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it
                        reviewArrayList.clear();
                        ratingSum = 0;
                        for(DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum+=rating;

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        adapterReview = new AdapterReview(ShopReviewActivity.this,reviewArrayList);

                        //set to recyclerview
                        reviewRV.setAdapter(adapterReview);
                        long numberOfReviewsCount = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviewsCount;

                        ratingsTv.setText(String.format("%.2f",avgRating));
                        ratingsbarr.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get value
                        String shopName = ""+snapshot.child("ShopName").getValue();
                        String profileImg = ""+snapshot.child("ProfileImg").getValue();

                        //set value
                        shopnTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImg).placeholder(R.drawable.ic_person).into(shopProfileV);
                        }
                        catch (Exception e){
                            shopProfileV.setImageResource(R.drawable.ic_person);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}