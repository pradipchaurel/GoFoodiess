package com.example.grocery.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview>{

    private Context context;
    private ArrayList<ModelReview> modelReviewArrayList;

    public AdapterReview(Context context, ArrayList<ModelReview> modelReviewArrayList) {
        this.context = context;
        this.modelReviewArrayList = modelReviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_review,parent,false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        //get data at position
        ModelReview modelReview = modelReviewArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String timestamps = modelReview.getTimestamp();
        String reviews = modelReview.getReview();

        //to know the info of user who has written review
        loadUserDetails(modelReview,holder);

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamps));
        String dateformat = DateFormat.format("dd/MM/yyyy",calendar).toString();


        //set data
        holder.ratinggbar.setRating(Float.parseFloat(ratings));
        holder.originalreview.setText(reviews);
        holder.datee.setText(dateformat);
    }

    private void loadUserDetails(ModelReview modelReview, HolderReview holder) {
        String uid = modelReview.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user info
                        String name = ""+snapshot.child("Name").getValue();
                        String profileImage = ""+snapshot.child("ProfileImg").getValue();

                        //set user info
                        holder.nameTv.setText(name);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_person).into(holder.userProfileView);
                        }
                        catch (Exception e){
                            holder.userProfileView.setImageResource(R.drawable.ic_person);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelReviewArrayList.size();
    }

    //view holder class, init/holds views of recyclerview
    class HolderReview extends RecyclerView.ViewHolder{

        //ui views of layout row_review
        private ImageView userProfileView;
        private TextView nameTv, datee, originalreview;
        private RatingBar ratinggbar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);
            userProfileView = itemView.findViewById(R.id.userProfileView);
            nameTv = itemView.findViewById(R.id.nameTv);
            datee = itemView.findViewById(R.id.datee);
            ratinggbar = itemView.findViewById(R.id.ratinggbar);
            originalreview = itemView.findViewById(R.id.originalreview);
        }
    }
}
