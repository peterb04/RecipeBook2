package com.example.m.recipebook;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private NotificationsPostAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference NotificationsRef, PostRef, UserRef, temp, NotificationFriendReq;
    static String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarNotifications);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();

        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child("Comments").child(CurrentUserID);
        NotificationFriendReq = FirebaseDatabase.getInstance().getReference().child("Notifications").child("FriendRequests").child(CurrentUserID);
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        temp = FirebaseDatabase.getInstance().getReference();

        notificationsRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewNotifications);

        DisplayNotifications();
    }

    private void DisplayNotifications() {

        final ArrayList<Notifications> list = new ArrayList<>();

        NotificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Notifications value = dataSnapshot1.getValue(Notifications.class);
                    Notifications all = new Notifications(Notifications.COMMENT_TYPE, 0, "", "", "", "", "", "", "", "", "","");
                    String PostKey = dataSnapshot1.getKey();
                    String CommenterID = null;
                    for (DataSnapshot id : dataSnapshot.child(PostKey).getChildren()) {
                        CommenterID = id.getKey();
                    }

                    all.setPostKey(PostKey);
                    all.setCommenterID(CommenterID);
                    list.addAll(Collections.singleton(all));
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled (DatabaseError databaseError){

            }
        });

        NotificationFriendReq.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Notifications all = new Notifications(Notifications.FRIEND_REQUEST_TYPE, 0,"","","","","","","","","","");
                    String FriendID = dataSnapshot1.getKey();

                    all.setUid(FriendID);
                    list.addAll(Collections.singleton(all));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new NotificationsPostAdapter(list,NotificationsActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationsActivity.this,OrientationHelper.VERTICAL,false);
        notificationsRecyclerView.setLayoutManager(linearLayoutManager);
        notificationsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        notificationsRecyclerView.setAdapter(adapter);
    }

    private void PostIntent(String postKey) {

        NotificationsRef.child(postKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(NotificationsActivity.this, "Notification removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent commentsIntent = new Intent(NotificationsActivity.this, CommentsActivity.class);
        commentsIntent.putExtra("PostKey", postKey);
        startActivity(commentsIntent);
    }

    private void FriendProfileIntent(String uid) {
        NotificationFriendReq.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(NotificationsActivity.this, "Notification removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent friendProfileIntent = new Intent(NotificationsActivity.this, OthersProfileActivity.class);
        friendProfileIntent.putExtra("visit__user_id", uid);
        startActivity(friendProfileIntent);
    }


    public class NotificationsPostAdapter extends RecyclerView.Adapter{

        private ArrayList<Notifications> dataSet;
        int total_types;
        Context mContext;

        public class CommentsNotification extends RecyclerView.ViewHolder{

            TextView userName, userPostTitle;
            ImageView userProfileImage;
            LinearLayout linearLayout;

           // String usersName;
            DatabaseReference userRef, commentNotification, postRef;

            public CommentsNotification(View itemView) {
                super(itemView);

                this.userName = (TextView) itemView.findViewById(R.id.TV_NotificationsFriendUsername);
                this.userProfileImage = (ImageView) itemView.findViewById(R.id.IV_NotificationProfilePhoto);
                this.userPostTitle = (TextView) itemView.findViewById(R.id.TV_NotificationPostTitle);
                this.linearLayout = (LinearLayout)itemView.findViewById(R.id.LL_Notifications);

                userRef = FirebaseDatabase.getInstance().getReference().child("Users");
                commentNotification = FirebaseDatabase.getInstance().getReference().child("Notifications").child("Comments").child(CurrentUserID);
                postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            }
        }

        public class FriendsNotification extends RecyclerView.ViewHolder{

            TextView userName, userPostTitle;
            ImageView userProfileImage;
            LinearLayout linearLayout;

            DatabaseReference usersRef, FriendNotification;

            public FriendsNotification(View itemView) {
                super(itemView);
                this.userName = (TextView) itemView.findViewById(R.id.TV_NotificationsFriendUsername);
                this.userProfileImage = (ImageView) itemView.findViewById(R.id.IV_NotificationProfilePhoto);
                this.userPostTitle = (TextView) itemView.findViewById(R.id.TV_NotificationPostTitle);
                this.linearLayout = (LinearLayout)itemView.findViewById(R.id.LL_Notifications);

                usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                FriendNotification = FirebaseDatabase.getInstance().getReference().child("Notifications").child("FriendRequests").child(CurrentUserID);
            }
        }

        public NotificationsPostAdapter(ArrayList<Notifications>data, Context context){
            this.dataSet = data;
            this.mContext = context;
            total_types = dataSet.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType){
                case Notifications.COMMENT_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notifications,parent,false);
                    return new CommentsNotification(view);
                case Notifications.FRIEND_REQUEST_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notifications,parent,false);
                    return new FriendsNotification(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            switch (dataSet.get(position).type){
                case 0:
                    return Notifications.COMMENT_TYPE;
                case 1:
                    return Notifications.FRIEND_REQUEST_TYPE;
                default:
                    return -1;
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            final Notifications object = dataSet.get(position);

            if (object!= null){
                switch (object.type){
                    case Notifications.COMMENT_TYPE:
                        final String[] usersName = {""};

                        ((CommentsNotification)holder).userRef.child(object.getCommenterID()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    usersName[0] = dataSnapshot.child("username").getValue().toString();
                                    String userPhoto = null;
                                    if (dataSnapshot.child("profileimage").exists()) {
                                        userPhoto = dataSnapshot.child("profileimage").getValue().toString();
                                    }
                                    RequestOptions requestOptions = new RequestOptions();
                                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                                    Glide.with(mContext).load(userPhoto).apply(requestOptions).into(((CommentsNotification)holder).userProfileImage);
                                }
                                ((CommentsNotification)holder).commentNotification.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final int[] commentsCount = new int[1];
                                        if (dataSnapshot.exists()){
                                            commentsCount[0] = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                            String number = Integer.toString(commentsCount[0] - 1);
                                            if (commentsCount[0] == 1) {
                                                ((CommentsNotification)holder).userName.setText(usersName[0] + " commented on your post");
                                            }
                                            else if (commentsCount[0] == 2){
                                                ((CommentsNotification)holder).userName.setText(usersName[0] + " and " + (number) + " other, commented on your post");
                                            }
                                            else if (commentsCount[0] > 2){
                                                ((CommentsNotification)holder).userName.setText(usersName[0] + " and " + number + " others, commented on your post");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        ((CommentsNotification)holder).postRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String postTitle = dataSnapshot.child(object.getPostKey()).child("title").getValue().toString();
                                    ((CommentsNotification)holder).userPostTitle.setText(postTitle);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        ((CommentsNotification)holder).linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                PostIntent(object.getPostKey());

                            }
                        });
                        break;

                    case Notifications.FRIEND_REQUEST_TYPE:

                        ((FriendsNotification)holder).usersRef.child(object.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String friendName = dataSnapshot.child("username").getValue().toString();
                                    String friendPhoto = null;
                                    if (dataSnapshot.child("profileimage").exists()){
                                        friendPhoto = dataSnapshot.child("profileimage").getValue().toString();
                                    }
                                    ((FriendsNotification)holder).userPostTitle.setText("Request to follow");
                                    ((FriendsNotification)holder).userName.setText(friendName + " wants to follow you");
                                    RequestOptions requestOptions = new RequestOptions();
                                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                                    Glide.with(mContext).load(friendPhoto).apply(requestOptions).into(((FriendsNotification)holder).userProfileImage);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        ((FriendsNotification)holder).linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FriendProfileIntent(object.getUid());
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

    }
}
