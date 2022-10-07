package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    private String shopUid;
    private ImageButton backB;
    private ImageView profileview;
    private TextView shoppname;
    private RatingBar ratingsBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        shopUid = getIntent().getStringExtra("ShopUid");
        backB = findViewById(R.id.backB);
        profileview = findViewById(R.id.profileview);
        shoppname = findViewById(R.id.shoppname);
        ratingsBar = findViewById(R.id.ratingsBar);
        reviewEt = findViewById(R.id.reviewEt);
        submitBtn = findViewById(R.id.submitBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        //if user has written review to it then load it
        loadMyReview();

        //load shop info: shop name, shop image
        loadShopInfo();

        backB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //input data after clicking on submit button
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inputData();
            }
        });
    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop info
                String shopName = ""+snapshot.child("ShopName").getValue();
                String shopImage = ""+snapshot.child("ProfileImage").getValue();

                //set shop info
                shoppname.setText(shopName);
                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store).into(profileview);
                }
                catch (Exception e){
                    profileview.setImageResource(R.drawable.ic_store);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //review available in this shop
                            //get review details
                            String uid = ""+snapshot.child("uid").getValue();
                            String ratingss = ""+snapshot.child("ratings").getValue();
                            String reviews = ""+snapshot.child("review").getValue();
                            String timestampss = ""+snapshot.child("timestamp").getValue();

                            //set review details to ui
                            float myRating = Float.parseFloat(ratingss);
                            ratingsBar.setRating(myRating);
                            reviewEt.setText(reviews);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String rating = ""+ratingsBar.getRating();
        String review = ""+reviewEt.getText().toString().trim();

        //for time of review
        String timestamp = ""+System.currentTimeMillis();

        //set up data in hashmap
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("ratings",""+rating);
        hashMap.put("review",""+review);
        hashMap.put("timestamp",""+timestamp);

        //put it to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(shopUid).child("ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Thank you for your review...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update db
                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}