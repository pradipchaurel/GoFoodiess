package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingActivity extends AppCompatActivity {
    //ui views
    private ImageButton settingBackBtn;
    private SwitchCompat fcmswitch;
    private TextView notificationsStatusTv;

    private static final String enabledMessage = "Notifications are enabled";
    private static final String disabledMessage = "Notifications are disabled";

    private boolean isChecked = false;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //init views
        fcmswitch = findViewById(R.id.fcmswitch);
        notificationsStatusTv = findViewById(R.id.notificationsStatusTv);
        settingBackBtn = findViewById(R.id.settingBackBtn);

        firebaseAuth = FirebaseAuth.getInstance();

        //init shared Preferences
        sharedPreferences = getSharedPreferences("SETTTINGS_SP",MODE_PRIVATE);
        //check last selected option, true/false
        isChecked = sharedPreferences.getBoolean("FCM_ENABLED",false);
        fcmswitch.setChecked(isChecked);

        if(isChecked){
            //was enabled
            notificationsStatusTv.setText(enabledMessage);
        }
        else{
            //was disabled
            notificationsStatusTv.setText(disabledMessage);
        }

        //when clicked on back button
        settingBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //add switch check change listener to enable disable notifications
        fcmswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //checked, enable notification

                    subsribeToTopic();

                }
                else{
                    //not checked, disable notification
                    unsubscribeToTopic();
                }
            }
        });
    }

    private void subsribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //save setting ins shared preferences
                        //subscribed successfully
                        //save setting
                        spEditor = sharedPreferences.edit();
                        spEditor.putBoolean("FCM_ENABLED",true);
                        spEditor.apply();

                        Toast.makeText(SettingActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        notificationsStatusTv.setText(enabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed subscribing
                        Toast.makeText(SettingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void unsubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //unsubscribed
                        spEditor = sharedPreferences.edit();
                        spEditor.putBoolean("FCM_ENABLED",false);
                        spEditor.apply();
                        Toast.makeText(SettingActivity.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        notificationsStatusTv.setText(disabledMessage);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed unsubscribing
                        Toast.makeText(SettingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

//we will work with topic based firebase messaging, for user to receive topic based message/notification have to subscribe to that topic
//requirements FCM server key