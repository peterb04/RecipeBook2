package com.example.m.recipebook;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddFriendsActivity extends AppCompatActivity {

    private RecyclerView AddFriendsRecyclerView;
    private EditText searchFriends;
    private Button searchButton;
    private DatabaseReference UserRef, PostsRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String OnlineUserID;
    private Query searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarAddFriends);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find People");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        OnlineUserID = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(OnlineUserID);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        AddFriendsRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewAddFriends);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        AddFriendsRecyclerView.setLayoutManager(linearLayoutManager);
        AddFriendsRecyclerView.setHasFixedSize(true);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        AddFriendsRecyclerView.setLayoutManager(linearLayoutManager);

        searchFriends = (EditText) findViewById(R.id.ET_AddFriendsSearch);
        searchButton = (Button) findViewById(R.id.B_AddFriendsSearchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String SearchFriendsInput = searchFriends.getText().toString();
                if (!SearchFriendsInput.isEmpty()) {
                    SearchForFriends(SearchFriendsInput);
                }

            }
        });
    }

    private void SearchForFriends(String SearchFriendsInput) {
        searchQuery = UserRef.orderByChild("username").startAt(SearchFriendsInput).endAt(SearchFriendsInput + "\uf8ff");
        FirebaseRecyclerAdapter<FindFriends, AddFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, AddFriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.layout_add_friends,
                        AddFriendsViewHolder.class,
                        searchQuery
                )
        {
            @Override
            protected void populateViewHolder(final AddFriendsViewHolder viewHolder, FindFriends model, int position) {
                final String usersID = getRef(position).getKey();
                UserRef.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String accountStatus = "";
                            if (dataSnapshot.child("accountStatus").exists()) {
                                accountStatus = dataSnapshot.child("accountStatus").getValue().toString();
                            }
                            if (!accountStatus.equals("Suspended"))
                            {
                                final String username = dataSnapshot.child("username").getValue().toString();
                                String profileImage = null;
                                if (dataSnapshot.child("profileimage").exists()) {
                                    profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                }

                                viewHolder.setUsername(username);
                                viewHolder.setProfileimage(getApplicationContext(), profileImage);

                                if (OnlineUserID.equals(usersID)) {
                                    viewHolder.followButton.setVisibility(View.GONE);
                                    viewHolder.followButton.setEnabled(false);
                                }

                                FriendsRef.child(usersID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            viewHolder.followButton.setVisibility(View.GONE);
                                            viewHolder.followButton.setEnabled(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                viewHolder.linearLayoutFollowers.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent profileIntent = new Intent(AddFriendsActivity.this, OthersProfileActivity.class);
                                        profileIntent.putExtra("visit__user_id", usersID);
                                        startActivity(profileIntent);
                                    }
                                });

                                viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent profileIntent = new Intent(AddFriendsActivity.this, OthersProfileActivity.class);
                                        profileIntent.putExtra("visit__user_id", usersID);
                                        startActivity(profileIntent);
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
        AddFriendsRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }



    public static class AddFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        TextView friendsSinceDate;
        LinearLayout linearLayoutFollowers;
        Button followButton;

        public AddFriendsViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            this.friendsSinceDate = (TextView) mView.findViewById(R.id.TV_AddFriendDate);
            friendsSinceDate.setVisibility(View.INVISIBLE);
            linearLayoutFollowers = (LinearLayout) mView.findViewById(R.id.LL_AddFriends);
            followButton = (Button) mView.findViewById(R.id.B_AddFriendFollow);
        }

        public void setProfileimage(Context ctx, String profileimage){
            ImageView userImage = (ImageView) mView.findViewById(R.id.IV_AddFriendsProfileImage);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
            Glide.with(mView.getContext()).load(profileimage).apply(requestOptions).into(userImage);
        }

        public void setUsername(String username){
            TextView usersName = (TextView) mView.findViewById(R.id.TV_AddFriendUsername);
            usersName.setText(username);
        }
    }
}

