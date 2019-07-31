package com.example.m.recipebook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.m.recipebook.Controller.StarController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class OthersProfileActivity extends AppCompatActivity {

    private DatabaseReference FriendRequestRef, otherUserRef, friendRef, NotificationsRef;
    private DatabaseReference PostRef, LikesRef;
    private Query query;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;
    MultiViewTypeAdapter adapter;
    private RecyclerView profileRecyclerView;
    private AlertDialog.Builder alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarOthersProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mAuth = FirebaseAuth.getInstance();

        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visit__user_id").toString();
        otherUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uid").equalTo(receiverUserId);
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef =  FirebaseDatabase.getInstance().getReference().child("Likes");

        profileRecyclerView = (RecyclerView) findViewById(R.id.OthersActivityRecyclerView);

        alertDialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        CURRENT_STATE = ("not_friends");
        LoadProfile();
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

    private void LoadProfile() {

        final ArrayList<ProfileHeaderModel> list = new ArrayList<>();

        otherUserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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
                    adapter.notifyDataSetChanged();
                }
                GetRecipes(list);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new MultiViewTypeAdapter(list,OthersProfileActivity.this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OthersProfileActivity.this,OrientationHelper.VERTICAL,false);
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
                    String status = "";
                    if (dataSnapshot1.child("Status").exists()){
                        status = dataSnapshot1.child("Status").getValue().toString();
                    }
                    String recipeImage = "0";
                    if ( dataSnapshot1.child("recipeMainImage").exists()){
                        recipeImage = value.getRecipeMainImage();
                    }
                    else{
                        recipeImage = "0";
                    }
                    String time = value.getTime();
                    String date = value.getDate();

                    if (!status.equals("suspended")) {
                        all.setTitle(title);
                        all.setDescription(description);
                        all.setRecipeMainImage(recipeImage);
                        all.setTime(time);
                        all.setDate(date);
                        all.setPostKey(dataSnapshot1.getKey());

                        list.add(1, all);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OthersProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class MultiViewTypeAdapter extends RecyclerView.Adapter {

        private ArrayList<ProfileHeaderModel> dataSet;
        Context mContext;
        int total_types;

        public class ProfileHeader extends RecyclerView.ViewHolder {
            TextView profileUserName, profileUserInfo;
            ImageView profileUserPicture;
            Button sendFriendRequestButton1, removeFriendRequestButton1;

            public ProfileHeader(View itemView) {
                super(itemView);

                this.profileUserName = (TextView) itemView.findViewById(R.id.TV_OthersProfileUsername);
                this.profileUserInfo = (TextView) itemView.findViewById(R.id.TV_OthersProfileInfo);
                this.profileUserPicture = (ImageView) itemView.findViewById(R.id.IV_OtherUserProfilePicture);
                this.sendFriendRequestButton1 = (Button) itemView.findViewById(R.id.B_OthersAddFriend2);
                this.removeFriendRequestButton1 = (Button) itemView.findViewById(R.id.B_OthersRemoveFriendRequest2);

            }
        }

        public class ProfileRecipes extends RecyclerView.ViewHolder {
            TextView postTitle, postRecipeDescription, postDate, postTime;
            ImageView postRecipeImage;
            ImageButton likePostButton, commentPostButton;
            TextView displayNumberOfLikes, displayNumberOfComments;
            DatabaseReference PostRef, LikesRef, CommentsStatusRef;
            String CurrentUserID;
            StarController starController = new StarController();

            public ProfileRecipes(View itemView) {
                super(itemView);

                this.postTitle = (TextView) itemView.findViewById(R.id.TV_ProfileTitle);
                this.postRecipeDescription = (TextView) itemView.findViewById(R.id.TV_ProfileDesc);
                this.postRecipeImage = (ImageView) itemView.findViewById(R.id.IV_ProfileRecImage);
                this.postDate = (TextView) itemView.findViewById(R.id.TV_ProfileDate);
                this.postTime = (TextView) itemView.findViewById(R.id.TV_ProfileTime);
                this.likePostButton = (ImageButton) itemView.findViewById(R.id.IB_ProfileLikeButton);
                this.commentPostButton = (ImageButton) itemView.findViewById(R.id.IB_ProfilePostMessage);
                this.displayNumberOfLikes = (TextView) itemView.findViewById(R.id.TV_ProfileNumberOfLikes);
                this.displayNumberOfComments = (TextView) itemView.findViewById(R.id.TV_ProfileComments);

                PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
                LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                CommentsStatusRef = FirebaseDatabase.getInstance().getReference().child("CommentsStatus");
                CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }


        public MultiViewTypeAdapter(ArrayList<ProfileHeaderModel> data, Context context) {
            this.dataSet = data;
            this.mContext = context;
            total_types = dataSet.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            switch (viewType) {
                case ProfileHeaderModel.PROFILE_HEADER_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_othersprofile_header, parent, false);
                    return new ProfileHeader(view);

                case ProfileHeaderModel.PROFILE_RECIPE_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_list, parent, false);
                    return new ProfileRecipes(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            switch (dataSet.get(position).type) {
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

            if (object != null) {
                switch (object.type) {
                    case ProfileHeaderModel.PROFILE_HEADER_TYPE:
                        ((ProfileHeader) holder).profileUserName.setText(object.username);
                        ((ProfileHeader) holder).profileUserInfo.setText(object.userInfo);
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                        if (object.profileImage != "0") {
                            Glide.with(mContext).load(object.profileImage).apply(requestOptions).into(((ProfileHeader) holder).profileUserPicture);
                        }
                        else {
                            Drawable image = (Drawable)getResources().getDrawable(R.drawable.icon_person);
                            ((ProfileHeader) holder).profileUserPicture.setImageDrawable(image);
                        }

                        InitialiseButtons(holder);
                        MaintenanceOfButtons(holder);
                        break;

                    case ProfileHeaderModel.PROFILE_RECIPE_TYPE:
                        ((ProfileRecipes) holder).postTitle.setText(object.getTitle());
                        ((ProfileRecipes) holder).postRecipeDescription.setText(object.getDescription());
                        if(object.recipeMainImage != "0") {
                            Glide.with(mContext).load(object.recipeMainImage).into(((ProfileRecipes) holder).postRecipeImage);
                        }
                        else {
                            ((ProfileRecipes) holder).postRecipeImage.setVisibility(View.GONE);
                        }
                        ((ProfileRecipes) holder).postDate.setText(object.getDate());
                        ((ProfileRecipes) holder).postTime.setText(object.getTime());

                        ((ProfileRecipes) holder).commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent = new Intent(OthersProfileActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey", object.getPostKey());
                                startActivity(commentsIntent);
                            }
                        });

                        ((ProfileRecipes) holder).LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(object.getPostKey()).hasChild(((ProfileRecipes) holder).CurrentUserID)) {
                                    int countLikes = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                    ((ProfileRecipes) holder).likePostButton.setImageResource(R.drawable.icon_star_checked);
                                    ((ProfileRecipes) holder).displayNumberOfLikes.setText(Integer.toString(countLikes));
                                } else {
                                    int countLikes = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                    ((ProfileRecipes) holder).likePostButton.setImageResource(R.drawable.icon_star_unchecked);
                                    ((ProfileRecipes) holder).displayNumberOfLikes.setText(Integer.toString(countLikes));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        ((ProfileRecipes) holder).likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((ProfileRecipes) holder).starController.updateStarCount(OthersProfileActivity.this, PostRef, LikesRef, senderUserId, object.getPostKey());
                            }
                        });

                        ((ProfileRecipes) holder).CommentsStatusRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int countComments = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                ((ProfileRecipes) holder).displayNumberOfComments.setText(Integer.toString(countComments));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ViewRecipeIntent(object.getPostKey());
                            }
                        });
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }


        private void MaintenanceOfButtons(final RecyclerView.ViewHolder holder) {
            FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(receiverUserId)) {
                        String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                        if (request_type.equals("sent")) {
                            CURRENT_STATE = "request_sent";
                            ((ProfileHeader) holder).sendFriendRequestButton1.setText("Cancel Follow Request");

                            ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
                            ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(false);
                        } else if (request_type.equals("received")) {
                            CURRENT_STATE = "request_received";
                            ((ProfileHeader) holder).sendFriendRequestButton1.setText("Accept Follow Request");

                            ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.VISIBLE);
                            ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(true);

                            ((ProfileHeader) holder).removeFriendRequestButton1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CancelFriendRequest1(holder);
                                }
                            });

                        }
                    } else {
                        friendRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(receiverUserId)) {
                                    CURRENT_STATE = "friends";
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setText("Following");
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void InitialiseButtons(final RecyclerView.ViewHolder holder) {
            ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
            ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(false);

            if (!senderUserId.equals(receiverUserId)) {
                ((ProfileHeader) holder).sendFriendRequestButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ProfileHeader) holder).sendFriendRequestButton1.setEnabled(false);

                        if (CURRENT_STATE.equals("not_friends")) {
                            SendFriendRequestTopPerson1(holder);

                        }

                        if (CURRENT_STATE.equals("request_sent")){
                            alertDialog.setTitle("Cancel Request");
                            alertDialog.setMessage("Are you sure you would like to cancel the request to follow?");
                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CancelFriendRequest1(holder);
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setVisibility(View.VISIBLE);
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setEnabled(true);
                                }
                            });
                            alertDialog.create();
                            alertDialog.show();

                        }

                        if (CURRENT_STATE.equals("request_received")) {
                            AcceptFriendRequest1(holder);
                        }
                        if (CURRENT_STATE.equals("friends")) {
                            alertDialog.setTitle("Unfollow Profile");
                            alertDialog.setMessage("Are you sure you would like to stop following this profile?");
                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    UnFriendAnExistingFriend1(holder);
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", null).create();
                            alertDialog.show();
                        }
                    }
                });
            } else {
                ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
                ((ProfileHeader) holder).sendFriendRequestButton1.setVisibility(View.INVISIBLE);
            }
        }

        private void UnFriendAnExistingFriend1(final RecyclerView.ViewHolder holder) {
            friendRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        friendRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setEnabled(true);
                                    CURRENT_STATE = "not_friends";
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setText("Send Follow Request");
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(false);
                                }
                            }
                        });
                    }
                }
            });
        }

        private void AcceptFriendRequest1(final RecyclerView.ViewHolder holder) {
            Calendar callForDate = Calendar.getInstance();                          //to give the image a unique id using the date and time
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate =currentDate.format(callForDate.getTime());

            friendRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        friendRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    FriendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                FriendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            ((ProfileHeader) holder).sendFriendRequestButton1.setEnabled(true);
                                                            CURRENT_STATE = "friends";
                                                            ((ProfileHeader) holder).sendFriendRequestButton1.setText("Following");
                                                            ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
                                                            ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(false);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        private void CancelFriendRequest1(final RecyclerView.ViewHolder holder) {
            FriendRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        FriendRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setEnabled(true);
                                    CURRENT_STATE = "not_friends";
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setText("Send Follow Request");
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(false);
                                }
                            }
                        });
                        NotificationsRef.child("FriendRequests").child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(OthersProfileActivity.this, "Notification removed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        private void SendFriendRequestTopPerson1(final RecyclerView.ViewHolder holder) {
            FriendRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        FriendRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setEnabled(true);
                                    CURRENT_STATE = "request_sent";
                                    ((ProfileHeader) holder).sendFriendRequestButton1.setText("Cancel Follow Request");
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setVisibility(View.INVISIBLE);
                                    ((ProfileHeader) holder).removeFriendRequestButton1.setEnabled(false);
                                }
                            }
                        });
                    }
                }
            });
            NotificationsRef.child("FriendRequests").child(receiverUserId).child(senderUserId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(mContext, "Notification sent", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void ViewRecipeIntent(String postKey) {
        Intent clickPostIntent = new Intent(OthersProfileActivity.this, DisplayFullRecipeActivity.class);
        clickPostIntent.putExtra("PostKey", postKey);
        startActivity(clickPostIntent);
    }

}
