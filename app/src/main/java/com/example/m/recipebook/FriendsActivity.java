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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendsList;
    private DatabaseReference FriendsRef, UserRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    private FriendsViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarFriends);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Followers");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendsList = (RecyclerView) findViewById(R.id.recyclerViewFriendsList);
        DisplayAllFriends();
    }

    private void DisplayAllFriends() {
        final List<String> friendID = new ArrayList<String>();
        final List<String> dateStart = new ArrayList<String>();

        FriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Friends value = dataSnapshot1.getValue(Friends.class);
                    String date = value.getDate();
                    String uid = dataSnapshot1.getRef().getKey();

                    dateStart.add(date);
                    friendID.add(uid);
                }
                CheckUserExistance(dateStart, friendID);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void CheckUserExistance(final List<String> date, final List<String> uid) {
        final ArrayList<Friends> list = new ArrayList<>();
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i;
                for (i = 0; i < uid.size(); i++ ){
                    if (dataSnapshot.hasChild(uid.get(i))){
                        Friends all = new Friends(Friends.Friends_Type,"","","","");
                        String friendsID = uid.get(i);
                        String friendsDate = date.get(i);
                        String username = dataSnapshot.child(uid.get(i)).child("username").getValue().toString();
                        String userImage = null;
                        if (dataSnapshot.child(uid.get(i)).child("profileimage").exists()) {
                            userImage = dataSnapshot.child(uid.get(i)).child("profileimage").getValue().toString();
                        }

                        all.setUid(friendsID);
                        all.setDate(friendsDate);
                        all.setUsername(username);
                        if (userImage!=null) {
                            all.setUserimage(userImage);
                        }
                        list.add(all);
                    }
                }
                Collections.reverse(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter = new FriendsViewAdapter(list, FriendsActivity.this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FriendsActivity.this, OrientationHelper.VERTICAL, false);
        myFriendsList.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(false);
        myFriendsList.setLayoutManager(linearLayoutManager);
        myFriendsList.setItemAnimator(new DefaultItemAnimator());
        myFriendsList.setAdapter(adapter);
    }

    public class FriendsViewAdapter extends RecyclerView.Adapter {
        private ArrayList<Friends> dataset;
        int total_types;
        Context mContext;


        public class FriendsList extends RecyclerView.ViewHolder {
            TextView tvUserName, tvDate;
            ImageView ivFriends;
            Button bFollow;
            LinearLayout linearLayout;

            DatabaseReference UserRef;

            public FriendsList(View itemView) {
                super(itemView);
                this.tvUserName = (TextView) itemView.findViewById(R.id.TV_AddFriendUsername);
                this.tvDate = (TextView) itemView.findViewById(R.id.TV_AddFriendDate);
                this.ivFriends = (ImageView) itemView.findViewById(R.id.IV_AddFriendsProfileImage);
                this.bFollow = (Button) itemView.findViewById(R.id.B_AddFriendFollow);
                this.linearLayout = (LinearLayout) itemView.findViewById(R.id.LL_AddFriends);

                UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
            }
        }

        public FriendsViewAdapter(ArrayList<Friends> data, Context context) {
            this.dataset = data;
            this.mContext = context;
            total_types = dataset.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            switch (viewType) {
                case Friends.Friends_Type:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_add_friends, viewGroup, false);
                    return new FriendsList(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            switch (dataset.get(position).type) {
                case 0:
                    return Friends.Friends_Type;
                default:
                    return -1;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
            final Friends object = dataset.get(position);

            if (object != null) {
                switch (object.type) {
                    case Friends.Friends_Type:
                        ((FriendsList) viewHolder).bFollow.setVisibility(View.GONE);
                        ((FriendsList) viewHolder).bFollow.setEnabled(false);
                        ((FriendsList) viewHolder).tvDate.setText("Following Since: " + object.getDate());
                        ((FriendsList) viewHolder).tvUserName.setText(object.getUsername());
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                        Glide.with(mContext).load(object.getUserimage()).apply(requestOptions).into(((FriendsList)viewHolder).ivFriends);

                        ((FriendsList) viewHolder).linearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(FriendsActivity.this, OthersProfileActivity.class);
                                profileIntent.putExtra("visit__user_id", object.getUid());
                                startActivity(profileIntent);
                            }
                        });

                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }
    }
}
