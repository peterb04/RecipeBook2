package com.example.m.recipebook;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.m.recipebook.Database.SqliteDatabaseHelper;
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
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
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
import java.util.HashMap;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    EditText NrecipeTitle, Ndescription;
    TextView ingredients, equipment, addIngred, addEquipment, addDirections, categories, addCategories, Ndirections;
    private ImageView recipePhoto;
    private VideoView recipeVideo, fullScreenVideo;
    private String title;
    private String description;
    private String savedTableID;
    private String recipeImage;
    private int mediaType = 0, image = 0, video = 1;
    private Button saveRecipe, publishLater;
    private ImageButton deleteRecipeButton, playVideoButton;
    private StorageReference PostedImageReference;
    private String saveCurrentDate, saveCurrentTime, timestamp, postRandomName, downloadUrl, current_user_id;
    private static final int Gallery_Pick = 1, Gallery_Video_Pick = 2;
    private Uri ImageUri, selectedVideo;
    private DatabaseReference usersRef, postRef, CategoriesRef, savedRecipeRef, friendsPostRef, friendsRef;
    private FirebaseAuth mAuth;
    private AlertDialog.Builder alertDialog;
    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 0;
    private ConstraintLayout constraintLayoutVideo;

    private SqliteDatabaseHelper recipeDB;

    int INGREDIENTS = 0, EQUIPMENT = 1, option, fromMain = 0, savedRecipeTracker = 0, editFinishLater = 0;                     //TO allow the AddPopUp activity class know which option the user has selected so that both options can use the same class
    List<String> savedIngredients = new ArrayList<String>();  //Sets these variables as an arrayList
    List<String> savedEquipment = new ArrayList<String>();
    List<String> savedDirections = new ArrayList<String>();
    List<String> retIngred = new ArrayList<String>();
    List<String> retEquip = new ArrayList<String>();
    List<String> retDir = new ArrayList<String>();
    List<String> category = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarAddRecipe);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add new recipe");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();

        PostedImageReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        savedRecipeRef = FirebaseDatabase.getInstance().getReference().child("UnfinishedRecipes");
        friendsPostRef = FirebaseDatabase.getInstance().getReference().child("FriendsPost");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        CategoriesRef = FirebaseDatabase.getInstance().getReference().child("Categories");

        NrecipeTitle = (EditText) findViewById(R.id.ET_RecipeTitle);
        recipePhoto = (ImageView) findViewById(R.id.newRecipePhoto);
        recipeVideo = (VideoView) findViewById(R.id.VV_newRecipeVideo);
        Ndescription = (EditText) findViewById(R.id.ET_Description);
        ingredients = (TextView) findViewById(R.id.TV_Ingredients);
        equipment = (TextView) findViewById(R.id.TV_Equipment);
        addEquipment = (TextView) findViewById(R.id.TV_AddEquipment);
        addIngred = (TextView) findViewById(R.id.TV_AddIngredient);
        Ndirections = (TextView) findViewById(R.id.ET_Directions);
        addDirections = (TextView) findViewById(R.id.TV_AddDirections);
        addCategories = (TextView) findViewById(R.id.TV_AddRecipesAddEditCategories);
        categories = (TextView) findViewById(R.id.TV_AddRecipeSelectedCategories);
        saveRecipe = (Button) findViewById(R.id.B_AddRecipeSave);
        publishLater = (Button) findViewById(R.id.B_AddRecipePublishLater);
        deleteRecipeButton = (ImageButton) findViewById(R.id.IB_AddRecipeDelete);
        playVideoButton = (ImageButton) findViewById(R.id.IB_AddRecipePlay);
        constraintLayoutVideo = (ConstraintLayout) findViewById(R.id.CL_AddRecipeVideo);

        alertDialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        progressDialog = new ProgressDialog(this, R.style.MyDialogTheme);

        recipeDB = new SqliteDatabaseHelper(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //   Retrieve any information that was sent from the AddPopUp class (which was sent to the AddPopClass from this activity.)
        final Intent intent = getIntent();

        title = intent.getStringExtra("TITLEret");
        NrecipeTitle.setText(title);
        recipeImage = intent.getStringExtra("RECIPEIMAGE");
        if (recipeImage != null){
            recipePhoto.setImageURI(Uri.parse(recipeImage));
        }
        Ndescription.setText( intent.getStringExtra("DESCRIPTIONret"));
        retIngred = getIntent().getStringArrayListExtra("INGREDret");//
        retEquip = getIntent().getStringArrayListExtra("EQUIPret");//
        retDir = getIntent().getStringArrayListExtra("DIRret");
        category = getIntent().getStringArrayListExtra("CATEGORY");
        savedRecipeTracker = intent.getIntExtra("SavedRecipeTracker",0);

        option = intent.getIntExtra("OPTION",0);        //So we know what option was worked on in the AddPopUp class.
        fromMain = intent.getIntExtra("FromMain",0);
        editFinishLater = intent.getIntExtra("EDIT", 0);
        savedTableID = intent.getStringExtra("TABLE_ID");

        SetupEditAndFinishLaterButton();

        NrecipeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() >= 38){
                    Toast.makeText(AddRecipeActivity.this, "Title limit reached.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Ndescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() >= 249){
                    Toast.makeText(AddRecipeActivity.this, "Text limit reached.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (fromMain == 5){ // Called from MainActivity
            int table = 0, ingredients = 1, equipment = 2, method = 3;
            int position = intent.getIntExtra("RECIPE_ID",0);
            String recipeID = String.valueOf(position);
            Toast.makeText(AddRecipeActivity.this, recipeID, Toast.LENGTH_SHORT).show();

            Cursor data = recipeDB.showData(table);
            data.moveToPosition(position);
            savedTableID = (data.getString(0));
            String title = (data.getString(1));
            String unDescription = (data.getString(2));

            NrecipeTitle.setText(title);
            Ndescription.setText(unDescription);

            Cursor dataIngred = recipeDB.showData(ingredients);
            dataIngred.moveToPosition(position);
            List<String> savedIngredients = new ArrayList<String>();
            try {
                for (int i = 0; i <15; i++){
                        if (dataIngred.getString(i+1) != null){
                            if (!dataIngred.getString(i + 1).equals(" ")) {
                                savedIngredients.add(dataIngred.getString(i + 1));
                            }
                        }
                }
            } catch (Exception e) {
                Toast.makeText(this, "No ingredients", Toast.LENGTH_SHORT).show();
            }
            retIngred = savedIngredients;

            Cursor dataEquip = recipeDB.showData(equipment);
            dataEquip.moveToPosition(position);
            List<String> savedEquipment = new ArrayList<String>();
            try {
                for (int i = 0; i <15; i++){
                        if (dataEquip.getString(i+1) != null){
                            if (!dataEquip.getString(i + 1).equals(" ")) {
                                savedEquipment.add(dataEquip.getString(i + 1));
                            }
                        }
                }
            } catch (Exception e) {
                Toast.makeText(this, "No Equipment", Toast.LENGTH_SHORT).show();
            }
            retEquip = savedEquipment;

            Cursor dataMethod = recipeDB.showData(method);
            dataMethod.moveToPosition(position);
            List<String> savedMethod = new ArrayList<String>();
            try {
                for (int i = 0; i <15; i++){
                        if (dataMethod.getString(i+1) != null){
                            if (!dataMethod.getString(i + 1).equals(" ")) {
                                savedMethod.add(dataMethod.getString(i + 1));
                            }
                        }
                }
            } catch (Exception e) {
                Toast.makeText(this, "No Method saved", Toast.LENGTH_SHORT).show();
            }
            retDir = savedMethod;

        }

        //retrieve data from AddPopUp class
        if (retIngred != null){
            for (int i = 0; i < retIngred.size(); i++) {
                ingredients.setText(ingredients.getText() + retIngred.get(i) + "\n");
            }
        }

        if (retEquip != null){
            for (int i = 0; i < retEquip.size(); i++) {
                equipment.setText(equipment.getText() + retEquip.get(i) + "\n");
            }
        }

        if (retDir != null){
            for (int i = 0; i < retDir.size(); i++){
                Ndirections.setText(Ndirections.getText() + retDir.get(i) + "\n");
            }
        }

        if (category != null){
            for (int i = 0; i < category.size(); i++) {
                categories.setText(categories.getText() + category.get(i) + "\n");
            }
        }

        recipePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        recipeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        //On click listener for add / modify ingredients button pressed
        addIngred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = NrecipeTitle.getText().toString();                  //Prepares variables to send values to next activity so that we dont lose them
                description = Ndescription.getText().toString();

                showPopup(INGREDIENTS);                                 //Ingredients button was selected by user.
            }
        });

        ingredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = NrecipeTitle.getText().toString();                  //Prepares variables to send values to next activity so that we dont lose them
                description = Ndescription.getText().toString();

                showPopup(INGREDIENTS);
            }
        });

        //OnClickListener for add / modify equipment button pressed
        addEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = NrecipeTitle.getText().toString();              //Prepares variables to send values to next activity so that we dont lose them
                description = Ndescription.getText().toString();

                showPopup(EQUIPMENT);                               //Equipment button was selected by user.
            }
        });

        equipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = NrecipeTitle.getText().toString();              //Prepares variables to send values to next activity so that we dont lose them
                description = Ndescription.getText().toString();

                showPopup(EQUIPMENT);
            }
        });

        addDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = NrecipeTitle.getText().toString();              //Prepares variables to send values to next activity so that we dont lose them
                description = Ndescription.getText().toString();

                showDirectionsPopup();
            }
        });

        Ndirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = NrecipeTitle.getText().toString();              //Prepares variables to send values to next activity so that we dont lose them
                description = Ndescription.getText().toString();

                showDirectionsPopup();
            }
        });



        addCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = NrecipeTitle.getText().toString();              //Prepares variables to send values to next activity so that we don't lose them
                description = Ndescription.getText().toString();
                AddCategories();
            }
        });

        categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = NrecipeTitle.getText().toString();              //Prepares variables to send values to next activity so that we don't lose them
                description = Ndescription.getText().toString();
                AddCategories();
            }
        });

        saveRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAddRecipeActivityInfo();
            }
        });

        publishLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.setTitle("Save Recipe");
                alertDialog.setMessage("Recipe will be saved to your device.");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PublishLater(savedTableID);
                    }
                });

                alertDialog.setNegativeButton("Cancel", null).create();
                alertDialog.show();
            }
        });

        deleteRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteRecipe(savedTableID);
            }
        });

        playVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayVideo();
            }
        });
    }

    private void PlayVideo() {
        Intent intent = new Intent(AddRecipeActivity.this, FullScreenVideo.class);         //Sends all these values to the next activity;
        intent.putExtra("VIDEOURI", selectedVideo.toString());
        AddRecipeActivity.this.startActivity(intent);

    }

    private void DeleteRecipe(final String savedTableID) {
        alertDialog.setTitle("Delete Recipe");
        alertDialog.setMessage("Do you want to delete this recipe from your device?");
        alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (savedTableID != null){
                    recipeDB.deleteRecipe(savedTableID);
                    finish();
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", null);
        alertDialog.show();
    }

    private void SetupEditAndFinishLaterButton() {
        if (editFinishLater == 1){
            publishLater.setText("Edit and Finish Later");
            deleteRecipeButton.setVisibility(View.VISIBLE);
        }
        else{
            deleteRecipeButton.setVisibility(View.GONE);
        }
    }

    private void PublishLater(String id) {

        String title = NrecipeTitle.getText().toString();
        String description = Ndescription.getText().toString();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Title field is empty", Toast.LENGTH_SHORT).show();
        }

        else{
            if (description.isEmpty()){
                description = " ";
            }
            if (retIngred == null){
                retIngred = new ArrayList<>();
                retIngred.add(" ");
            }
            if(retEquip == null){
                retEquip = new ArrayList<>();
                retEquip.add(" ");
            }
            if (retDir == null){
                retDir = new ArrayList<>();
                retDir.add(" ");
            }

            if (editFinishLater == 0) {
                boolean insertData = recipeDB.addData(title, description, retIngred, retEquip, retDir);

                if (insertData == true) {
                    Toast.makeText(this, "recipe saved to phone", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "recipe did not save", Toast.LENGTH_SHORT).show();
                }
            }

            else if (editFinishLater == 1){
                if (savedTableID.length() > 0){
                    boolean update = recipeDB.updateData(savedTableID, title, description, retIngred, retEquip, retDir);
                    if (update == true){
                        Toast.makeText(this, "Recipe Successfully Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(AddRecipeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddRecipeActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(AddRecipeActivity.this, "Permission to Read Storage Needed", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(AddRecipeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else {
            galleryIntent();
        }

    }

    private void galleryIntent() {
        ///////////////////////////////////
        //Modified 26/07/2019
        final CharSequence[] items = {"Image", "Video", "Remove Image / Video", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipeActivity.this);
        builder.setTitle("Recipe Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (items[i].equals("Image")) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    mediaType = image;
                    startActivityForResult(galleryIntent, Gallery_Pick);
                } else if (items[i].equals("Video")) {
                    Intent galleryVideoIntent = new Intent();
                    galleryVideoIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryVideoIntent.setType("video/*");
                    mediaType = video;
                    startActivityForResult(galleryVideoIntent, Gallery_Video_Pick);
                } else if (items[i].equals("Remove Image / Video")) {
                    constraintLayoutVideo.setVisibility(View.GONE);
                    selectedVideo.equals("");
                    recipePhoto.setVisibility(View.VISIBLE);
                    if (recipeImage != null) {
                        recipeImage.equals(null);
                    }
                    recipePhoto.setImageDrawable(getResources().getDrawable(R.drawable.icon_image));
                    mediaType = 0;
                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }

        });
        builder.show();
        //////////////////////////////////

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleryIntent();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            recipePhoto.setVisibility(View.VISIBLE);
            constraintLayoutVideo.setVisibility(View.GONE);
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
                //recipePhoto.setImageBitmap(rotatedBitmap);

                Uri temp = getImageUri(this, rotatedBitmap);
                recipePhoto.setImageURI(temp);
                recipeImage = temp.toString();
            }
            else {
                recipePhoto.setImageURI(selectedImage);
                recipeImage = selectedImage.toString();
            }
        }
        //////////////////////
        //Modified 26/07/2019
        else if (requestCode == Gallery_Video_Pick && resultCode == RESULT_OK && data != null){
            recipePhoto.setVisibility(View.INVISIBLE);
            constraintLayoutVideo.setVisibility(View.VISIBLE);
            selectedVideo = data.getData();

            try{
                long videoSize = FileSize(selectedVideo);
               // Toast.makeText(this, "Video Size: " + videoSize, Toast.LENGTH_SHORT).show();
                if (videoSize <= 250){
                    recipeVideo.setVideoURI(selectedVideo);
                    recipeVideo.pause();
                    recipeVideo.seekTo(50); //To show first frame of video at 50 ms
                }
                else {
                    Toast.makeText(this, "Video must be less than 250MB", Toast.LENGTH_SHORT).show();
                    selectedVideo = null;
                }

            }catch (Exception e){
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
            //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        }
        ///////////////////////////
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


    private long FileSize(Uri mediaUri){
        long dataSize = 0;
        Cursor returnCursor = getContentResolver().query(mediaUri,null,null,null,null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        dataSize = returnCursor.getLong(sizeIndex);

        return dataSize /(1024 * 1024);
    }

    private void ValidateAddRecipeActivityInfo() {
        String title, description;
        title = NrecipeTitle.getText().toString();
        description = Ndescription.getText().toString();
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Title field is empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Description field is empty", Toast.LENGTH_SHORT).show();
        }
        else {
            if (recipeImage == null && selectedVideo == null){
                alertDialog.setTitle("Recipe image");
                alertDialog.setMessage("There is no image or video for your recipe. Would you like to continue?");
                alertDialog.setNegativeButton("Cancel",null);
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.setTitle("Add New Recipe");
                        progressDialog.setMessage("Please wait, while we're creating your recipe");
                        progressDialog.show();
                        progressDialog.setCanceledOnTouchOutside(true);
                        if (mediaType == image){
                            SaveImageToFirebaseStorage();
                        }
                        else if (mediaType == video){
                            SaveVideoToFirebaseStorage();
                        }

                    }
                });
                alertDialog.create();
                alertDialog.show();
            }
            else {
                progressDialog.setTitle("Add New Recipe");
                progressDialog.setMessage("Please wait, while we're creating your recipe");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);
                if (mediaType == image){
                    SaveImageToFirebaseStorage();
                }
                else if (mediaType == video){
                    SaveVideoToFirebaseStorage();
                }
                //SaveImageToFirebaseStorage();       //will work once the image gallery task is complete
            }
        }
    }

    private void SaveImageToFirebaseStorage() {         //Will work once image gallery task is complete
        Calendar callForDate = Calendar.getInstance();                          //to give the image a unique id using the date and time
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime =currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        Calendar callForDateStamp = Calendar.getInstance();
        SimpleDateFormat dateStampDate = new SimpleDateFormat("yyyy-MM-dd");
        String dateStampTemp = dateStampDate.format(callForDateStamp.getTime());

        Calendar callForTimeStamp = Calendar.getInstance();
        SimpleDateFormat timeStampTime = new SimpleDateFormat("HH:mm:ss");
        String timeStampTemp = timeStampTime.format(callForTimeStamp.getTime());
        timestamp = dateStampTemp + timeStampTemp;

        if (recipeImage != null){
            ImageUri = Uri.parse(recipeImage);
            recipePhoto.setImageURI(ImageUri);

            final StorageReference filePath = PostedImageReference.child("Recipe Main Image").child(ImageUri.getLastPathSegment() + current_user_id + postRandomName + ".jpg");
            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                Toast.makeText(AddRecipeActivity.this, "Image Uploaded Successfully to storage", Toast.LENGTH_SHORT).show();
                                SavePostInformationToDatabase();
                            }
                        });
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(AddRecipeActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
        else {
            SavePostInformationToDatabase();
        }
    }

    private  void SaveVideoToFirebaseStorage(){
        Calendar callForDate = Calendar.getInstance();                          //to give the image a unique id using the date and time
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime =currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        Calendar callForDateStamp = Calendar.getInstance();
        SimpleDateFormat dateStampDate = new SimpleDateFormat("yyyy-MM-dd");
        String dateStampTemp = dateStampDate.format(callForDateStamp.getTime());

        Calendar callForTimeStamp = Calendar.getInstance();
        SimpleDateFormat timeStampTime = new SimpleDateFormat("HH:mm:ss");
        String timeStampTemp = timeStampTime.format(callForTimeStamp.getTime());
        timestamp = dateStampTemp + timeStampTemp;

        if (recipeVideo != null){
            ImageUri = selectedVideo;


            final StorageReference filePath = PostedImageReference.child("Recipe Main Video").child(ImageUri.getLastPathSegment() + current_user_id + postRandomName + ".mp4");
            filePath.putFile(ImageUri)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.00 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            System.out.println("Uploading Video: " + progress + " % done");
                        }
                    })
                    .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("Upload is paused");
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                Toast.makeText(AddRecipeActivity.this, "Video Uploaded Successfully to storage", Toast.LENGTH_SHORT).show();
                                SavePostInformationToDatabase();
                            }
                        });
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(AddRecipeActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
        else {
            SavePostInformationToDatabase();
        }
    }

    private void SavePostInformationToDatabase() {
        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String usersName = dataSnapshot.child("username").getValue().toString();
                    //String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String recTitle = NrecipeTitle.getText().toString();
                    String recDescription = Ndescription.getText().toString();
                    final String PostID = current_user_id + timestamp;

                    HashMap postMap = new HashMap();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("title", recTitle);
                    postMap.put("recipeMainImage", downloadUrl);
                    postMap.put("description", recDescription);
                    postMap.put("Ingredients", retIngred);
                    postMap.put("Equipment", retEquip);
                    postMap.put("Directions", retDir);
                   // postMap.put("profileImage", userProfileImage);
                    postMap.put("username", usersName);
                    postMap.put("timestamp", timestamp);
                    postMap.put("Categories", category); //Needed to update categories section in Posts
                    if (mediaType == video){
                        postMap.put("mediaType", "video");
                    }
                    postRef.child(PostID).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                UpdateFriendsPost(PostID, timestamp);
                                SendUserToMainActivity();
                                Toast.makeText(AddRecipeActivity.this, "Recipe uploaded successfully ", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                if (category != null) {  //To update categories section in Categories.
                                    progressDialog.setTitle("Add New Recipe");
                                    progressDialog.setMessage("Saving categories");
                                    progressDialog.show();
                                    progressDialog.setCanceledOnTouchOutside(true);
                                    SaveCategoriesInformationToDatabase(PostID);
                                }
                            }
                            else {
                                Toast.makeText(AddRecipeActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                    if (savedRecipeTracker == 1) {
                        savedRecipeRef.child(current_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AddRecipeActivity.this, "Unfinished recipe removed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddRecipeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void UpdateFriendsPost(final String postID, final String timestamp) {

        friendsPostRef.child(current_user_id).child(postID).child("timestamp").setValue(timestamp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddRecipeActivity.this, "FriendsPostRefUpdated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        friendsRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        String friendID = dataSnapshot1.getKey();
                        friendsPostRef.child(friendID).child(postID).child("timestamp").setValue(timestamp);
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void SaveCategoriesInformationToDatabase(String postID) {

        for (int i=0 ; i < category.size(); i++){
            CategoriesRef.child(category.get(i)).child(postID).child("category").setValue(category.get(i)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(AddRecipeActivity.this, "Categories uploaded successfully ", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        Toast.makeText(AddRecipeActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(AddRecipeActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


    private void showPopup(int option){

        Intent intent = new Intent(AddRecipeActivity.this, AddPopUp.class);                     //Sends all these values to the next activity
        intent.putExtra("OPTION", option);
        intent.putExtra("TITLE", title);
        intent.putExtra("RECIPEIMAGE", recipeImage);
        intent.putExtra("DESCRIPTION", description);
        intent.putStringArrayListExtra("INGREDIENTS", (ArrayList<String>) retIngred);//ingredient);
        intent.putStringArrayListExtra("EQUIPMENT", (ArrayList<String>) retEquip);//equipments);
        intent.putStringArrayListExtra("DIRECTIONS", (ArrayList<String>) retDir);
        intent.putStringArrayListExtra("CATEGORY", (ArrayList<String>) category);
        intent.putExtra("SavedRecipeTracker", savedRecipeTracker);
        intent.putExtra("EDIT", editFinishLater);
        intent.putExtra("TABLE_ID", savedTableID);

        AddRecipeActivity.this.startActivity(intent);                                       //Go to the AddPopUp Activity
    }

    private void showDirectionsPopup(){

        Intent intent = new Intent(AddRecipeActivity.this, AddDirections.class);         //Sends all these values to the next activity;
        intent.putExtra("TITLE", title);
        intent.putExtra("RECIPEIMAGE", recipeImage);
        intent.putExtra("DESCRIPTION", description);
        intent.putStringArrayListExtra("INGREDIENTS", (ArrayList<String>) retIngred);//ingredient);
        intent.putStringArrayListExtra("EQUIPMENT", (ArrayList<String>) retEquip);//equipments);
        intent.putStringArrayListExtra("DIRECTIONS", (ArrayList<String>) retDir);
        intent.putStringArrayListExtra("CATEGORY", (ArrayList<String>) category);
        intent.putExtra("SavedRecipeTracker", savedRecipeTracker);
        intent.putExtra("EDIT", editFinishLater);
        intent.putExtra("TABLE_ID", savedTableID);

        AddRecipeActivity.this.startActivity(intent);
    }

    private void AddCategories() {
        Intent intent = new Intent(AddRecipeActivity.this, CategoriesActivity.class);         //Sends all these values to the next activity;
        intent.putExtra("TITLE", title);
        intent.putExtra("RECIPEIMAGE", recipeImage);
        intent.putExtra("DESCRIPTION", description);
        intent.putStringArrayListExtra("INGREDIENTS", (ArrayList<String>) retIngred);//ingredient);
        intent.putStringArrayListExtra("EQUIPMENT", (ArrayList<String>) retEquip);//equipments);
        intent.putStringArrayListExtra("DIRECTIONS", (ArrayList<String>) retDir);
        intent.putStringArrayListExtra("CATEGORY", (ArrayList<String>) category);
        intent.putExtra("CLASS", 1);
        intent.putExtra("SavedRecipeTracker", savedRecipeTracker);
        intent.putExtra("EDIT", editFinishLater);
        intent.putExtra("TABLE_ID", savedTableID);

        AddRecipeActivity.this.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddRecipeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //Clears current activity from memory.
        startActivity(intent);
        finish();

        super.onBackPressed();
    }
}
