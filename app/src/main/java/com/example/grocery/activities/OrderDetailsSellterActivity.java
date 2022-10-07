package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.grocery.Constants;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderedItems;
import com.example.grocery.models.ModelOrderedItems;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailsSellterActivity extends AppCompatActivity {

    //ui views
    private ImageButton backkiBtn, editiBtn;
    private TextView orderIdN,dateiT,orderStatuss,buyerEmail,buyerPhone,itemsOrder;
    private TextView amount,addrress;
    private RecyclerView itemsRvv;

    String orderId,orderBy;
    //to open destination in map
    double sourceLatitude, sourceLongitude;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItems> modelOrderedItemsList;
    private AdapterOrderedItems orderedItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_sellter);

        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        //init ui views
        backkiBtn = findViewById(R.id.backkiBtn);
        editiBtn = findViewById(R.id.editiBtn);
        orderIdN = findViewById(R.id.orderIdN);
        dateiT = findViewById(R.id.dateiT);
        orderStatuss = findViewById(R.id.orderStatuss);
        buyerEmail = findViewById(R.id.buyerEmail);
        buyerPhone = findViewById(R.id.buyerPhone);
        itemsOrder = findViewById(R.id.itemsOrder);
        amount = findViewById(R.id.amount);
        addrress = findViewById(R.id.addrress);
        itemsRvv = findViewById(R.id.itemsRvv);

        firebaseAuth = FirebaseAuth.getInstance();

//        loadMyinfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        //when clicked on back button
        backkiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //when clicked on edit button
        editiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit order status that is in progress, cancelled, completed
                editOrderDialog();
            }
        });


    }

    private void editOrderDialog() {
        //options to display in dialog
        String[] options = {"In Progress","Completed","Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Items")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle items clicks
                        String selectedOptions = options[which];
                        editOrderStatus(selectedOptions);
                    }
                })
                .show();
    }

    private void editOrderStatus(String selectedOptions) {
        //setup data to put in firebase
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus",""+selectedOptions);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //status updated
                        String message = "Order is now" + selectedOptions;
                        Toast.makeText(OrderDetailsSellterActivity.this, message, Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(orderId, message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed in updating status
                        Toast.makeText(OrderDetailsSellterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderDetails() {
        //load details of this order based on order id
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get order info
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("Latitude").getValue();
                        String longitude = ""+snapshot.child("Longitude").getValue();


                        //convert timestamp
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormat = DateFormat.format("dd/MM/yyy",calendar).toString();

                        //order status
                        if(orderStatus.equals("In Progress")){
                            orderStatuss.setTextColor(getResources().getColor(R.color.purple_500));
                        }
                        else if(orderStatus.equals("Completed")){
                            orderStatuss.setTextColor(getResources().getColor(R.color.green));
                        }
                        else if(orderStatus.equals("Cancelled")){
                            orderStatuss.setTextColor(getResources().getColor(R.color.red));
                        }

                        //set data
                        orderIdN.setText(orderId);
                        orderStatuss.setText(orderStatus);
                        amount.setText("Rs."+orderCost+"[Including delivery fee Rs."+deliveryFee+"]");
                        dateiT.setText(dateFormat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMyinfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sourceLatitude = Double.parseDouble(""+snapshot.child("Latitude").getValue());
                        sourceLongitude = Double.parseDouble(""+snapshot.child("Longitude").getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get buyer info
//                        sourceLatitude = Double.parseDouble(""+snapshot.child("Latitude").getValue());
//                        sourceLongitude = Double.parseDouble(""+snapshot.child("Longitude").getValue());
                        String email = ""+snapshot.child("Email").getValue();
                        String phone = ""+snapshot.child("Phone").getValue();

                        //set value
                        buyerEmail.setText(email);
                        buyerPhone.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems(){
        //load products/items of order

        //init arraylist
        modelOrderedItemsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelOrderedItemsList.clear(); //before adding data clear list
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderedItems modelOrderedItems = ds.getValue(ModelOrderedItems.class);
                            //add to list
                            modelOrderedItemsList.add(modelOrderedItems);
                        }

                        //setup adapter
                        orderedItems = new AdapterOrderedItems(modelOrderedItemsList,OrderDetailsSellterActivity.this);
                        //set adapter to our recyclerview
                        itemsRvv.setAdapter(orderedItems);

                        //set total number of items/products in order
                        itemsOrder.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void prepareNotificationMessage(String orderId,String message){
        //when user seller changes order status send notification to buyer

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topic/" + Constants.FCM_TOPIC;  //must be same as subscribed by user
        String NOTIFICATION_TITLE = "Your Order"+orderId;
        String NOTIFICATION_MESSAGE = "" + message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";
        String NOTIFICATION_DESCRIPTION = "ABCD";

        //prepare json what to send and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",orderBy); //since we have logged in as user so the current user uid is buyer uid
            notificationBodyJo.put("sellerUid",firebaseAuth.getUid()); //since we have logged in as user so the current user uid is buyer uid
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);
            notificationBodyJo.put("notificationDescription",NOTIFICATION_MESSAGE);
            notificationBodyJo.put("notificationDescription",NOTIFICATION_DESCRIPTION);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);  //to all who subscribed to this topic
            notificationJo.put("data",notificationBodyJo);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //notication failed
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                notification failed
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-type","applicatioin/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);
                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }

}