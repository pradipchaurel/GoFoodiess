package com.example.grocery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.grocery.activities.OrderDetailsSellterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFireBaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID"; //required for android 0 and above
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
//        all notification will be received here

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //get data from notification
        String notificationType =  message.getData().get("notificationType");
        if(notificationType.equals("NewOrder")){

            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("buyerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationDescription = message.getData().get("notificationDescription");
            if(firebaseUser !=null && firebaseAuth.getUid().equals(sellerUid)){
                //user is signed and to same user the notification is to be sent
                showNotification(orderId,sellerUid,buyerUid,notificationTitle,notificationDescription,notificationType);
            }

        }
        if(notificationType.equals("OrderStatusChanged")){
            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("buyerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationDescription = message.getData().get("notificationMessage");
            if(firebaseUser !=null && firebaseAuth.getUid().equals(buyerUid)){
                //user is signed and to same user the notification is to be sent
                showNotification(orderId,sellerUid,buyerUid,notificationTitle,notificationDescription,notificationType);

            }
        }
    }
    private void showNotification(String orderId, String sellerUid, String buyerUid, String notificationTitle, String notificationDescription,String notificationType){

        //notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //id for notification, random
        int notificationID = new Random().nextInt(3000);

        //check if android version is Oreo/O or above
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            setUpNotificationChannel(notificationManager);
        }

        //handle notification click, start order activity
        Intent intent = null;
        if(notificationType.equals("NewOrder")){
            //open orderDetailsSellerActivity
            intent = new Intent(this, OrderDetailsSellterActivity.class);
            intent.putExtra("orderId",orderId);
            intent.putExtra("orderBy",buyerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else if(notificationType.equals("OrderStatusChanged")){
//            open detailsuserActivity
            intent = new Intent(this, OrderDetailsSellterActivity.class);
            intent.putExtra("orderId",orderId);
            intent.putExtra("orderTo",sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        //largest icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_store);

        //sound of notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.ic_store)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setSound(notificationSoundUri)
                .setAutoCancel(true)   //cancel / dismiss when clicked
                .setContentIntent(pendingIntent);  //add intent

        //show notification
        notificationManager.notify(notificationID,notificationBuilder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some sample text";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if(notificationManager!=null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
