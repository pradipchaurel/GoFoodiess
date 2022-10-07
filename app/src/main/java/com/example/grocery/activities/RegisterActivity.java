package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity implements LocationListener {

    ImageButton backBtn, gpsBtn;
    EditText fullName, email, password, confirmPassword, phone, country, state, city, address;
    Button register;
    ImageView profilePic;
    TextView sellerreg;

    private LocationManager locationManager;
    private double latitude, longitude;


    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] localPermissions;
    private String[] cameraPermissions;
    private String[] storagePermisson;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        backBtn = (ImageButton) findViewById(R.id.regbackBtn);
        gpsBtn = (ImageButton) findViewById(R.id.gpsLocation);
        fullName = (EditText) findViewById(R.id.fullname);
        email = (EditText) findViewById(R.id.regEmail);
        password = (EditText) findViewById(R.id.regpassword);
        confirmPassword = (EditText) findViewById(R.id.regConfirmpassword);
        phone = (EditText) findViewById(R.id.regphonecall);
        country = (EditText) findViewById(R.id.country);
        state = (EditText) findViewById(R.id.state);
        city = (EditText) findViewById(R.id.city);
        address = (EditText) findViewById(R.id.fullAddress);
        register = (Button) findViewById(R.id.registerButton);
        profilePic = (ImageView) findViewById(R.id.profilePic);
        sellerreg = (TextView) findViewById(R.id.regseller);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();

        //init persmission array
        localPermissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set profile pic
                showImagePickDialog();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set location
                if (checkLocalPermission()) {
                    detectLocation();
                } else {
                    requestLocalPermission();
                }
            }
        });

        sellerreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open seller register activity

                startActivity(new Intent(RegisterActivity.this, RegisterSeller.class));

            }
        });
    }

    private void showImagePickDialog() {
        String[] options = {"Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0) {
                            //camera clicked
                            pickFromGallery();

                        } else {
                            //gallery clicked
                        }
                    }
                });

    }

    private String FullName,PhoneNumber,Country,State,City,Address,Email,Password,ConfirmPassword;
    private void inputData() {
        FullName = fullName.getText().toString().trim();

        PhoneNumber = phone.getText().toString().trim();

        Country = country.getText().toString().trim();
        State = state.getText().toString().trim();
        City = city.getText().toString().trim();
        Address = address.getText().toString().trim();
        Email = email.getText().toString().trim();
        Password = password.getText().toString().trim();
        ConfirmPassword = confirmPassword.getText().toString().trim();

        //validate data

        if(TextUtils.isEmpty(FullName)){
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(PhoneNumber)){
            Toast.makeText(this, "Enter phone number...", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(Country)){
            Toast.makeText(this, "Enter country...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(State)){
            Toast.makeText(this, "Enter State...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(City)){
            Toast.makeText(this, "Enter City...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(Address)){
            Toast.makeText(this, "Enter full address...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Password.length()<6){
            Toast.makeText(this, "Password must be atleast 6 characters long ...", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!Password.equals(ConfirmPassword)){
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();

    }

    private void createAccount() {
        progressDialog.setTitle("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(Email,Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //account created
                saverFireBase();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failing in creating account
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));


    private void saverFireBase() {
        progressDialog.setTitle("Saving Profile Info");
        if(image_uri==null){
            //save info without profile pic

            HashMap<String,Object> hashMap= new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("Email",""+Email);
            hashMap.put("Name",""+FullName);

            hashMap.put("Phone",""+PhoneNumber);

            hashMap.put("Country",""+Country);
            hashMap.put("State",""+State);
            hashMap.put("City",""+City);
            hashMap.put("Address",""+Address);
            hashMap.put("Latitude",""+latitude);
            hashMap.put("Longitude",""+longitude);
            hashMap.put("Timestamp",""+timeStamp);
            hashMap.put("AccountType","User");
            hashMap.put("Online","true");
            hashMap.put("ShopOpen","true");
            hashMap.put("ProfileImg","");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterActivity.this, MainUserActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterActivity.this,MainUserActivity.class));
                            finish();
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
                                HashMap<String,Object> hashMap= new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("Email",""+Email);
                                hashMap.put("Name",""+FullName);

                                hashMap.put("Phone",""+PhoneNumber);

                                hashMap.put("Country",""+Country);
                                hashMap.put("State",""+State);
                                hashMap.put("City",""+City);
                                hashMap.put("Address",""+Address);
                                hashMap.put("Latitude",""+latitude);
                                hashMap.put("Longitude",""+longitude);
                                hashMap.put("Timestamp",""+timeStamp);
                                hashMap.put("AccountType","User");
                                hashMap.put("Online","true");
                                hashMap.put("ShopOpen","open");
                                hashMap.put("ProfileImg",downloadImageUri);
                                //update to database
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(RegisterActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to update
                                                progressDialog.dismiss();
                                                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to upload image
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
    }



    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }


    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }
    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
//            Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String useraddress = addresses.get(0).getAddressLine(0);
            String usercity = addresses.get(0).getLocality();
            String usercountry = addresses.get(0).getCountryName();
            String userstate = addresses.get(0).getAdminArea();

            city.setText(usercity);
            country.setText(usercountry);
            state.setText(userstate);
            address.setText(useraddress);
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkLocalPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocalPermission(){
        ActivityCompat.requestPermissions(this,localPermissions,LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestPermissionStorage(){
        ActivityCompat.requestPermissions(this,storagePermisson,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return (result && result1);
    }

    private void requestPermissionCamera(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();
    }




    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Please Turn on location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean localAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(localAccepted){
                        detectLocation();
                    }
                    else{
                        Toast.makeText(this, "Please allow location", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}