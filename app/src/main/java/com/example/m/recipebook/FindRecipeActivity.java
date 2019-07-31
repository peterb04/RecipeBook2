package com.example.m.recipebook;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FindRecipeActivity extends AppCompatActivity {

    private RecyclerView searchRecipesRecyclerView;
    private Button searchRecipesButton;
    private EditText searchRecipesInput;

    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, CategoriesRef, LikesRef;
    private Query searchRecipeQuery;

    private String category;
    private int Class = 0;
    Boolean likeChecker;
    private String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_recipe);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarRecipe);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search Recipes");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        CategoriesRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        searchRecipesRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewfindRecipes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        searchRecipesRecyclerView.setLayoutManager(linearLayoutManager);
        searchRecipesRecyclerView.setHasFixedSize(true);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        searchRecipesRecyclerView.setLayoutManager(linearLayoutManager);

        Class = getIntent().getIntExtra("CLASS",0);  // if 1, then this activity was called from the categories activity

        searchRecipesButton = (Button) findViewById(R.id.B_SearchRecipes);
        searchRecipesInput = (EditText) findViewById(R.id.ET_FindRecipes);
        searchRecipesButton.setEnabled(false);

        if (Class == 1){
            category = getIntent().getExtras().getString("Category");
            searchRecipesButton.setVisibility(View.GONE);
            searchRecipesInput.setVisibility(View.GONE);
            searchRecipesButton.setEnabled(false);
            SearchForRecipes(category);
        }
        else
        {
            searchRecipesButton.setVisibility(View.VISIBLE);
            searchRecipesInput.setVisibility(View.VISIBLE);
        }


        searchRecipesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    searchRecipesButton.setEnabled(false);
                }
                else{
                    searchRecipesButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchRecipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String SearchRecipeInput = searchRecipesInput.getText().toString();
                SearchForRecipes(SearchRecipeInput);
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

    private void SearchForRecipes(String searchRecipeInput) {

        if (Class == 0) {
            searchRecipeQuery = PostsRef.orderByChild("title").startAt(searchRecipeInput).endAt(searchRecipeInput + "\uf8ff");
        }
        else if (Class == 1){
            searchRecipeQuery = CategoriesRef.child(searchRecipeInput).orderByChild("category").equalTo(searchRecipeInput);
        }
        final FirebaseRecyclerAdapter<Posts, SearchRecipesViewHolder > firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, SearchRecipesViewHolder>
                (
                        Posts.class,
                        R.layout.list_layout,
                        SearchRecipesViewHolder.class,
                        searchRecipeQuery
                )
        {
            @Override
            protected void populateViewHolder(final SearchRecipesViewHolder viewHolder, Posts model, int position) {
                final String postID = getRef(position).getKey();
                PostsRef.child(postID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String status = "";
                            if (dataSnapshot.child("Status").exists()) {
                                status = dataSnapshot.child("Status").getValue().toString();
                            }
                            if (!status.equals("suspended")) {
                                final String username = dataSnapshot.child("username").getValue().toString();
                                String profileImage = null;
                                if (dataSnapshot.child("profileImage").exists()) {
                                    profileImage = dataSnapshot.child("profileImage").getValue().toString();
                                }
                                final String time = dataSnapshot.child("time").getValue().toString();
                                final String date = dataSnapshot.child("date").getValue().toString();
                                final String title = dataSnapshot.child("title").getValue().toString();
                                final String description = dataSnapshot.child("description").getValue().toString();
                                final String recipeImage = dataSnapshot.child("recipeMainImage").getValue().toString();

                                viewHolder.setUsername(username);
                                viewHolder.setProfileImage(getApplicationContext(), profileImage);
                                viewHolder.setTime(time);
                                viewHolder.setDate(date);
                                viewHolder.setTitle(title);
                                viewHolder.setDescription(description);
                                viewHolder.setRecipeMainImage(getApplicationContext(), recipeImage);
                                viewHolder.setLikeStatus(postID);
                                viewHolder.setCommentStatus(postID);

                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent clickPostIntent = new Intent(FindRecipeActivity.this, DisplayFullRecipeActivity.class);
                                        clickPostIntent.putExtra("PostKey", postID);
                                        startActivity(clickPostIntent);
                                    }
                                });

                                viewHolder.linearLayoutProfile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        searchRecipeQuery.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    String userID = dataSnapshot.child(postID).child("uid").getValue().toString();
                                                    Intent profileIntent = new Intent(FindRecipeActivity.this, OthersProfileActivity.class);
                                                    profileIntent.putExtra("visit__user_id", userID);
                                                    startActivity(profileIntent);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });

                                viewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        likeChecker = true;

                                        LikesRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (likeChecker.equals(true)) {
                                                    if (dataSnapshot.child(postID).hasChild(CurrentUserID)) {
                                                        LikesRef.child(postID).child(CurrentUserID).removeValue();
                                                        likeChecker = false;

                                                        PostsRef.child(postID).child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                                                    numberOfLikes = numberOfLikes - 1;
                                                                    String numberLikes = String.valueOf(numberOfLikes);
                                                                    PostsRef.child(postID).child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Toast.makeText(FindRecipeActivity.this, "Likes updated", Toast.LENGTH_SHORT).show();
                                                                                return;
                                                                            } else {
                                                                                Toast.makeText(FindRecipeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                } else {
                                                                    Toast.makeText(FindRecipeActivity.this, "Does not exist", Toast.LENGTH_SHORT).show();
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    } else {
                                                        LikesRef.child(postID).child(CurrentUserID).setValue(true);
                                                        likeChecker = false;
                                                        PostsRef.child(postID).child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                                                    numberOfLikes = numberOfLikes + 1;
                                                                    String numberLikes = String.valueOf(numberOfLikes);
                                                                    PostsRef.child(postID).child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Toast.makeText(FindRecipeActivity.this, "Likes updated", Toast.LENGTH_SHORT).show();
                                                                                return;
                                                                            } else {
                                                                                Toast.makeText(FindRecipeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                } else {
                                                                    PostsRef.child(postID).child("likesnumber").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Toast.makeText(FindRecipeActivity.this, "Star uploaded successfully", Toast.LENGTH_SHORT).show();
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
                                        });
                                    }
                                });

                                viewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent commentsIntent = new Intent(FindRecipeActivity.this, CommentsActivity.class);
                                        commentsIntent.putExtra("PostKey", postID);
                                        startActivity(commentsIntent);
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
        };

        searchRecipesRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class SearchRecipesViewHolder extends RecyclerView.ViewHolder{
        View mView;

        LinearLayout linearLayoutProfile;
        DatabaseReference LikesRef, CommentsStatusRef;
        String currentUserID;
        int countLikes, countComments;
        ImageButton likePostButton, commentPostButton;
        TextView displayNumberOfLikes, displayNumberOfComments;

        public SearchRecipesViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            linearLayoutProfile = mView.findViewById(R.id.LL_MainUserProfile);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            CommentsStatusRef = FirebaseDatabase.getInstance().getReference().child("CommentsStatus");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            likePostButton = (ImageButton) mView.findViewById(R.id.IB_MainLikeButton);
            commentPostButton = (ImageButton) mView.findViewById(R.id.IB_MainPostMessage);
            displayNumberOfLikes = (TextView) mView.findViewById(R.id.TV_MainNumberOfLikes);
            displayNumberOfComments = (TextView) mView.findViewById(R.id.TV_CommentsNumber);
        }

        public void setUsername(String username){
            TextView userN = (TextView) mView.findViewById(R.id.TV_MainUsername);
            userN.setText(username);
        }

        public void setProfileImage(Context ctx, String profileImage){
            ImageView profilePicture = (ImageView) mView.findViewById(R.id.IV_MainUserImage);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
            Glide.with(mView).load(profileImage).apply(requestOptions).into(profilePicture);
        }
        public void setTime(String time){
            TextView postTime = (TextView) mView.findViewById(R.id.TV_MainTime);
            postTime.setText("   " + time);
        }
        public void setDate(String date){
            TextView postDate = (TextView) mView.findViewById(R.id.TV_MainDate);
            postDate.setText("   " + date);
        }
        public void setTitle(String title){
            TextView postTitle = (TextView) mView.findViewById(R.id.TV_Title);
            postTitle.setText(title);
        }
        public void setDescription(String description){
            TextView postDescription = (TextView) mView.findViewById(R.id.TV_Description);
            postDescription.setText(description);
        }
        public void setRecipeMainImage(Context ctx, String recipeMainImage){
            ImageView postRecipeImage = (ImageView) mView.findViewById(R.id.IV_MainPicture);
            Glide.with(mView).load(recipeMainImage).into(postRecipeImage);
        }

        public void setLikeStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserID)){
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.icon_star_checked);
                        displayNumberOfLikes.setText(("Stars: ")+ Integer.toString(countLikes));
                    }
                    else {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.icon_star_unchecked);
                        displayNumberOfLikes.setText(("Stars: ")+ Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setCommentStatus(final String postKey) {

            CommentsStatusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    countComments = (int) dataSnapshot.child(postKey).getChildrenCount();
                    displayNumberOfComments.setText("Comments: " + Integer.toString(countComments));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
