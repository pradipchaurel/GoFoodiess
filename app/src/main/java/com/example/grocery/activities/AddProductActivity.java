package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.grocery.Constants;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {


    private ImageButton backBtn;
    private ShapeableImageView appProduct;
    private EditText title,description,quantity,price,category,discount,discountNote;
    private Button addproduct;
    private SwitchCompat switch1;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;


    //permission arrays
    private String[] cameraPermission;
    private String[] storagePermission;

    //image picked uri
    private Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        backBtn = (ImageButton) findViewById(R.id.backbtnn);
        appProduct = findViewById(R.id.addproduct);
        title = (EditText) findViewById(R.id.titleEt);
        description = (EditText) findViewById(R.id.descriptionEt);
        quantity = (EditText) findViewById(R.id.quantity);
        category = (EditText) findViewById(R.id.category);
        price = (EditText) findViewById(R.id.price);
        discount = (EditText) findViewById(R.id.discountedPrice);
        discountNote = (EditText) findViewById(R.id.discountedNote);
        addproduct = (Button) findViewById(R.id.addProductButton);
        switch1 = (SwitchCompat) findViewById(R.id.discountSwitch);

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);


        discount.setVisibility(View.GONE);
        discountNote.setVisibility(View.GONE);
        //id discountSwitch is checked show discountprice and discountnote otherwise hide it
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    discount.setVisibility(View.VISIBLE);
                    discountNote.setVisibility(View.VISIBLE);
                }
                else{

                    discount.setVisibility(View.GONE);
                    discountNote.setVisibility(View.GONE);
                }
            }

        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        appProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //seller dialog to pick an image
                showImagePickDialog();
            }
        });

        //here the seller can add product
        addproduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //flow
                //input data
                //validate data
                //add data to db
                inputData();
            }
        });


        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //pick category
                categoryDialog();
            }
        });


    }
    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNode;
    private boolean discountAvailable;

    private void inputData() {
        productTitle = title.getText().toString().trim();
        productDescription = description.getText().toString().trim();
        productCategory = category.getText().toString().trim();
        productQuantity = quantity.getText().toString().trim();
        originalPrice = price.getText().toString().trim();
        discountPrice = discount.getText().toString().trim();
        discountNode = discountNote.getText().toString().trim();
        discountAvailable = switch1.isChecked();  //true //false


        //validate data
        if(TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price is required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(discountAvailable){
            discountPrice = discount.getText().toString().trim();
            discountNode = discountNote.getText().toString().trim();
            if(TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "Discount price is required...", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else{
            //product is without discount
            discountPrice = "0";
            discountNode = "";
        }
        addproducti();
    }

    private void addproducti() {
        //add data to db
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();
        final String timestamp = ""+System.currentTimeMillis();
        if(image_uri==null){
            //upload without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId",""+timestamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescrition",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productIcon","");  //without image
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNode);
            hashMap.put("discountAvailable",""+discountAvailable);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("uid",""+firebaseAuth.getUid());

            //add to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added to db
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product Added...", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    }).
            addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //failed adding to db
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
        else{
            //upload with image

            //first upload image to db
            //name and path of image to be uploaded
            String filePathAndName = "product_images/"+timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //image uploaded
                    //get url of image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    Uri downloadImageUri  = uriTask.getResult();
                    if(uriTask.isSuccessful()){
                        //url of image received , upload to db
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("productId",""+timestamp);
                        hashMap.put("productTitle",""+productTitle);
                        hashMap.put("productDescrition",""+productDescription);
                        hashMap.put("productCategory",""+productCategory);
                        hashMap.put("productQuantity",""+productQuantity);
                        hashMap.put("productIcon",""+downloadImageUri);  //with image
                        hashMap.put("originalPrice",""+originalPrice);
                        hashMap.put("discountNote",""+discountNode);
                        hashMap.put("discountAvailable",""+discountAvailable);
                        hashMap.put("timestamp",""+timestamp);
                        hashMap.put("uid",""+firebaseAuth.getUid());

                        //add to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //added to db
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, "Product Added...", Toast.LENGTH_SHORT).show();
                                        clearData();
                                    }
                                }).
                                addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //failed adding to db
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //failed uploading images
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void clearData(){
        //clear data after uploading product
        title.setText("");
        description.setText("");
        category.setText("");
        quantity.setText("");
        price.setText("");
        discount.setText("");
        discountNote.setText("");
        appProduct.setImageResource(R.drawable.ic_baseline_add_shopping_blue);
        image_uri = null;
    }

    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").
        setItems(Constants.options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //set picked category
                String categorii = Constants.options[which];
                category.setText(categorii);
            }
        });

    }

    private void showImagePickDialog() {

        //options to display in dialog
        String[] options = {"Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle click items
                if(which==0){
                    //gallery clicked
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
    private void pickFromGallery(){

        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){

        //intent to pick image from camera
        //using media store to pick high/original quality image

//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image_title");
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image_Description");
//
//        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

//        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result & result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    //handle permission results

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //both permission granted
                        pickFromCamera();
                    }
                    else{
                        //both or none of permission denied
                        Toast.makeText(this, "Storage and camera permission required...", Toast.LENGTH_SHORT).show();
                    }
                }
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle image pick results

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if(requestCode==RESULT_OK){
//            if(requestCode == IMAGE_PICK_GALLERY_CODE){
//                //image picked from gallery
//
//                //saved picked image uri
//                image_uri = data.getData();
//
//                //set image
//                appProduct.setImageURI(image_uri);
//            }
//            else if(resultCode == IMAGE_PICK_CAMERA_CODE){
//                appProduct.setImageURI(image_uri);
//            }
//        }
        if(requestCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();
                appProduct.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}