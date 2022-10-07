package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class RegistrationActivity extends AppCompatActivity {

    private EditText user_firstname_edittext,user_email_edittext,user_phoneno_edittext;
    private EditText user_password_edittext,user_cPassword_edittext;
    private Button user_btnRegister_button;
    private TextView alreadyAccount;

//  permissions code
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;


    //permission arrays
    private String[] localPermissions;
    private String[] cameraPermissions;
    private String[] storagePermisson;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
//
//        init ui views
        user_email_edittext = findViewById(R.id.user_email_edittext);
        user_firstname_edittext = findViewById(R.id.user_firstname_edittext);
        user_phoneno_edittext = findViewById(R.id.user_phoneno_edittext);
        user_password_edittext = findViewById(R.id.user_password_edittext);
        user_cPassword_edittext = findViewById(R.id.user_cPassword_edittext);
        user_btnRegister_button = findViewById(R.id.user_btnRegister_button);
        alreadyAccount = findViewById(R.id.already_have_an_account);


        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

//         when user clicks on registration activity
        user_btnRegister_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

//        when user clicks on already have an account
        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this,SellerRegistrationActivity.class));
            }
        });
    }

    private String FullName,PhoneNumber,Email,Password,ConfirmPassword;
    private void inputData() {

        FullName = user_firstname_edittext.getText().toString().trim();
        PhoneNumber = user_phoneno_edittext.getText().toString().trim();
        Password = user_password_edittext.getText().toString().trim();
        ConfirmPassword = user_cPassword_edittext.getText().toString().trim();
        Email = user_email_edittext.getText().toString().trim();

//        validate data

        if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
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

//        create account
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
                Toast.makeText(RegistrationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

    private void saverFireBase() {

        progressDialog.setTitle("Saving Profile Info");

            //save info without profile pic

            HashMap<String,Object> hashMap= new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("Email",""+Email);
            hashMap.put("Name",""+FullName);
            hashMap.put("Phone",""+PhoneNumber);
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
                            startActivity(new Intent(RegistrationActivity.this, MainUserActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegistrationActivity.this,MainUserActivity.class));
                            finish();
                        }
                    });


    }


}
