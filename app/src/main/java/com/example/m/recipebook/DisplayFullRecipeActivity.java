package com.example.m.recipebook;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.m.recipebook.Controller.StarController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DisplayFullRecipeActivity extends AppCompatActivity {
    String CurrentUserID;

    private FirebaseAuth mAuth;
    private DatabaseReference UserProfile, PostRef, LikesRef, CommentsStatusRef;

    private TextView recipeTitle, recipeDescription,dispIngredients, dispEquipment, dispDirections,numberOfStars, numberOfComments, usersname;
    private List<String> ingredients = new ArrayList<String>();
    private List<String> equipment = new ArrayList<String>();
    private List<String> directions = new ArrayList<String>();
    private ImageView recImage;
    private ImageButton stars,comments, playButton;
    private String PostKey, mediaType = "";;
    private Boolean likeChecker;
    private LinearLayout llIngredients, llEquipment, llMethod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_full_recipe);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarFullRecipe);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        CommentsStatusRef = FirebaseDatabase.getInstance().getReference().child("CommentsStatus");
        UserProfile = FirebaseDatabase.getInstance().getReference().child("Users");

        recipeTitle = (TextView) findViewById(R.id.TV_DispTitle);
        recipeDescription = (TextView) findViewById(R.id.TV_DispDescription);
        dispIngredients = (TextView) findViewById(R.id.TV_DispIngredients);
        dispEquipment = (TextView) findViewById(R.id.TV_DispEquipment);
        dispDirections = (TextView) findViewById(R.id.TV_DispDirections);
        recImage = (ImageView) findViewById(R.id.IV_DispRecImage);
        stars = (ImageButton) findViewById(R.id.IB_DispStar);
        numberOfStars = (TextView) findViewById(R.id.TV_DispNumberOfStars);
        comments = (ImageButton) findViewById(R.id.IB_DispComments);
        numberOfComments = (TextView) findViewById(R.id.TV_DispCommentsNumber);
        llIngredients = (LinearLayout) findViewById(R.id.ll_DisplayIngredients);
        llEquipment = (LinearLayout) findViewById(R.id.ll_DisplayEquipment);
        llMethod = (LinearLayout) findViewById(R.id.ll_DisplayMethod);
        usersname = (TextView) findViewById(R.id.TV_DispUsername);
        playButton = (ImageButton) findViewById(R.id.IB_DispPlayButton);



        PostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue().toString();
                String image = "0";
                if ( dataSnapshot.child("recipeMainImage").exists()){
                    image = dataSnapshot.child("recipeMainImage").getValue().toString();
                }
                else{
                    image = "0";
                }
                if (dataSnapshot.child("mediaType").exists()){  //todo edited 30/07/2019
                    mediaType = dataSnapshot.child("mediaType").getValue().toString();
                }
                String desc = dataSnapshot.child("description").getValue().toString();

                int noIngred = (int) dataSnapshot.child("Ingredients").getChildrenCount();
                int noEquipment = (int) dataSnapshot.child("Equipment").getChildrenCount();
                int noDirections = (int) dataSnapshot.child("Directions").getChildrenCount();
                final String userID = dataSnapshot.child("uid").getValue().toString();

                UserProfile.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String name = dataSnapshot.child(userID).child("username").getValue().toString();
                            usersname.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                String temp = null;
                if (noIngred != 0) {

                    for (int i = 0; i < noIngred; i++) {//sorts data in ingredients database to ArrayList

                        temp = String.valueOf(i);
                        String ingred = dataSnapshot.child("Ingredients").child(temp).getValue().toString();
                        ingredients.add(ingred);
                    }

                    if (!ingredients.equals(null)) {
                        for (int i = 0; i < ingredients.size(); i++) {
                            dispIngredients.setText(dispIngredients.getText() + "\u2022 " + ingredients.get(i) + "\n");
                        }
                    }
                }
                else {
                    llIngredients.setVisibility(View.GONE);
                }

                if (noEquipment != 0 ) {
                    for (int i = 0; i < noEquipment; i++) {//sorts data in ingredients database to ArrayList

                        temp = String.valueOf(i);
                        String equip = dataSnapshot.child("Equipment").child(temp).getValue().toString();
                        equipment.add(equip);
                    }

                    if (equipment != null) {
                        for (int i = 0; i < equipment.size(); i++) {
                            dispEquipment.setText(dispEquipment.getText() + "\u2022 " + equipment.get(i) + "\n");
                        }
                    }
                }
                else {
                    llEquipment.setVisibility(View.GONE);
                }

                if (noDirections != 0) {

                    for (int i = 0; i < noDirections; i++) {//sorts data in ingredients database to ArrayList

                        temp = String.valueOf(i);
                        String direct = dataSnapshot.child("Directions").child(temp).getValue().toString();
                        directions.add(direct);
                    }

                    if (directions != null) {
                        for (int i = 0; i < directions.size(); i++) {
                            dispDirections.setText(dispDirections.getText() + directions.get(i) + "\n" + "\n");
                        }
                    }
                }
                else{
                    llMethod.setVisibility(View.GONE);
                }

                recipeTitle.setText(title);
                recipeDescription.setText(desc);
                if (!image.equals("0")){
                    Glide.with(DisplayFullRecipeActivity.this).load(image).into(recImage);
                }
                else{
                    recImage.setVisibility(View.GONE);
                }
                if (mediaType.equals("video")){  //todo edited 30/07/2019
                    playButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        stars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddRemoveStar();
            }
        });
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddComment();
            }
        });
        usersname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewUsersProfile();
            }
        });

        DisplayNumberOfStars();
        DisplayNumberOfComments();

        recImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayFullScreen();
            }
        });
    }

    private void DisplayFullScreen() {
        if (mediaType.equals("video")){

        }
    }

    private void ViewUsersProfile() {

        PostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String uid = dataSnapshot.child("uid").getValue().toString();
                    if (uid.equals(CurrentUserID)){
                        Intent profileIntent = new Intent(DisplayFullRecipeActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                    }
                    else {
                    UserProfile.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                Intent profileIntent = new Intent(DisplayFullRecipeActivity.this, OthersProfileActivity.class);
                                profileIntent.putExtra("visit__user_id", uid);
                                startActivity(profileIntent);
                            }
                            else{
                                Toast.makeText(DisplayFullRecipeActivity.this, "Unable to find users profile", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void AddComment() {
        Intent commentsIntent = new Intent(DisplayFullRecipeActivity.this, CommentsActivity.class);
        commentsIntent.putExtra("PostKey", PostKey);
        startActivity(commentsIntent);
    }

    private void DisplayNumberOfComments() {
        final int[] countComments = new int[1];
        CommentsStatusRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                countComments[0] = (int) dataSnapshot.child(PostKey).getChildrenCount();
                numberOfComments.setText("Comments: " + Integer.toString(countComments[0]));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void DisplayNumberOfStars() {
        final int[] countLikes = new int[1];

        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(PostKey).hasChild(CurrentUserID)){
                    countLikes[0] = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    stars.setImageResource(R.drawable.icon_star_checked);
                    numberOfStars.setText(("Stars: ")+ Integer.toString(countLikes[0]));
                }
                else {
                    countLikes[0] = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    stars.setImageResource(R.drawable.icon_star_unchecked);
                    numberOfStars.setText(("Stars: ")+ Integer.toString(countLikes[0]));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void AddRemoveStar() {

        ///////
        StarController starController = new StarController();

        starController.updateStarCount(DisplayFullRecipeActivity.this, PostRef, LikesRef, CurrentUserID, PostKey);
        ///////////
        /*likeChecker = true;
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (likeChecker.equals(true)) {
                    if (dataSnapshot.child(PostKey).hasChild(CurrentUserID)) {
                        LikesRef.child(PostKey).child(CurrentUserID).removeValue();
                        likeChecker = false;

                        PostRef.child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                    numberOfLikes = numberOfLikes - 1;
                                    String numberLikes = String.valueOf(numberOfLikes);
                                    PostRef.child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(DisplayFullRecipeActivity.this, "Likes updated", Toast.LENGTH_SHORT).show();
                                                return;
                                            } else {
                                                Toast.makeText(DisplayFullRecipeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(DisplayFullRecipeActivity.this, "Does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        LikesRef.child(PostKey).child(CurrentUserID).setValue(true);
                        likeChecker = false;
                        PostRef.child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                    numberOfLikes = numberOfLikes + 1;
                                    String numberLikes = String.valueOf(numberOfLikes);
                                    PostRef.child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(DisplayFullRecipeActivity.this, "Likes updated", Toast.LENGTH_SHORT).show();
                                                return;
                                            } else {
                                                Toast.makeText(DisplayFullRecipeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    PostRef.child("likesnumber").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(DisplayFullRecipeActivity.this, "Star uploaded successfully", Toast.LENGTH_SHORT).show();
                                                likeChecker = false;
                                                return;
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }
}
