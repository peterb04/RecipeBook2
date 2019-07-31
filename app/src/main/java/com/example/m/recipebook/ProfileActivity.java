package com.example.m.recipebook;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private Query query, categoriesQuery;

    private DatabaseReference userRef, postsRef, userId, commentRef, likesRef;
    private StorageReference ProfileImageRef;
    private FirebaseAuth mAuth;
    String CurrentUserID;
    MultiViewTypeAdapter adapter;
    private RecyclerView profileRecyclerView;

    private AlertDialog.Builder alertDialog;
    private ProgressDialog progressDialog;

    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String recipeImage;

    private ShareDialog shareDialog;
    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID);
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");//.child(CurrentUserID);
        ProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        commentRef = FirebaseDatabase.getInstance().getReference().child("CommentsStatus");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        userId= FirebaseDatabase.getInstance().getReference().child("Posts");
        query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uid").equalTo(CurrentUserID);
        categoriesQuery = FirebaseDatabase.getInstance().getReference().child("Categories");

        setContentView(R.layout.activity_profile);

        alertDialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        progressDialog = new ProgressDialog(this, R.style.MyDialogTheme);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(ProfileActivity.this, "Success Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ProfileActivity.this, "Unable to share to facebook", Toast.LENGTH_SHORT).show();
            }
        });

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        profileRecyclerView = (RecyclerView) findViewById(R.id.ProfileActivityRecyclerView);

        DisplayAllofCurrentUsersPost();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void DisplayAllofCurrentUsersPost() {
        ////

        final ArrayList<ProfileHeaderModel> list = new ArrayList<>();
        // list.add(new ProfileHeaderModel(ProfileHeaderModel.PROFILE_HEADER_TYPE));


        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    ProfileHeaderModel all = new ProfileHeaderModel(ProfileHeaderModel.PROFILE_HEADER_TYPE,0,"","","","","","","","","","","");
                    String name = dataSnapshot.child("username").getValue().toString();
                    String userDescription = dataSnapshot.child("userInfo").getValue().toString();
                    String profilePicture = "0";
                    if(dataSnapshot.child("profileimage").exists()) {
                        profilePicture = dataSnapshot.child("profileimage").getValue().toString();
                    }
                    else{
                        profilePicture = "0";
                    }

                    all.setUsername(name);
                    all.setUserInfo(userDescription);
                    all.setProfileImage(profilePicture);

                    list.add(all);
                }
                adapter.notifyDataSetChanged();
                GetRecipes(list);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new MultiViewTypeAdapter(list,ProfileActivity.this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProfileActivity.this,OrientationHelper.VERTICAL,false);
        profileRecyclerView.setLayoutManager(linearLayoutManager);
        profileRecyclerView.setItemAnimator(new DefaultItemAnimator());
        profileRecyclerView.setAdapter(adapter);
    }

    private void GetRecipes(final ArrayList<ProfileHeaderModel> list) {

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    ProfileHeaderModel value = dataSnapshot1.getValue(ProfileHeaderModel.class);
                    ProfileHeaderModel all = new ProfileHeaderModel(ProfileHeaderModel.PROFILE_RECIPE_TYPE,0,"","","","","","","","","","","");
                    String title = value.getTitle();
                    String description = value.getDescription();
                    String recipeImage = "0";
                    if ( dataSnapshot1.child("recipeMainImage").exists()){
                        recipeImage = value.getRecipeMainImage();
                    }
                    else{
                        recipeImage = "0";
                    }
                    String time = value.getTime();
                    String date = value.getDate();

                    all.setTitle(title);
                    all.setDescription(description);
                    all.setRecipeMainImage(recipeImage);
                    all.setTime(time);
                    all.setDate(date);
                    all.setPostKey(dataSnapshot1.getKey());

                    list.add(1, all);
                }
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ViewRecipeIntent(String postKey) {
        Intent clickPostIntent = new Intent(ProfileActivity.this, DisplayFullRecipeActivity.class);
        clickPostIntent.putExtra("PostKey", postKey);
        startActivity(clickPostIntent);
    }

    private void EditRecipeIntent(String postKey) {
        Intent clickPostIntent = new Intent(ProfileActivity.this, EditPostActivity.class);
        clickPostIntent.putExtra("PostKey", postKey);
        startActivity(clickPostIntent);
    }

    private void DeleteRecipeIntent(final String postKey) {
        final List<String> categories = new ArrayList<String>();

            progressDialog.setTitle("Deleting Recipe");
            progressDialog.setMessage("Your recipe is now being deleted...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            postsRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String temp;
                        int noCategories = (int) dataSnapshot.child("Categories").getChildrenCount();
                        if (noCategories != 0) {
                            for (int i = 0; i < noCategories; i++) {
                                temp = String.valueOf(i);
                                String cat = dataSnapshot.child("Categories").child(temp).getValue().toString();
                                categories.add(cat);

                            }
                            categoriesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (int i = 0; i < categories.size(); i++) {
                                            String check = dataSnapshot.child(categories.get(i)).child(postKey).getKey();
                                            if (check == postKey) {
                                                dataSnapshot.child(categories.get(i)).child(postKey).getRef().removeValue();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        dataSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfileActivity.this, "Recipe has been deleted", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                finish();
                                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled (@NonNull DatabaseError databaseError){

                }

            });

            commentRef.child(postKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ProfileActivity.this, "Comments status deleted", Toast.LENGTH_SHORT).show();
                }
            });

            likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(postKey)){
                        dataSnapshot.child(postKey).getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    public class MultiViewTypeAdapter extends RecyclerView.Adapter {

        private ArrayList<ProfileHeaderModel> dataSet;
        Context mContext;
        int total_types;


        public  class ProfileHeader extends RecyclerView.ViewHolder{
            TextView profileUserName, profileUserInfo;
            ImageView profileUserPicture;

            public ProfileHeader(View itemView){
                super(itemView);

                this.profileUserName = (TextView)itemView.findViewById(R.id.TV_ProfileUsername);
                this.profileUserInfo = (TextView)itemView.findViewById(R.id.TV_ProfileInfo);
                this.profileUserPicture = (ImageView)itemView.findViewById(R.id.IV_ProfilePicture);
            }
        }

        public  class ProfileRecipes extends RecyclerView.ViewHolder{
            TextView postTitle, postRecipeDescription, postDate, postTime;
            ImageView postRecipeImage;
            ImageButton likePostButton, commentPostButton, shareButton;
            TextView displayNumberOfLikes, displayNumberOfComments;

            DatabaseReference LikesRef, CommentsStatusRef;
            String CurrentUserID;

            public ProfileRecipes(View itemView){
                super(itemView);

                this.postTitle = (TextView)itemView.findViewById(R.id.TV_ProfileTitle);
                this.postRecipeDescription = (TextView)itemView.findViewById(R.id.TV_ProfileDesc);
                this.postRecipeImage = (ImageView)itemView.findViewById(R.id.IV_ProfileRecImage);
                this.postDate = (TextView)itemView.findViewById(R.id.TV_ProfileDate);
                this.postTime = (TextView)itemView.findViewById(R.id.TV_ProfileTime);
                this.likePostButton = (ImageButton) itemView.findViewById(R.id.IB_ProfileLikeButton);
                this.commentPostButton = (ImageButton) itemView.findViewById(R.id.IB_ProfilePostMessage);
                this.displayNumberOfLikes = (TextView) itemView.findViewById(R.id.TV_ProfileNumberOfLikes);
                this.displayNumberOfComments = (TextView)itemView.findViewById(R.id.TV_ProfileComments);
                this.shareButton = (ImageButton) itemView.findViewById(R.id.facebookShareButton);

                LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                CommentsStatusRef = FirebaseDatabase.getInstance().getReference().child("CommentsStatus");
                CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }


        public MultiViewTypeAdapter(ArrayList<ProfileHeaderModel>data, Context context){
            this.dataSet = data;
            this.mContext = context;
            total_types = dataSet.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            switch (viewType){
                case ProfileHeaderModel.PROFILE_HEADER_TYPE:
                    view=LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_header,parent,false);
                    return new ProfileHeader(view);

                case ProfileHeaderModel.PROFILE_RECIPE_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_list,parent,false);
                    return new ProfileRecipes(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            switch (dataSet.get(position).type){
                case 0:
                    return ProfileHeaderModel.PROFILE_HEADER_TYPE;

                case 1:
                    return ProfileHeaderModel.PROFILE_RECIPE_TYPE;

                default:
                    return -1;
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            final ProfileHeaderModel object = dataSet.get(position);

            if (object != null){
                switch (object.type){
                    case ProfileHeaderModel.PROFILE_HEADER_TYPE:
                        ((ProfileHeader)holder).profileUserName.setText(object.username);
                        ((ProfileHeader)holder).profileUserInfo.setText(object.userInfo);

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                        if (!object.profileImage.equals("0")) {
                            Glide.with(mContext).load(object.profileImage).apply(requestOptions).into(((ProfileHeader) holder).profileUserPicture);
                        }
                        else {
                            Drawable image = (Drawable)getResources().getDrawable(R.drawable.icon_person);
                            ((ProfileHeader) holder).profileUserPicture.setImageDrawable(image);
                        }
                        ((ProfileHeader)holder).profileUserPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditImageRoutine(object.profileImage);
                            }
                        });

                        ((ProfileHeader)holder).profileUserName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditUserNameRoutine(object.username);
                            }
                        });

                        ((ProfileHeader)holder).profileUserInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditUserDescritpionRoutine(object.userInfo);
                            }
                        });

                        break;

                    case ProfileHeaderModel.PROFILE_RECIPE_TYPE:
                        ((ProfileRecipes)holder).postTitle.setText(object.getTitle());
                        ((ProfileRecipes)holder).postRecipeDescription.setText(object.getDescription());
                        if(!object.recipeMainImage.equals("0")) {
                            Glide.with(mContext).load(object.recipeMainImage).into(((ProfileRecipes) holder).postRecipeImage);
                        }
                        else {
                            ((ProfileRecipes) holder).postRecipeImage.setVisibility(View.GONE);
                        }
                        //Picasso.get().load(object.recipeMainImage).into(((ProfileRecipes)holder).postRecipeImage);
                        ((ProfileRecipes)holder).postDate.setText(object.getDate());
                        ((ProfileRecipes)holder).postTime.setText(object.getTime());

                        ((ProfileRecipes)holder).commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent = new Intent(ProfileActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey", object.getPostKey());
                                startActivity(commentsIntent);
                            }
                        });

                        ((ProfileRecipes)holder).LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(object.getPostKey()).hasChild(((ProfileRecipes)holder).CurrentUserID)){
                                    int countLikes = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                    ((ProfileRecipes)holder).likePostButton.setImageResource(R.drawable.icon_star_checked);
                                    ((ProfileRecipes)holder).displayNumberOfLikes.setText(Integer.toString(countLikes));
                                }
                                else {
                                    int countLikes = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                    ((ProfileRecipes)holder).likePostButton.setImageResource(R.drawable.icon_star_unchecked);
                                    ((ProfileRecipes)holder).displayNumberOfLikes.setText(Integer.toString(countLikes));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        ((ProfileRecipes)holder).CommentsStatusRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int countComments = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                ((ProfileRecipes)holder).displayNumberOfComments.setText(Integer.toString(countComments));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final CharSequence[] items = {"View Recipe", "Edit Recipe", "Delete Recipe", "Cancel"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                                builder.setTitle("Recipe Options");
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (items[i].equals("View Recipe")) {
                                            ViewRecipeIntent(object.getPostKey());
                                        } else if (items[i].equals("Edit Recipe")) {
                                            EditRecipeIntent(object.getPostKey());
                                        } else if (items[i].equals("Delete Recipe")) {
                                            alertDialog.setTitle("Delete Recipe");
                                            alertDialog.setMessage("Are you sure you want to delete this recipe?");
                                            alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    postsRef.child(object.getPostKey()).child("recipeMainImage").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()){
                                                                String imageRef = dataSnapshot.getValue().toString();
                                                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageRef);
                                                                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(mContext, "Old image deleted", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(mContext, "Image not deleted", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                    DeleteRecipeIntent(object.getPostKey());
                                                }
                                            });
                                            alertDialog.setNegativeButton("Cancel", null);
                                            alertDialog.show();
                                        } else if (items[i].equals("Cancel")) {
                                            dialogInterface.dismiss();
                                        }
                                    }

                                });
                                builder.show();
                            }
                        });

                        ((ProfileRecipes)holder).shareButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final Dialog dialog = new Dialog(ProfileActivity.this);

                                dialog.setContentView(R.layout.custom_layout_unfinished_recipes);
                                TextView title = (TextView) dialog.findViewById(R.id.TV_customUnfinishedRecipes);
                                title.setText("Share With");
                                        ArrayAdapter<String> addAdapter;
                                ListView lvShareOptions = (ListView) dialog.findViewById(R.id.LV_customUnfinishedRecipes);
                                addAdapter = new ArrayAdapter<>(ProfileActivity.this, android.R.layout.simple_list_item_1);
                                lvShareOptions.setAdapter(addAdapter);

                                addAdapter.add("Facebook");
                                addAdapter.notifyDataSetChanged();
                                /*
                                dialog.setContentView(R.layout.custom_dialog_share);
                                final ShareButton facebookShare = (ShareButton) dialog.findViewById(R.id.dialog_facebookShareButton);
                                dialog.show();


                                        Glide.with(mContext).asBitmap().load(object.recipeMainImage).into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                                SharePhoto sharePhoto = new SharePhoto.Builder()
                                                        .setBitmap(resource)
                                                        .build();

                                                SharePhotoContent content = new SharePhotoContent.Builder()
                                                        .setContentUrl(Uri.parse("https://developers.facebook.com"))
                                                        .addPhoto(sharePhoto)
                                                        .build();
                                                facebookShare.setShareContent(content);
                                            }
                                            });

*/
                                lvShareOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                                            @Override
                                            public void onSuccess(Sharer.Result result) {
                                                Toast.makeText(mContext, "Share Successful", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancel() {
                                                Toast.makeText(mContext, "Share Cancelled", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onError(FacebookException error) {
                                                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        });

                                        Glide.with(mContext).asBitmap().load(object.recipeMainImage).into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                                SharePhoto sharePhoto = new SharePhoto.Builder()
                                                        .setBitmap(resource)
                                                        .build();

                                                if (ShareDialog.canShow(SharePhotoContent.class)){
                                                    SharePhotoContent content = new SharePhotoContent.Builder()
                                                            .addPhoto(sharePhoto)
                                                            .build();
                                                    shareDialog.show(content);
                                                }
                                                else{
                                                    Toast.makeText(ProfileActivity.this, "Unable to share. Facebook app must be installed to share photos", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }

                                            }
                                        });
                                    }
                                });
                                dialog.show();
                            };
                        });
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }
    }

    private void EditUserNameRoutine(String username) {

        String message = "This will edit your username.";
        ShowEditDialog("Edit Username",message,username, 0);

    }

    private void EditUserDescritpionRoutine(String userInfo) {
        String message = "This will edit your user information.";
        ShowEditDialog("Edit User Info",message,userInfo, 1);
    }

    private void EditImageRoutine(final String profileImage) {
        final StorageReference[] PostedImageReference = new StorageReference[1];
        final CharSequence[] items;
        items = new CharSequence[]{"Change Profile Image", "Delete Image", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Profile Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (items[i].equals("Change Profile Image")) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, Gallery_Pick);
                }
                else if (items[i].equals("Delete Image")) {
                    alertDialog.setTitle("Delete Profile Image");
                    alertDialog.setMessage("This will delete your profile image");
                    alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                PostedImageReference[0] = FirebaseStorage.getInstance().getReferenceFromUrl(profileImage);
                                PostedImageReference[0].delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                userRef.child("profileimage").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, "Profile image deleted", Toast.LENGTH_SHORT).show();
                                        recreate();

                                    }
                                });
                            }catch (Exception e){

                            }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();

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
            String message = "This will update your profile image.";
            ShowEditDialog("New Photo", message,"", 2);
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

    private String ShowEditDialog(String title,String message, final String text, final int type) {

        final EditText editText = new EditText(ProfileActivity.this);
        final ImageView imageView = new ImageView(ProfileActivity.this);

        if (type == 0 || type == 1) {

            final  CustomDialogEditText customDialogEditText = new CustomDialogEditText(ProfileActivity.this, title, text);
            customDialogEditText.show();
            customDialogEditText.dialogEditTextOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customDialogEditText.dismiss();
                    String userInput = customDialogEditText.dialogTextEditText.getText().toString();
                    UpdateInfoRoutine(userInput, type );
                }
            });
        }
        else if (type == 2){
            final CustomDialogProfileImage cdp = new CustomDialogProfileImage(ProfileActivity.this, title,message,recipeImage);
            cdp.show();
            cdp.dialogProfileImageOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cdp.dismiss();
                    String newImage = recipeImage;
                    progressDialog.setTitle("Updating Profile Image");
                    progressDialog.setMessage("Updating your profile image...");
                    progressDialog.show();
                    UpdateInfoRoutine(newImage, type);

                }
            });
        }
        return null;
    }

    private void UpdateInfoRoutine(String userInput, int type) {
        String value = null;
        if (type == 0){
            value = "username";
        }
        else if (type == 1){
            value = "userInfo";
        }
        if (type == 0 || type == 1) {
            userRef.child(value).setValue(userInput).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                    }
                    else{
                        Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else if (type == 2){

            ImageUri = Uri.parse(recipeImage);
            final StorageReference filePath = ProfileImageRef.child(CurrentUserID + ".jpg");
            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                    final String[] downloadUrl = {null};
                    if (task.isSuccessful()){
                        Toast.makeText(ProfileActivity.this, "Profile image stored successfuly to firebase storage", Toast.LENGTH_SHORT).show();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl[0] = uri.toString();
                                SaveProfileImageUserNode(downloadUrl[0]);
                            }
                        });
                    }
                    else{
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void SaveProfileImageUserNode(String downloadUrl) {
        userRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ProfileActivity.this, "Profile image stored to firebase database successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                    startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                }
                else {
                    progressDialog.dismiss();
                    String message = task.getException().getMessage();
                    Toast.makeText(ProfileActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            SendUserToLoginActivity();
        }
        else{
            CheckUserExistence();
        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();           //Does not allow the user to come back to this class if the back button is pressed
    }

    private void CheckUserExistence()       // Checks if the user already has an account and brings back their UID from firebase
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)){
                    //SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
