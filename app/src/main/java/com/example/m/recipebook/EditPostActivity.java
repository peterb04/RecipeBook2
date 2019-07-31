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
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditPostActivity extends AppCompatActivity {

    private TextView editPostTitle, editPostDescription, editPostIngredients, editPostEquipment, editPostDirections, editPostCategories;
    private ImageView editPostImage;
    private ImageButton addImage;
    private DatabaseReference clickPostReference;

    private FirebaseAuth mAuth;

    private int EDIT_POST_ACT_REC = 2, EDIT_POST_ACT_EQUIP = 3;
    private int EditPostActivity = 5, CATEGORIES_ACTIVITY = 6;
    private String PostKey, CurrentUserId;
    private List<String> ingredients = new ArrayList<String>();
    private List<String> equipment = new ArrayList<String>();
    private List<String> directions = new ArrayList<String>();
    private List<String> categories = new ArrayList<String>();
    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String recipeImage, image = null;
    private ProgressDialog progressDialog;
    private StorageReference PostedImageReference;
    private AlertDialog.Builder alertDialog;
    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarEditPost);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Recipe");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        progressDialog = new ProgressDialog(this, R.style.MyDialogTheme);
        alertDialog = new AlertDialog.Builder(this, R.style.EditDialogTheme);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        editPostTitle = (TextView) findViewById(R.id.TV_editPostRecTitle);
        editPostDescription = (TextView) findViewById(R.id.TV_EditPostDescription);
        editPostImage = (ImageView) findViewById(R.id.IV_EditPostImage);
        editPostIngredients = (TextView) findViewById(R.id.TV_EditPostIngredients);
        editPostEquipment = (TextView) findViewById(R.id.TV_EditPostEquipment);
        editPostDirections = (TextView) findViewById(R.id.TV_EditPostDirections);
        editPostCategories = (TextView) findViewById(R.id.TV_EditPostCategories);
        addImage = (ImageButton) findViewById(R.id.IB_EditPostImage);

        clickPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue().toString();
                String description = dataSnapshot.child("description").getValue().toString();
                if (dataSnapshot.child("recipeMainImage").exists()) {
                    image = dataSnapshot.child("recipeMainImage").getValue().toString();
                }
                int noIngred = (int) dataSnapshot.child("Ingredients").getChildrenCount();
                int noEquipment = (int) dataSnapshot.child("Equipment").getChildrenCount();
                int noDirections = (int) dataSnapshot.child("Directions").getChildrenCount();
                int noCategories = (int) dataSnapshot.child("Categories").getChildrenCount();

                String temp = "";
                for (int i = 0; i < noIngred; i++) {//sorts data in ingredients database to ArrayList

                    temp = String .valueOf(i);
                    String ingred = dataSnapshot.child("Ingredients").child(temp).getValue().toString();
                    ingredients.add(ingred);
                }

                if (ingredients != null){
                    for (int i = 0; i < ingredients.size(); i++){
                        editPostIngredients.setText(editPostIngredients.getText() + ingredients.get(i) + "\n");
                    }
                }

                for (int i = 0; i < noEquipment; i++) {//sorts data in ingredients database to ArrayList

                    temp = String .valueOf(i);
                    String equip = dataSnapshot.child("Equipment").child(temp).getValue().toString();
                    equipment.add(equip);
                }

                if (equipment != null){
                    for (int i = 0; i < equipment.size(); i++){
                        editPostEquipment.setText(editPostEquipment.getText() + equipment.get(i) + "\n");
                    }
                }

                for (int i = 0; i < noDirections; i++) {//sorts data in ingredients database to ArrayList

                    temp = String .valueOf(i);
                    String direct = dataSnapshot.child("Directions").child(temp).getValue().toString();
                    directions.add(direct);
                }

                if (directions != null){
                    for (int i = 0; i < directions.size(); i++){
                        editPostDirections.setText(editPostDirections.getText() + directions.get(i) + "\n");
                    }
                }

                for (int i = 0; i < noCategories; i++) {//sorts data in ingredients database to ArrayList

                    temp = String .valueOf(i);
                    String cat = dataSnapshot.child("Categories").child(temp).getValue().toString();
                    categories.add(cat);
                }

                if (categories != null){
                    for (int i = 0; i < categories.size(); i++){
                        editPostCategories.setText(editPostCategories.getText() + categories.get(i) + "\n");
                    }
                }

                editPostTitle.setText(title);
                editPostDescription.setText(description);
                //Picasso.get().load(image).into(editPostImage);
                Glide.with(getApplicationContext()).load(image).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        editPostImage.setVisibility(View.GONE);
                        editPostImage.setEnabled(false);
                        addImage.setVisibility(View.VISIBLE);
                        addImage.setEnabled(true);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        editPostImage.setVisibility(View.VISIBLE);
                        editPostImage.setEnabled(true);
                        addImage.setVisibility(View.GONE);
                        addImage.setEnabled(false);
                        return false;
                    }
                }).into(editPostImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editPostTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editPostTitle.getText().toString();
                EditPost("Edit Recipe Title:" , title, "title");
            }
        });

        editPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditRecipeImage();
            }
        });

        editPostDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Descript = editPostDescription.getText().toString();
                EditPost("Edit Description:", Descript, "description");
            }
        });

        editPostIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditIngredients();
            }
        });

        editPostEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditEquipment();
            }
        });

        editPostDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditDirections();
            }
        });

        editPostCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditCategories();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditRecipeImage();
            }
        });
    }

    private void EditCategories() {
        showCategoriesPopup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void EditRecipeImage() {
        final CharSequence[] items;
        if (image != null) {
             items = new CharSequence[]{"Change Image", "Delete Image", "Cancel"};
        }
        else{
             items = new CharSequence[] {"Add Image", "Cancel"};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
        builder.setTitle("Recipe Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (items[i].equals("Change Image") || items[i].equals("Add Image")) {
                    openGallery();
                }
                else if (items[i].equals("Delete Image")) {
                    alertDialog.setTitle("Delete Image");
                    alertDialog.setMessage("This will delete your recipes image");
                    alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PostedImageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);
                            PostedImageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditPostActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                            clickPostReference.child("recipeMainImage").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditPostActivity.this, "Image database deleted", Toast.LENGTH_SHORT).show();
                                    editPostImage.setVisibility(View.GONE);
                                    editPostImage.setEnabled(false);
                                    addImage.setVisibility(View.VISIBLE);
                                    addImage.setEnabled(true);

                                }
                            });
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", null);
                    alertDialog.show();
                }
                else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }

        });
        builder.show();
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(EditPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(EditPostActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(EditPostActivity.this, "Permission to Read Storage Needed", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(EditPostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else {
            galleryIntent();
        }
    }

    private void galleryIntent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleryIntent();
                } else {
                    Toast.makeText(EditPostActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){

            Bitmap bitmap = null;
            Bitmap rotatedBitmap = null;
            Uri selectedImage = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                rotatedBitmap = rotateImage(bitmap, selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (rotatedBitmap != null) {
                Uri temp = getImageUri(this, rotatedBitmap);
                recipeImage = temp.toString();
            }
            else {
                recipeImage = selectedImage.toString();
            }

            ShowEditDialog();
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
            image.compress(Bitmap.CompressFormat.JPEG,20,stream);
            stream.flush();
            stream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return Uri.parse(String.valueOf(Uri.fromFile(file)));
    }

    private void ShowEditDialog() {
        String title = "Update Recipe Image";
        String message = "This will update your recipe image.";
        int success = 1, failed = 0, result;
        final CustomDialogRecipeImage customDialogRecipeImage = new CustomDialogRecipeImage(EditPostActivity.this, title, message, recipeImage);
        customDialogRecipeImage.show();
        customDialogRecipeImage.dialogRecipeImageOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogRecipeImage.dismiss();
                if (image != null) {
                    PostedImageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);
                    PostedImageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditPostActivity.this, "Old image deleted", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditPostActivity.this, "Did not delete image", Toast.LENGTH_SHORT).show();
                        }
                    });
                    PostedImageReference = FirebaseStorage.getInstance().getReference();
                }
                else{
                    PostedImageReference = FirebaseStorage.getInstance().getReference();
                }
                progressDialog.setTitle("Updating Recipe Image");
                progressDialog.setMessage("Updating your recipe image...");
                progressDialog.show();
                UpdateRecipeImageToStorage();

            }
        });
    }

    private void UpdateRecipeImageToStorage() {
        String saveCurrentDate;
        String saveCurrentTime;
        String postRandomName;
        final String[] downloadUrl = new String[1];
        Calendar callForDate = Calendar.getInstance();                          //to give the image a unique id using the date and time
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime =currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        ImageUri = Uri.parse(recipeImage);

        final StorageReference filePath = PostedImageReference.child("Recipe Main Image").child(ImageUri.getLastPathSegment() + CurrentUserId + postRandomName + ".jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl[0] = uri.toString();
                            Toast.makeText(EditPostActivity.this, "Image Uploaded Successfully to storage", Toast.LENGTH_SHORT).show();
                            SavePostInformationToDatabase(downloadUrl[0]);
                        }
                    });
                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(EditPostActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void SavePostInformationToDatabase(String downloadUrl) {
        clickPostReference.child("recipeMainImage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(EditPostActivity.this, "Recipe image updated in database", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditPostActivity.this, EditPostActivity.class);
                    intent.putExtra("PostKey", PostKey);
                    startActivity(intent);
                    finish();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(EditPostActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void EditDirections() {
        showDirectionsPopup();
    }

    private void EditEquipment() {
        showPopup(EDIT_POST_ACT_EQUIP);
    }

    private void EditIngredients() {
        showPopup(EDIT_POST_ACT_REC);
    }

    private void EditPost(String title, String editText, final String childVal){

        final  CustomDialogEditText customDialogEditText = new CustomDialogEditText(EditPostActivity.this, title, editText);
        customDialogEditText.show();
        customDialogEditText.dialogEditTextOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogEditText.dismiss();
                clickPostReference.child(childVal).setValue(customDialogEditText.dialogTextEditText.getText().toString());
                Toast.makeText(EditPostActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EditPostActivity.this, ProfileActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showPopup(int option){

        Intent intent = new Intent(EditPostActivity.this, AddPopUp.class);                     //Sends all these values to the next activity
        intent.putExtra("OPTION", option);
        intent.putExtra("CLASS", EditPostActivity);
        intent.putStringArrayListExtra("INGREDIENTS", (ArrayList<String>) ingredients);//ingredient);
        intent.putStringArrayListExtra("EQUIPMENT",(ArrayList<String>) equipment);
        intent.putExtra("PostKey", PostKey);

        EditPostActivity.this.startActivity(intent);                                       //Go to the AddPopUp Activity
    }

    private void showDirectionsPopup() {
        Intent intent = new Intent(EditPostActivity.this, AddDirections.class);         //Sends all these values to the next activity;
        intent.putExtra("CLASS", EditPostActivity);
        intent.putStringArrayListExtra("DIRECTIONS", (ArrayList<String>) directions);
        intent.putExtra("PostKey", PostKey);

        EditPostActivity.this.startActivity(intent);
    }

    private void showCategoriesPopup(){
        Intent intent = new Intent(EditPostActivity.this, CategoriesActivity.class);
        intent.putExtra("CLASS", EditPostActivity);
        intent.putExtra("PostKey", PostKey);
        intent.putStringArrayListExtra("CATEGORIES", (ArrayList<String>)categories);
        EditPostActivity.this.startActivity(intent);
    }
}
