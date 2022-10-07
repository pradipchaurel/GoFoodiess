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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET,passwordEt;
    private TextView forgotIv,registerHere;
    private Button loginButton;
    private ImageView phoneLogin, googleLogin;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        emailET = (EditText) findViewById(R.id.useremail_edittext);
        passwordEt = (EditText) findViewById(R.id.user_password_edittext);
        forgotIv = (TextView) findViewById(R.id.forget_password_textview);
        registerHere = (TextView) findViewById(R.id.register_textview_signup);
        loginButton = (Button) findViewById(R.id.user_login_button);
        phoneLogin = (ImageView) findViewById(R.id.login_with_phone);
        googleLogin = (ImageView) findViewById(R.id.login_with_google);



        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

//when user clicks on register button
        registerHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

//        when user clicks on forgot textview
        forgotIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

//        when user clicks on login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //login user
                loginUser();
            }
        });
    }

    private String email,password;
    private void loginUser() {
        email = emailET.getText().toString().trim();
        password = passwordEt.getText().toString().trim();


        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        }


        progressDialog.setMessage("Logging...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressDialog.dismiss();
                //login successful
//                if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                    makeOnline();
//                }
//                else{
//                    Toast.makeText(LoginActivity.this, "Email is not verified", Toast.LENGTH_SHORT).show();
//                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //login fail
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void makeOnline() {
        progressDialog.setMessage("Checking user....");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("Online","true");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                            //updated successfully
                        checkUserType();
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserType() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    String accountType = ""+ds.child("AccountType");
                    if(accountType.equals("Seller")){
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
//                        startActivity(new Intent(LoginActivity.this,MainUserActivity.class));
                        finish();
                    }
                    else{
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
//                        startActivity(new Intent(LoginActivity.this,MainSellerActivity.class));
                        finish();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}