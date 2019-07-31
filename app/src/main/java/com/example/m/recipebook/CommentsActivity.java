package com.example.m.recipebook;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Collections;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private Button postCommentButton;
    private EditText postCommentText;
    private RecyclerView CommentsList;
    private CommentsViewHolder adapter;
    private String PostKey, CurrentUserID;
    private DatabaseReference usersRef, postsRef, commentsStatusRef, notificationsRef, postUserRef;;
    private FirebaseAuth mAuth;
    private Query query;
    private AlertDialog.Builder alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarComments);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        PostKey = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments");
        postUserRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        commentsStatusRef = FirebaseDatabase.getInstance().getReference().child("CommentsStatus");
        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        query = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments").orderByChild("timestamp");

        alertDialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        CommentsList = (RecyclerView) findViewById(R.id.recyclerViewComments);
        postCommentButton = (Button) findViewById(R.id.B_PostComment);
        postCommentText = (EditText) findViewById(R.id.ET_CommentInput);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(CurrentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String userImage = null;
                            if (dataSnapshot.child("profileimage").exists()) {
                                userImage = dataSnapshot.child("profileimage").getValue().toString();
                            }
                            ValidateComment(userName, userImage);
                            postCommentText.setText("");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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

    @Override
    protected void onStart() {
        super.onStart();

        final ArrayList<Comments> list = new ArrayList<>();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        Comments value = dataSnapshot1.getValue(Comments.class);
                        Comments all = new Comments(Comments.COMMENTS_HEADER_TYPE, 0, "", "", "", "","","","");
                       // String name = value.getUsername();
                        String comment = value.getComment();
                        String date = value.getDate();
                        String time = value.getTime();
                        //String image = value.getProfileImage();
                        String uid = value.getUid();
                        String commentNodeID = dataSnapshot1.getKey();

                        //all.setUsername(name);
                        all.setComment(comment);
                        all.setDate(date);
                        all.setTime(time);
                        //all.setProfileImage(image);
                        all.setUid(uid);
                        all.setNodeID(commentNodeID);

                        list.addAll(Collections.singleton(all));

                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new CommentsViewHolder(list, CommentsActivity.this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CommentsActivity.this, OrientationHelper.VERTICAL, false);
        CommentsList.setLayoutManager(linearLayoutManager);
        CommentsList.setItemAnimator(new DefaultItemAnimator());
        CommentsList.setAdapter(adapter);
    }

    public class CommentsViewHolder extends RecyclerView.Adapter{
        private ArrayList<Comments> dataset;
        Context mContext;
        int total_types;

        public class comments extends RecyclerView.ViewHolder{

            TextView userName, userComment, userDate, userTime;
            ImageView userImage;
            Button editButton;

            public comments(View itemView) {
                super(itemView);

                this.userName = (TextView) itemView.findViewById(R.id.TV_CommentUsername);
                this.userComment = (TextView) itemView.findViewById(R.id.TV_CommentComment);
                this.userDate = (TextView) itemView.findViewById(R.id.TV_CommentDate);
                this.userTime = (TextView) itemView.findViewById(R.id.TV_CommentTime);
                this.userImage = (ImageView) itemView.findViewById(R.id.IV_CommentProfilePicture);
                this.editButton = (Button) itemView.findViewById(R.id.B_CommentsEdit);
            }
        }

        public CommentsViewHolder(ArrayList<Comments>data, Context context){
            this.dataset = data;
            this.mContext = context;
            total_types = dataset.size();

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType){
                case Comments.COMMENTS_HEADER_TYPE:
                    view=LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comments,parent,false);
                    return new comments(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            switch (dataset.get(position).type) {
                case 0:
                    return Comments.COMMENTS_HEADER_TYPE;

                default:
                    return -1;
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            final Comments object = dataset.get(position);

            if (object != null) {
                switch (object.type){
                    case Comments.COMMENTS_HEADER_TYPE:
                        final String[] postUserID = new String[1];
                        //((comments)holder).userName.setText(object.username);
                        ((comments)holder).userComment.setText(object.comment);
                        ((comments)holder).userDate.setText("Posted: " + object.date);
                        ((comments)holder).userTime.setText(object.time);
                        /*
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                        Glide.with(mContext).load(object.profileImage).apply(requestOptions).into(((comments) holder).userImage);
*/
                        postUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    if (dataSnapshot.child("uid").exists()){
                                        postUserID[0] = dataSnapshot.child("uid").getValue().toString();
                                        if (object.uid.equals(CurrentUserID)){
                                            ((comments)holder).editButton.setVisibility(View.VISIBLE);
                                            ((comments)holder).editButton.setEnabled(true);
                                        }

                                        else if (postUserID[0] != null) {
                                            if (postUserID[0].equals(CurrentUserID)) {
                                                ((comments) holder).editButton.setVisibility(View.VISIBLE);
                                                ((comments) holder).editButton.setEnabled(true);
                                            }
                                        }

                                        else{
                                            ((comments)holder).editButton.setVisibility(View.INVISIBLE);
                                            ((comments)holder).editButton.setEnabled(false);
                                        }
                                    }
                                    else{
                                        if (object.uid.equals(CurrentUserID)){
                                            ((comments)holder).editButton.setVisibility(View.VISIBLE);
                                            ((comments)holder).editButton.setEnabled(true);
                                        }

                                        else if (postUserID[0] != null) {
                                            if (postUserID[0].equals(CurrentUserID)) {
                                                ((comments) holder).editButton.setVisibility(View.VISIBLE);
                                                ((comments) holder).editButton.setEnabled(true);
                                            }
                                        }

                                        else{
                                            ((comments)holder).editButton.setVisibility(View.INVISIBLE);
                                            ((comments)holder).editButton.setEnabled(false);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                        final String UID = object.getUid();
                        final RequestOptions finalRequestOptions = requestOptions;

                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(UID).exists()) {
                                    if (dataSnapshot.child(UID).child("profileimage").exists()) {
                                        object.setProfileImage(dataSnapshot.child(UID).getValue().toString());
                                        Glide.with(mContext).load(object.profileImage).apply(finalRequestOptions).into(((comments) holder).userImage);
                                    } else {
                                        Drawable image = (Drawable) getResources().getDrawable(R.drawable.icon_person);
                                        ((comments) holder).userImage.setImageDrawable(image);
                                    }

                                    if (dataSnapshot.child(UID).child("username").exists()) {
                                        String username = dataSnapshot.child(UID).child("username").getValue().toString();
                                        ((comments) holder).userName.setText(username);
                                    } else {
                                        ((comments) holder).userName.setText("");
                                    }
                                }
                                else{
                                    Drawable image = (Drawable) getResources().getDrawable(R.drawable.icon_person);
                                    ((comments) holder).userImage.setImageDrawable(image);
                                    ((comments) holder).userName.setText("");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        ((comments)holder).editButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditComment(object.nodeID);
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

    private void EditComment(final String nodeID) {

        final CharSequence[] items = {"Delete Comment", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CommentsActivity.this);
        builder.setTitle("Comment");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Delete Comment")) {
                            alertDialog.setTitle("Delete Comment");
                            alertDialog.setMessage("This comment will be deleted. Continue?");
                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    postsRef.child(nodeID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CommentsActivity.this, "Comment Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CommentsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    commentsStatusRef.child(PostKey).child(nodeID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CommentsActivity.this, "Comment Status Removed", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CommentsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });
                            alertDialog.setNegativeButton("Cancel", null);
                            alertDialog.create();
                            alertDialog.show();
                        }
                 else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }

        });
        builder.show();
    }

    private void ValidateComment(String userName, String userImage) {
        String commentText = postCommentText.getText().toString();
        String timestamp;
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please enter text.", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTIme = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(callForTIme.getTime());

            Calendar callForDateStamp = Calendar.getInstance();
            SimpleDateFormat dateStampDate = new SimpleDateFormat("yyyyMMdd");
            String dateStampTemp = dateStampDate.format(callForDateStamp.getTime());


            Calendar callForTimeStamp = Calendar.getInstance();
            SimpleDateFormat timeStampTime = new SimpleDateFormat("HHmmss");
            String timeStampTemp = timeStampTime.format(callForTimeStamp.getTime());
            timestamp = dateStampTemp + timeStampTemp;

            //final String randomKey = CurrentUserID + saveCurrentDate + saveCurrentTime;
            final String randomKey = CurrentUserID + timestamp;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", CurrentUserID);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);
            commentsMap.put("timestamp", timestamp);
            commentsMap.put("profileImage", userImage);

            postsRef.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "Comment Posted", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(CommentsActivity.this, "Error Occurred, try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            commentsStatusRef.child(PostKey).child(randomKey).setValue("unread").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "Comment Status Posted", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(CommentsActivity.this, "Comment Status Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            postUserRef.child("uid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        String userID = dataSnapshot.getValue().toString();
                        if (!userID.equals(CurrentUserID)) {
                            notificationsRef.child("Comments").child(userID).child(PostKey).child(CurrentUserID).setValue(true);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
