package com.example.m.recipebook;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {


    private TextView usernameTV, emailTV, deleteAccount, changePassword, termsConditions, privacyPolicy;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostRef, CommentStatusRef, LikesRef, FriendsRef, FriendsPostRef;
    private Query PostRefQuery, CategoriesQuery;

    private String CurrentUserID;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarAccount);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        usernameTV = (TextView) findViewById(R.id.TV_AccountUsername);
        emailTV = (TextView) findViewById(R.id.TV_AccountEmail);

        final Intent intent = getIntent();
        String username = intent.getStringExtra("NAME");
        String email = intent.getStringExtra("EMAIL");

        String providerID = null;

        usernameTV.setText(username);
        emailTV.setText(email);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRefQuery = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uid").equalTo(CurrentUserID);
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        CategoriesQuery = FirebaseDatabase.getInstance().getReference().child("Categories");
        CommentStatusRef = FirebaseDatabase.getInstance().getReference().child("CommentStatus");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsPostRef = FirebaseDatabase.getInstance().getReference().child("FriendsPost");

        progressDialog = new ProgressDialog(this);

        deleteAccount = (TextView) findViewById(R.id.TV_AccountDelete);
        changePassword = (TextView) findViewById(R.id.TV_AccountChangePassword);
        termsConditions = (TextView) findViewById(R.id.TV_AccountTC);
        privacyPolicy = (TextView) findViewById(R.id.TV_AccountPrivacyPolicy);

        //FirebaseUser user = mAuth.getCurrentUser();

        try {
            for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                providerID = userInfo.getProviderId();
            }
            if (providerID.equals("facebook.com")) {
                changePassword.setEnabled(false);
                changePassword.setTextColor(Color.parseColor("#C7C0C0"));
            }

        }
        catch (Exception e){
            // Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmDeleteAccount();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangePassword();
            }
        });

        termsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayTermsConditions();
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayPrivacyPolicy();
            }
        });


    }

    private void DisplayTermsConditions() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Terms and Conditions");

        WebView webView = new WebView(this);
        //webView.loadUrl("file:///android_asset/privacy_policy.html");
        webView.loadUrl("https://docs.google.com/document/d/e/2PACX-1vTbjgoy00ydHybbi3Cu2_UrYF_X3YJFJZ9SWV-h2kOrIuPKP-RQtGzF6bvW5d4QkxGB-UH9bTqbdT7K/pub");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        alert.setView(webView);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    private void DisplayPrivacyPolicy() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Privacy Policy");

        WebView webView = new WebView(this);
        webView.loadUrl("https://docs.google.com/document/d/e/2PACX-1vRbbdVCQLa9lv3hba5rCwbWyUL9evnYLBgnInwUCpPUACp3ooy0vqDJGbnSvcnsbgmGVe2u8ECDXqd6/pub");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        alert.setView(webView);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();

    }

    private void ChangePassword() {
        startActivity(new Intent(AccountActivity.this, UpdatePasswordActivity.class));
    }

    private void ConfirmDeleteAccount() {
        // final CharSequence[] items = {"Delete Account", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        AlertDialog.Builder reauthenticateBuilder = new AlertDialog.Builder(AccountActivity.this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to proceed. This will delete your account");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteAccount();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();

    }

    private void DeleteAccount() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        builder.setTitle("Delete Account");
        builder.setMessage("Would you like to keep your recipes for others to view on myCuisine?");
        builder.setPositiveButton("Keep Recipes and Delete Account", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.setTitle("Deleting Account");
                progressDialog.setMessage("Your account is now being deleted");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);
                KeepRecipesAndDeleteAccount();
            }
        });
        builder.setNegativeButton("Delete Account and Recipes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.setTitle("Deleting Account");
                progressDialog.setMessage("Your recipes are now being deleted");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);
                DeleteRecipesAndDeleteAccount();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();

/*
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                            startActivity(intent);
                            //Log.d(Tag, "User account deleted.");
                        }
                        else {
                            Toast.makeText(AccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
*/
    }

    private void KeepRecipesAndDeleteAccount() {

        //Delete Friends List
        FriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child(CurrentUserID).exists()){
                        dataSnapshot.child(CurrentUserID).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Delete FriendsPostList
        FriendsPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child(CurrentUserID).exists()){
                        dataSnapshot.child(CurrentUserID).getRef().removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersRef.child(CurrentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(AccountActivity.this, "User profile has been removed", Toast.LENGTH_SHORT).show();
                DeleteUsersAccount();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                DeleteUsersAccount();
            }
        });
    }

    private void DeleteRecipesAndDeleteAccount(){
        final List<String> recipes = new ArrayList<String>();
        final List<String> categories = new ArrayList<String>();
        final List<String> comments = new ArrayList<String>();
        final Boolean[] postsDeleted = {false};
        PostRefQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        int noCategories = (int) dataSnapshot1.child("Categories").getChildrenCount();
                        if (noCategories != 0){
                            for (int i = 0; i <noCategories; i++){
                                String temp = String.valueOf(i);
                                String cat = dataSnapshot1.child("Categories").child(temp).getValue().toString();
                                categories.add(cat);
                            }
                        }

                        final String recipeKey = dataSnapshot1.getKey();
                        CategoriesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    for (int i = 0; i < categories.size(); i++) {
                                        String check = dataSnapshot.child(categories.get(i)).child(recipeKey).getKey();
                                        if (check == recipeKey) {
                                            dataSnapshot.child(categories.get(i)).child(recipeKey).getRef().removeValue();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        recipes.add(recipeKey);
                    }
/*
                    //Delete Posts Categories
                    for (int i = 0; i < recipes.size(); i++){
                        CategoriesQuery.orderByKey().equalTo(recipes.get(0)).getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
*/
                    //Delete Comments Status
                    for (int i = 0; i < recipes.size(); i++){
                        final int finalI = i;
                        CommentStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.child(recipes.get(finalI)).exists()){
                                        dataSnapshot.child(recipes.get(finalI)).getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    //Delete Post Likes
                    for (int i = 0; i < recipes.size(); i++){
                        final int finalI = i;
                        LikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.child(recipes.get(finalI)).exists()){
                                        dataSnapshot.child(recipes.get(finalI)).getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    //Delete Friends List
                    FriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.child(CurrentUserID).exists()){
                                    dataSnapshot.child(CurrentUserID).getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //Delete FriendsPost List
                    FriendsPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.child(CurrentUserID).exists()){
                                    dataSnapshot.child(CurrentUserID).getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //Delete Posts
                    for (int i = 0; i < recipes.size(); i++){
                        final int finalI = i;
                        PostRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    dataSnapshot.child(recipes.get(finalI)).getRef().removeValue();
                                    if (finalI == recipes.size()-1){
                                        progressDialog.dismiss();
                                        DeleteUserAndAccount();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }
                else{
                    progressDialog.dismiss();
                   // progressDialog.setTitle("Deleting Account");
                   // progressDialog.setMessage("Your recipes are now being deleted");
                   // progressDialog.show();
                    //progressDialog.setCanceledOnTouchOutside(true);
                    DeleteUserAndAccount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DeleteUserAndAccount() {
        UsersRef.child(CurrentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(AccountActivity.this, "User profile has been removed", Toast.LENGTH_SHORT).show();
                DeleteUsersAccount();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                DeleteUsersAccount();
            }
        });
    }

    private void DeleteUsersAccount() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                            startActivity(intent);

                            //Log.d(Tag, "User account deleted.");
                        }
                        else {
                            Toast.makeText(AccountActivity.this, "Something went wrong. Please sign out from your account and try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


}
