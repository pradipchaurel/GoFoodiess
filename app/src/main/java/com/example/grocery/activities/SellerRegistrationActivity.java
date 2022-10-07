package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SellerRegistrationActivity extends AppCompatActivity {

    private EditText sellername_edittext,shopName_edittext,seller_email_edittext,
            seller_phoneNo_edittext,seller_password_edittext,seller_cPassword_edittext;
    private Button seller_register_button;



//    permission codes
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;


//permission arrays
    private String[] localPermissions;
    private String[] cameraPermissions;
    private String[] storagePermisson;

    private Uri image_uri=null;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_registration);

        //init ui views
        sellername_edittext = findViewById(R.id.sellername_edittext);
        shopName_edittext = findViewById(R.id.shopName_edittext);
        seller_email_edittext = findViewById(R.id.seller_email_edittext);
        seller_phoneNo_edittext = findViewById(R.id.seller_phoneNo_edittext);
        seller_password_edittext = findViewById(R.id.seller_password_edittext);
        seller_cPassword_edittext = findViewById(R.id.seller_cPassword_edittext);
        seller_register_button = findViewById(R.id.seller_register_button);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


//        when seller clicks on register button
        seller_register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }

    private String fullName,ShopName,PhoneNumber,Email,Password,ConfirmPassword;

    private void inputData() {
        fullName = sellername_edittext.getText().toString().trim();
        ShopName = shopName_edittext.getText().toString();
        PhoneNumber = seller_phoneNo_edittext.getText().toString().trim();
        Email = seller_email_edittext.getText().toString().trim();
        Password = seller_password_edittext.getText().toString().trim();
        ConfirmPassword = seller_cPassword_edittext.getText().toString().trim();


        //validate data

        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(ShopName)){
            Toast.makeText(this, "Enter shop name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(PhoneNumber)){
            Toast.makeText(this, "Enter phone number...", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SellerRegistrationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    private void saverFireBase() {
        progressDialog.setTitle("Saving Profile Info");

        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("Email",""+Email);
        hashMap.put("Name",""+fullName);
        hashMap.put("ShopName",""+ShopName);
        hashMap.put("Phone",""+PhoneNumber);
        hashMap.put("Timestamp",""+timeStamp);
        hashMap.put("AccountType","Seller");
        hashMap.put("Online","true");
        hashMap.put("ShopOpen","true");
        hashMap.put("ProfileImg","");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        startActivity(new Intent(SellerRegistrationActivity.this, MainSellerActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        startActivity(new Intent(SellerRegistrationActivity.this,MainSellerActivity.class));
                        finish();
                    }
                });

    }
}