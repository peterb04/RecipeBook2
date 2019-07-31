package com.example.m.recipebook;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class FirstTimeUserActivity extends AppCompatActivity {

    private EditText firstTimeUsername, firstTimeInfo;
    private ImageView firstTimePhoto;
    private Button saveButton;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private int SELECT_IMAGE = 0, REQUEST_CAMERA = 1;

    private String username, profileImage;
    private String email, firstName;

    //Folder Path for Firebase Storage
    String mStoragePath = "All_Image_Uploads/";
    //Root Database name for Firebase database
    String mDatabasePath = "Data";
    //Create URI
    Uri imageUri;
    //Create storage reference and database reference
    //StorageReference mStorageReference;
    //DatabaseReference mDataBaseReference;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference ProfileImageRef;
    String currentUserID;
    //Progress Dialogue
    private ProgressDialog mProgressDialog;
    //Image Requiest Code for choosing image
    int IMAGE_REQUEST_CODE = 5;
    final int PIC_CROP = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_user);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        ProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        firstTimeUsername = (EditText) findViewById(R.id.ET_FirstTimeUName);
        firstTimeInfo = (EditText) findViewById(R.id.ET_FirstTimeInfo);
        firstTimePhoto = (ImageView) findViewById(R.id.IV_FIrstTimeProfilePic);
        saveButton = (Button) findViewById(R.id.B_FirstTimeUserSave);
        firstTimeInfo = (EditText) findViewById(R.id.ET_FirstTimeInfo);
        mProgressDialog = new ProgressDialog(this, R.style.MyDialogTheme);

        final Intent intent = getIntent();
        email = intent.getStringExtra("EMAIL");
        firstName = intent.getStringExtra("FIRST_NAME");

        if (firstName != null) {
            firstTimeUsername.setText(firstName);
        }

        firstTimeUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().trim().length() == 0){
                    saveButton.setEnabled(false);
                }
                else {
                    saveButton.setEnabled(true);
                }

                if (charSequence.toString().length() >= 29){
                    Toast.makeText(FirstTimeUserActivity.this, "Username limit reached.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0){
                    saveButton.setEnabled(false);
                }
                else {
                    saveButton.setEnabled(true);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        firstTimePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfilePicture();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email != null){
                    SaveAccountFirstTimeInfo(email);
                }
                else {
                    SaveAccountFirstTimeInfo("");
                }

            }
        });
    }

    private void SaveAccountFirstTimeInfo(String email) {
        String username = firstTimeUsername.getText().toString();
        String userInfo = firstTimeInfo.getText().toString();
        final String[] downloadUrl = new String[1];

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter your username",Toast.LENGTH_SHORT).show(); //Checks if username is empty
        }
        else
        {   mProgressDialog.setTitle("Saving Information");
            mProgressDialog.setMessage("Please wait, while we're creating your account");
            mProgressDialog.show();
            mProgressDialog.setCanceledOnTouchOutside(true);


            if (profileImage != null) {
                imageUri = Uri.parse(profileImage);
                final StorageReference filePath = ProfileImageRef.child(currentUserID + ".jpg");
                filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(FirstTimeUserActivity.this, "Profile image stored successfully", Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl[0] = uri.toString();
                                    StoreImageLocationToDatabase(downloadUrl[0]);
                                }
                            });
                        }
                    }
                });
            }

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("userInfo", userInfo);
            userMap.put("userEmail", email);
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(FirstTimeUserActivity.this, "Account Created Successfully",Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(FirstTimeUserActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                }
            });

        }
    }

    private void StoreImageLocationToDatabase(String imageUrl) {
        UsersRef.child("profileimage").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(FirstTimeUserActivity.this, "Profile image stored successfully", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(FirstTimeUserActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(FirstTimeUserActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //Does not allow the user to come back to this class if the back button is pressed
        startActivity(mainIntent);
        finish();
    }


    private void editProfilePicture() {
        final CharSequence[] items = {"Choose from Library", "Delete Image", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(FirstTimeUserActivity.this);
        builder.setTitle("Profile Picture");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (items[i].equals("Choose from Library")) {
                    if (ContextCompat.checkSelfPermission(FirstTimeUserActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(FirstTimeUserActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            Toast.makeText(FirstTimeUserActivity.this, "Permission to Read Storage Needed", Toast.LENGTH_SHORT).show();
                        } else {
                            ActivityCompat.requestPermissions(FirstTimeUserActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    } else {
                        galleryIntent();
                    }
                }
                /*else if (items[i].equals("Take Photo")) {  // Needs to be updated
                    if (ContextCompat.checkSelfPermission(FirstTimeUserActivity.this, Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(FirstTimeUserActivity.this,
                                Manifest.permission.CAMERA)) {
                            Toast.makeText(FirstTimeUserActivity.this, "Permission to Access Camera Needed", Toast.LENGTH_SHORT).show();
                        } else {
                            ActivityCompat.requestPermissions(FirstTimeUserActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }
                    else {
                        cameraIntent();
                    }


                }*/
                else if (items[i].equals("Delete Image")){
                    firstTimePhoto.setImageDrawable(null);
                }
                else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_IMAGE);


    }

    private void cameraIntent(){

        Intent cameraIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Bitmap bitmap = null;
                Bitmap rotatedBitmap = null;
                Uri selectedImage = data.getData();
                // String[] filePathColumn = {MediaStore.Images.Media.DATA};
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    rotatedBitmap = rotateImage(bitmap, selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (rotatedBitmap != null) {
                    Uri temp = getImageUri(this, rotatedBitmap);
                    profileImage = temp.toString();
                }
                else {
                    profileImage = selectedImage.toString();
                }
                firstTimePhoto.setImageURI(Uri.parse(profileImage));
            }

        }
        else if (requestCode == REQUEST_CAMERA) {

            Bitmap bitmap = null;
            Bitmap rotatedBitmap = null;
            Uri selectedImage = (Uri)data.getExtras().get("data");
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                rotatedBitmap = rotateImage(bitmap, selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (rotatedBitmap != null) {
                Uri temp = getImageUri(this, rotatedBitmap);
                profileImage = temp.toString();
            }
            else {
                profileImage = selectedImage.toString();
            }
            firstTimePhoto.setImageURI(Uri.parse(profileImage));
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, Uri selectedImage) {
        InputStream inputStream = null;
        Bitmap rotatedBitmap = null;
        try {
            inputStream = getContentResolver().openInputStream(selectedImage);
            ExifInterface exifInterface = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                exifInterface = new ExifInterface(inputStream);
            }

            int rotation = 0;
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    matrix.setRotate(270);
                    break;
            }
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return rotatedBitmap;
    }

    private Uri getImageUri(Context context, Bitmap image) {
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("Images", MODE_PRIVATE);
        file = new File(file, "tempImage"+".jpg");
        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG,10,stream);
            stream.flush();
            stream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return Uri.parse(String.valueOf(Uri.fromFile(file)));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    galleryIntent();
                }
                else {
                    Toast.makeText(FirstTimeUserActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case MY_PERMISSIONS_REQUEST_CAMERA:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    cameraIntent();
                }
                else {
                    Toast.makeText(FirstTimeUserActivity.this, "Camera Permission not Granted", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Sign In");
        progressDialog.setMessage("Signing in to your account...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);
        try {
            currentUserID = mAuth.getCurrentUser().getUid();
            DatabaseReference UsersRef1;
            UsersRef1 = FirebaseDatabase.getInstance().getReference().child("Users");
            UsersRef1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if (dataSnapshot.hasChild(currentUserID)){
                            progressDialog.dismiss();
                            SendUserToMainActivity();
                        }
                        else{
                            progressDialog.dismiss();
                        }
                    }
                    else{
                        progressDialog.dismiss();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
}
