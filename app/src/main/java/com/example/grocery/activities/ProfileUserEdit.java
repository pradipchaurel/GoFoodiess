package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileUserEdit extends AppCompatActivity{

    ImageButton backbtn,gpsBtn;
    ImageView profilePic;
    EditText fullname,phoneNumber,country1,state1,city1,address1;
    Button update;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;


    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image,location pick constants
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;

    //permission arrays

    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;


    private Uri image_uri;

    //permissions code

    private double latitude=0.0,logitude=0.0;



    //progressDialog
    private ProgressDialog progressDialog;


    private LocationManager locationManager;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user_edit);

        profilePic = (ImageView) findViewById(R.id.editProfilePic);
        fullname = (EditText) findViewById(R.id.edituserfullname);
        phoneNumber = (EditText) findViewById(R.id.edituserregphonecall);
        update = (Button) findViewById(R.id.userUpdate);
        backbtn = (ImageButton) findViewById(R.id.userEditbackBtn);


        //initialize permissions
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //setup progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();



        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update seller profile
                inputData();

            }
        });
    }
    private String userName,userPhone,userCountry,userState,userCity,userAddress;

    private void inputData() {

        userName = fullname.getText().toString().trim();
        userPhone = phoneNumber.getText().toString().trim();
        userCountry = country1.getText().toString().trim();
        userState = state1.getText().toString().trim();
        userCity = city1.getText().toString().trim();
        userAddress = address1.getText().toString().trim();
        updateProfile();
    }

    private void updateProfile() {
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        if(image_uri==null){
            //update without profile pic
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("Name",""+userName);
            hashMap.put("Phone",""+userPhone);
            hashMap.put("Country",""+userCountry);
            hashMap.put("City",""+userCity);
            hashMap.put("State",""+userState);
            hashMap.put("Address",""+userAddress);
            hashMap.put("Latitude",""+latitude);
            hashMap.put("Longitude",""+logitude);

            //update to database
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updated
                            progressDialog.dismiss();
                            Toast.makeText(ProfileUserEdit.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to update
                            progressDialog.dismiss();
                            Toast.makeText(ProfileUserEdit.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else{
            //update with profile pic

//            upload image
            String filePathAndName = "profile_images/"+firebaseAuth.getUid();
            //get storage reference
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url of image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if(uriTask.isSuccessful()){
                                //image received, now update db
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("Name",""+userName);
                                hashMap.put("Phone",""+userPhone);
                                hashMap.put("Country",""+userCountry);
                                hashMap.put("State",""+userState);
                                hashMap.put("City",""+userCity);
                                hashMap.put("Address",""+userAddress);
                                hashMap.put("Latitude",""+latitude);
                                hashMap.put("Longitude",""+logitude);
                                hashMap.put("ProfileImg",""+downloadImageUri);

                                //update to database
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileUserEdit.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to update
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileUserEdit.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to upload image
                            progressDialog.dismiss();
                            Toast.makeText(ProfileUserEdit.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }


    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {

        //load user info and set to views
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String accountType = ""+ds.child("AccountType").getValue();
                    String address = ""+ds.child("Address").getValue();
                    String city = ""+ds.child("City").getValue();
                    String country = ""+ds.child("Country").getValue();
                    String state = ""+ds.child("State").getValue();
                    String email = ""+ds.child("Email").getValue();
//                     latitude =Double.parseDouble(""+ds.child("Latitude").getValue());
//                     logitude = Double.parseDouble(""+ds.child("Longitude").getValue());
                    String name = ""+ds.child("Name").getValue();
                    String phone = ""+ds.child("Phone").getValue();
                    String profileImage = ""+ds.child("ProfileImg").getValue();
                    String timestamp = ""+ds.child("Timestamp").getValue();

                    fullname.setText(name);
                    phoneNumber.setText(phone);
                    country1.setText(country);
                    city1.setText(city);
                    state1.setText(state);
                    country1.setText(country);
                    address1.setText(address);


                    try {

                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person).into(profilePic);
                    }
                    catch (Exception e){
                        profilePic.setImageResource(R.drawable.ic_person);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showImagePickDialog() {

        //options to display
        String[] options = new String[]{"Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle item clicks
                if(which ==0){
                    //gallery selected
                    if(checkStoragePermission()){
                        pickFromGallery();
                    }
                    else{
                        requestStoragePermission();
                    }
                }
            }
        }).show();
    }
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromGallery() {

        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode==RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                // gallery code allowed
                image_uri = data.getData();
                profilePic.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}