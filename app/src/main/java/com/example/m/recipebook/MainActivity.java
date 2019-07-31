package com.example.m.recipebook;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.m.recipebook.Adapter.EndlessRecyclerListener;
import com.example.m.recipebook.Controller.StarController;
import com.example.m.recipebook.Database.SqliteDatabaseHelper;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener, View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private DatabaseReference PostsRef;
    private DatabaseReference LikesRef;
    private DatabaseReference FollowersPost;
    private DatabaseReference NotificationsRef;
    private DatabaseReference AppStatusRef;
    private Query PostRefByTime, PostRefByLikes, PostRefByFollowers;

    SqliteDatabaseHelper recipeDB;

    private static final int MY_PERMISSIONS_REQUEST_NETWORK_PERMISSION = 3;

    private InterstitialAd mInterstitialAd;
    //private RewardedVideoAd mRewardedVideoAd;

    private RecyclerView mainRecyclerView;
    private ListView listViewUnfinishedRecipes;
    String CurrentUserID;
    Boolean getFollowersPostComplete = false;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    private ProgressBar progressBar;

    private ImageView navProfilePic;
    private TextView navUserName, navUserEmail;
    private String navName = "";
    private String navEmail = "";

    private Boolean ShowPosts = false;

    PostsViewHolder adapter;
    final ArrayList<Posts> list = new ArrayList<>();

    private int selected = 1;
    private int retrieveAmount = 5;
    private TextView buttonNewPosts, buttonTopRated, buttonFollowing;
    private View newestView, topRatedView, followingView;

    private int adOpenedChecker = 0, adCounter = 0;
    private int SORT_BY_NEWEST_RECIPE = 0, SORT_BY_RATING = 1, SORT_BY_FOLLOWERS = 2;
    private AdRequest request;

    private EndlessRecyclerListener scrollListener;

    private ImageView notificationIconAlertDot;

    final List<String> sortByValue = new ArrayList<String>();
    final int[] listSize = {0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        AppStatusRef = FirebaseDatabase.getInstance().getReference().child("AppStatus");

        MobileAds.initialize(this, "ca-app-pub-3940256099942544/1033173712");// Interstitial Test Ad
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");//Rewarded Video Test Ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        request = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(request);


        //mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
       // mRewardedVideoAd.setRewardedVideoAdListener(this);
       // mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());

        PostRefByTime = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("timestamp").limitToLast(retrieveAmount);
        PostRefByLikes = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("sortByLikes").limitToLast(retrieveAmount);
        PostRefByFollowers = FirebaseDatabase.getInstance().getReference().child("FriendsPost").child(CurrentUserID).orderByChild("timestamp").limitToLast(retrieveAmount);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        FollowersPost = FirebaseDatabase.getInstance().getReference().child("FriendsPost");
        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        recipeDB = new SqliteDatabaseHelper(this);

        buttonNewPosts = (TextView) findViewById(R.id.B_MainNewPostFirst);
        buttonTopRated = (TextView) findViewById(R.id.B_MainTopRated);
        buttonFollowing = (TextView) findViewById(R.id.TV_Following);

        newestView = (View) findViewById(R.id.view_newest);
        topRatedView = (View) findViewById(R.id.view_topRated);
        followingView = (View) findViewById(R.id.view_following);

        AdView adView = (AdView)findViewById(R.id.adView);
        MobileAds.initialize(this, "ca-app-pub-3940256099942544/1033173712");

        AdRequest adRequest =new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        adView.loadAd(adRequest);

        mainRecyclerView = (RecyclerView) findViewById(R.id.main_recyclerView);

        progressBar = (ProgressBar) findViewById(R.id.main_progressBar);

        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");

        //Button menuProfile = (Button) mToolbar.findViewById(R.id.menu_profile);
        Button menuSearch = (Button) mToolbar.findViewById(R.id.menu_search);
       // Button menuFriends = (Button) mToolbar.findViewById(R.id.menu_friends);
        Button menuCategories = (Button) mToolbar.findViewById(R.id.menu_categories);
        ImageButton menuTimer = (ImageButton) mToolbar.findViewById(R.id.menu_timer);
        Button menuAddRecipe = (Button) mToolbar.findViewById(R.id.menu_addRecipe);
        //menuProfile.setOnClickListener(this);
        menuSearch.setOnClickListener(this);
        //menuFriends.setOnClickListener(this);
        menuCategories.setOnClickListener(this);
        menuTimer.setOnClickListener(this);
        menuAddRecipe.setOnClickListener(this);

        notificationIconAlertDot = (ImageView) mToolbar.findViewById(R.id.menu_notificationDot);
        notificationIconAlertDot.bringToFront();

        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navView);

        View navHeaderView = navigationView.getHeaderView(0);
        navProfilePic = (ImageView) navHeaderView.findViewById(R.id.nav_headerProfilePic);
        navUserName = (TextView) navHeaderView.findViewById(R.id.nav_headerUserName);
        navUserEmail = (TextView) navHeaderView.findViewById(R.id.nav_headerEmail);

        UsersRef.child(CurrentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String navPic = null;
                   // String navName = "";
                    //String navEmail = "";
                    if (dataSnapshot.child("profileimage").exists()){
                        navPic = dataSnapshot.child("profileimage").getValue().toString();
                    }
                    if (dataSnapshot.child("username").exists()) {
                        navName = dataSnapshot.child("username").getValue().toString();
                    }
                    if(dataSnapshot.child("userEmail").exists()) {
                        navEmail = dataSnapshot.child("userEmail").getValue().toString();
                    }
                    RequestOptions requestOptions = new RequestOptions();
                    //requestOptions = requestOptions.circleCropTransform();
                    if (navPic != null) {
                        Glide.with(MainActivity.this).load(navPic).apply(RequestOptions.circleCropTransform()).into(navProfilePic);
                    }
                    navUserName.setText(navName);
                    navUserEmail.setText(navEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        navUserEmail.setText("email address");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelectorItem(item);
                return false;
            }
        });

        final int disabled = 0;
        final int visible = 1;
        int allPostsVisibility = 1;
        allPostsVisibility = AllPostsVisibilityStatus();

        final int finalAllPostsVisibility = allPostsVisibility;

        buttonNewPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShowPosts) {
                    buttonNewPosts.setTextColor(Color.parseColor("#ffffff"));
                    buttonTopRated.setTextColor(Color.parseColor("#000000"));
                    buttonFollowing.setTextColor(Color.parseColor("#000000"));
                    newestView.setVisibility(View.VISIBLE);
                    topRatedView.setVisibility(View.GONE);
                    followingView.setVisibility(View.GONE);


                    if (selected != 1) {
                        selected = 1;
                        DisplayAllUsersPosts(PostRefByTime, 0, SORT_BY_NEWEST_RECIPE);
                    }
                }
            }
        });

        buttonTopRated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShowPosts) {
                    buttonNewPosts.setTextColor(Color.parseColor("#000000"));
                    buttonTopRated.setTextColor(Color.parseColor("#ffffff"));
                    buttonFollowing.setTextColor(Color.parseColor("#000000"));
                    newestView.setVisibility(View.GONE);
                    topRatedView.setVisibility(View.VISIBLE);
                    followingView.setVisibility(View.GONE);
                    try {
                        scrollListener.resetState();
                    } catch (Exception e) {

                    }
                    if (selected != 2) {
                        selected = 2;
                        DisplayAllUsersPosts(PostRefByLikes, 1, SORT_BY_RATING);
                    }
                }

            }
        });


        buttonFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShowPosts) {
                    buttonNewPosts.setTextColor(Color.parseColor("#000000"));
                    buttonTopRated.setTextColor(Color.parseColor("#000000"));
                    buttonFollowing.setTextColor(Color.parseColor("#ffffff"));
                    newestView.setVisibility(View.GONE);
                    topRatedView.setVisibility(View.GONE);
                    followingView.setVisibility(View.VISIBLE);
                    try {
                        scrollListener.resetState();
                    } catch (Exception e) {

                    }
                    if (selected != 3) {
                        selected = 3;
                        DisplayAllUsersPosts(PostRefByFollowers, 2, SORT_BY_FOLLOWERS);
                    }
                }
            }
        });

        SetupNotificationIcon();

        if (ShowPosts) {
            DisplayAllUsersPosts(PostRefByTime, 0, SORT_BY_NEWEST_RECIPE);
        }

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
             //   Toast.makeText(MainActivity.this, "Interstitial Ad Loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int i) {
               // Toast.makeText(MainActivity.this, "Int Ad Failed to load", Toast.LENGTH_SHORT).show();
                Log.d("TAG", String.valueOf(i));
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdClosed() {
                adOpenedChecker = 0;
                mInterstitialAd.loadAd(request);
            }
        });
    }

    private int AllPostsVisibilityStatus() {
        final String[] postVisibility = {""};
        final int disabled = 0;
        final int visible = 1;
        final int[] result = {0};
        AppStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("PostsVisibility").exists()){
                        postVisibility[0] = dataSnapshot.child("PostsVisibility").getValue().toString();
                        if (postVisibility[0].equals("disabled")){
                            ShowPosts = false;
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                            alertDialog.setTitle("Feed Unavailable");
                            alertDialog.setMessage("All posts in the feed are temporarily unavailable");
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            alertDialog.show();
                        }
                        else if (postVisibility[0].equals("visible")){
                            ShowPosts = true;
                            DisplayAllUsersPosts(PostRefByTime, 0, SORT_BY_NEWEST_RECIPE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       // return result;
        return result[0];
    }

    private void SetupNotificationIcon() {
        NotificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("Comments").child(CurrentUserID).exists()){
                        notificationIconAlertDot.setVisibility(View.VISIBLE);
                    }
                    else if (dataSnapshot.child("FriendRequests").child(CurrentUserID).exists()){
                        notificationIconAlertDot.setVisibility(View.VISIBLE);
                    }
                    else{
                        notificationIconAlertDot.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //case R.id.menu_profile:
             //   profilePageRoutine();
             //  break;

            case R.id.menu_search:
                findRecipe();
                break;

           // case R.id.menu_friends:
             //   viewMyFriends();
             //   break;

            case R.id.menu_categories:
                categories();
                break;

            case R.id.menu_timer:
                notifications();
                break;

            case R.id.menu_addRecipe:
                createNewRecipe();
                break;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:
                if (item != null){
                    if (drawerLayout.isDrawerOpen(Gravity.LEFT)){
                        drawerLayout.closeDrawer((Gravity.LEFT));
                    }
                    else {
                        drawerLayout.openDrawer(Gravity.LEFT);
                    }
                }

        }
        return false;
    }

    private void UserMenuSelectorItem(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_Profile:
                profilePageRoutine();
                break;

            case R.id.nav_addRecipe:
                createNewRecipe();
                break;

            case R.id.nav_myFriends:
                viewMyFriends();
                break;

            case R.id.nav_findRecipe:
                findRecipe();
                break;

            case R.id.nav_categories:
                categories();
                break;

            case R.id.nav_friends:
                findFriends();
                break;

            case R.id.nav_unfinishedrecipes:
                unfinishedRecipes();
                break;

            case R.id.nav_account:
                accountSettings();
                break;

            case R.id.nav_signOut:
                signOut();
                break;
        }
    }

    private void unfinishedRecipes() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.custom_layout_unfinished_recipes);

        ArrayAdapter<String> addAdapter;

        listViewUnfinishedRecipes = (ListView) dialog.findViewById(R.id.LV_customUnfinishedRecipes);
        addAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewUnfinishedRecipes.setAdapter(addAdapter);

        Cursor data = recipeDB.showData(0);
        if (data.getCount() == 0){
            Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show();
            addAdapter.add("No recipes to display");
            addAdapter.notifyDataSetChanged();
        }

        else {
            StringBuffer buffer = new StringBuffer();
            while (data.moveToNext()){
                buffer.append("ID: " + data.getString(0)+ "\n");
                buffer.append("Title: " + data.getString(1)+ "\n");
                buffer.append("Description: " + data.getString(2)+ "\n");

                String title = ("Title: " + data.getString(1));
                addAdapter.add(title);
                addAdapter.notifyDataSetChanged();
            }
            // Toast.makeText(this, buffer.toString(), Toast.LENGTH_SHORT).show();


            listViewUnfinishedRecipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    //String recipeID = String.valueOf(position);
                    //Toast.makeText(MainActivity.this, recipeID, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
                    intent.putExtra("FromMain", 5);
                    intent.putExtra("RECIPE_ID", position);
                    intent.putExtra("EDIT", 1);
                    MainActivity.this.startActivity(intent);
                    recreate();
                }
            });
        }

        dialog.show();
    }

    private void accountSettings() {
        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
        intent.putExtra("NAME", navName);
        intent.putExtra("EMAIL", navEmail);
        MainActivity.this.startActivity(intent);
    }

    private void categories() {
        startActivity(new Intent(MainActivity.this, CategoriesActivity.class));
    }

    private void findRecipe() {
        startActivity(new Intent(MainActivity.this, FindRecipeActivity.class));
    }

    private void viewMyFriends() {
        startActivity(new Intent(MainActivity.this, FriendsActivity.class));
    }

    private void findFriends() {
        startActivity(new Intent(MainActivity.this, AddFriendsActivity.class));
    }
    private void notifications() {
        startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
    }

    private void profilePageRoutine() {
        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
    }

    private void DisplayAllUsersPosts(final Query method, final int queryType, final int recipeSortBy) {

        final boolean[] complete = {false};
        final int[] loadingBarPosition = new int[1];
        final ArrayList<Posts> listLoadMore = new ArrayList<>();
        final boolean[] endOfDatabaseReached = {false};
        final boolean[] loadingFromDatabase = {false};
        final boolean[] loadingBarActive = {false};
        final Query[] loadMoreFollowers = new Query[1];


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(MainActivity.this, "Permission to access network is required", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_NETWORK_PERMISSION);
            }
        } else {
            if ((queryType == 0) || (queryType == 1)) {
                list.clear();
                endOfDatabaseReached[0] = false;
                getFollowersPostComplete = false;
                final Boolean[] dataExists = {true};
                method.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        complete[0] = false;
                        list.clear();
                        sortByValue.clear();
                        adapter.notifyDataSetChanged();
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            Posts value = dataSnapshot1.getValue(Posts.class);
                            Posts all = new Posts(Posts.MAIN_POST_TYPE, 0, "", "", "", "", "", "", "", "", "","");
                            //String username = value.getUsername();
                            String title = value.getTitle();
                            String time = value.getTime();
                            String date = value.getDate();
                            String description = value.getDescription();
                            String recipeImage = "empty";
                            String status = "";
                            String mediaType = "";

                            if (dataSnapshot1.child("Status").exists()){
                                status = dataSnapshot1.child("Status").getValue().toString();
                            }
                            //String postKey = value.getPostKey();
                            if ( dataSnapshot1.child("recipeMainImage").exists()){
                                recipeImage = dataSnapshot1.child("recipeMainImage").getValue().toString();
                            }
                            if (dataSnapshot1.child("mediaType").exists()){
                                mediaType = dataSnapshot1.child("mediaType").getValue().toString();  // todo edited 30/07/2019
                            }
                            String uid = value.getUid();
                            if (queryType == 0) {
                                sortByValue.add(dataSnapshot1.child("timestamp").getValue().toString());
                                dataExists[0] = true;
                            }
                            else if (queryType == 1){
                                if (dataSnapshot1.child("sortByLikes").exists()){
                                    sortByValue.add(dataSnapshot1.child("sortByLikes").getValue().toString());
                                    dataExists[0] = true;
                                }
                                else{
                                    dataExists[0] = false;
                                }
                            }
                            if (!status.equals("suspended")) {
                               // all.setUsername(username);
                                all.setTitle(title);
                                all.setTime(time);
                                all.setDate(date);
                                all.setDescription(description);
                                //all.setProfileImage(profileImage);
                                all.setRecipeMainImage(recipeImage);
                                all.setPostKey(dataSnapshot1.getKey());
                                all.setUid(uid);
                                all.setMediaType(mediaType);  // todo edited 30/07/2019
                                if (dataExists[0]) {
                                    list.add(all);
                                }
                                complete[0] = true;
                            }
                            //previousPostValues.add(all.getPostKey());


                            //list.addAll(Collections.singleton(all));
                        }
                        Collections.reverse(list);
                        if (list.size() == retrieveAmount) {
                            list.remove(list.size() - 1);
                        }
                        listSize[0] = list.size();
                        progressBar.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                        //previousPostValues.remove(0);
                        complete[0] = true;
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            else if (queryType == 2){
                final int[] readFollowers = {0};
                final List<String> PostID = new ArrayList<String>();
                listSize[0] = 0;
                method.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        list.clear();
                        sortByValue.clear();
                        adapter.notifyDataSetChanged();
                        if (dataSnapshot.exists()){
                            int i = 0;
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                // PostID[i] = dataSnapshot1.getKey();
                                PostID.add(dataSnapshot1.getKey());
                                sortByValue.add(dataSnapshot1.child("timestamp").getValue().toString());
                                //i++;
                            }
                            PostID.remove(0);
                            GetFollowersPost(PostID);
                            readFollowers[0] = 1;
                        }
                        else{
                            Toast.makeText(MainActivity.this, "No posts to display", Toast.LENGTH_SHORT).show();
                            readFollowers[0] = 1;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            adapter = new PostsViewHolder(list, MainActivity.this);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this,OrientationHelper.VERTICAL,false);
            //profileRecyclerView.setHasFixedSize(true);
           // linearLayoutManager.setReverseLayout(true);
          //  linearLayoutManager.setStackFromEnd(true);
            mainRecyclerView.setLayoutManager(linearLayoutManager);
            mainRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mainRecyclerView.setAdapter(adapter);

            scrollListener = new EndlessRecyclerListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

                    if ((!endOfDatabaseReached[0]) && (listSize[0] >= retrieveAmount - 1)){// && !loadingFromDatabase[0]) {

                        Query loadMore = null;
                        if (!loadingBarActive[0]){
                            final Posts loadingBar = new Posts(Posts.LOAD_MORE, 0, "", "", "", "", "", "", "", "", "","");
                            list.add(loadingBar);
                            adapter.notifyDataSetChanged();
                            loadingBarActive[0] = true;
                            loadingBarPosition[0] = adapter.getItemCount();
                            //Toast.makeText(MainActivity.this, "Count: " + loadingBarPosition[0], Toast.LENGTH_SHORT).show();
                        }
                        if (!loadingFromDatabase[0]) {

                            if ((recipeSortBy == SORT_BY_NEWEST_RECIPE) && (sortByValue.get(0) != null)) {
                                loadMore = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("timestamp").endAt(sortByValue.get(0)).limitToLast(retrieveAmount);
                            } else if ((recipeSortBy == SORT_BY_RATING) && (sortByValue.get(0) != null)) {
                                loadMore = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("sortByLikes").endAt(sortByValue.get(0)).limitToLast(retrieveAmount);
                            } else if ((recipeSortBy == SORT_BY_FOLLOWERS) && (sortByValue.get(0) != null)) {
                                loadMoreFollowers[0] = FirebaseDatabase.getInstance().getReference().child("FriendsPost").child(CurrentUserID).orderByChild("timestamp").endAt(sortByValue.get(0)).limitToLast(retrieveAmount);
                                //loadMore = FirebaseDatabase.getInstance().getReference().child("FriendsPost").child(CurrentUserID).orderByChild("timestamp").endAt(sortByValue.get(0)).limitToLast(retrieveAmount);
                            }

                            if ((recipeSortBy == SORT_BY_NEWEST_RECIPE || recipeSortBy == SORT_BY_RATING) && (sortByValue.get(0) != null)) {

                                if (loadMore != null) {
                                    loadingFromDatabase[0] = true;
                                    loadMore.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            sortByValue.clear();
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                boolean flagNodata = false;
                                                Posts value = dataSnapshot1.getValue(Posts.class);
                                                Posts all = new Posts(Posts.MAIN_POST_TYPE, 0, "", "", "", "", "", "", "", "", "","");
                                               // String username = value.getUsername();
                                                String title = value.getTitle();
                                                String time = value.getTime();
                                                String date = value.getDate();
                                                String description = value.getDescription();
                                                String status = "";
                                                String mediaType = "";
                                                if (dataSnapshot1.child("Status").exists()){
                                                    status = dataSnapshot1.child("Status").getValue().toString();
                                                }
                                                String recipeImage = "empty";
                                                if (dataSnapshot1.child("recipeMainImage").exists()) {
                                                    recipeImage = dataSnapshot1.child("recipeMainImage").getValue().toString();
                                                }
                                                if (dataSnapshot1.child("mediaType").exists()){
                                                    mediaType = dataSnapshot1.child("mediaType").getValue().toString(); // todo edited 30/07/2019
                                                }
                                                String uid = value.getUid();
                                                if (queryType == 0) {
                                                    sortByValue.add(dataSnapshot1.child("timestamp").getValue().toString());
                                                }
                                                else if(queryType == 1){
                                                    if (dataSnapshot1.child("sortByLikes").exists()){
                                                        sortByValue.add(dataSnapshot1.child("sortByLikes").getValue().toString());
                                                    }
                                                    else{
                                                        flagNodata = true;
                                                    }
                                                }
                                                //all.setUsername(username);

                                                if (!flagNodata) {
                                                    if (!status.equals("suspended")) {
                                                        all.setTitle(title);
                                                        all.setTime(time);
                                                        all.setDate(date);
                                                        all.setDescription(description);
                                                        //all.setProfileImage(profileImage);
                                                        all.setRecipeMainImage(recipeImage);
                                                        all.setMediaType(mediaType); //todo edited 30/07/2019
                                                        all.setPostKey(dataSnapshot1.getKey());
                                                        all.setUid(uid);
                                                        listLoadMore.add(all);
                                                        // currentPostValues.add(all.getPostKey());
                                                    }
                                                }
                                            }

                                            //currentPostValues.remove(0);
                                            Collections.reverse(listLoadMore);
                                            if (listLoadMore.size() >= retrieveAmount - 1) {
                                                listLoadMore.remove(listLoadMore.size() - 1);
                                            } else {
                                                endOfDatabaseReached[0] = true;
                                                Toast.makeText(MainActivity.this, "No more recipes to show", Toast.LENGTH_SHORT).show();
                                            }
                                            list.addAll(listLoadMore);
                                            listSize[0] = list.size();
                                            listLoadMore.clear();
                                            list.remove(loadingBarPosition[0] -1);
                                            loadingBarActive[0] = false;
                                            adapter.notifyDataSetChanged();
                                            loadingFromDatabase[0] = false;
                                           // previousPostValues.addAll(currentPostValues);
                                           // currentPostValues.clear();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }
                            if ((recipeSortBy == SORT_BY_FOLLOWERS) && (sortByValue.get(0) != null)){
                                if (loadMoreFollowers[0] != null) {
                                    loadingFromDatabase[0] = true;
                                    final List<String> PostID = new ArrayList<String>();
                                    loadMoreFollowers[0].addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            sortByValue.clear();
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                    PostID.add(dataSnapshot1.getKey());
                                                    sortByValue.add(dataSnapshot1.child("timestamp").getValue().toString());
                                                }
                                                if (PostID.size() >= retrieveAmount -1){
                                                    PostID.remove(0);
                                                }
                                                else{
                                                    endOfDatabaseReached[0] = true;
                                                }
                                                GetFollowersPost(PostID);
                                                list.remove(loadingBarPosition[0] -1);
                                                loadingBarActive[0] = false;
                                                adapter.notifyDataSetChanged();
                                                loadingFromDatabase[0] = false;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }
                    }
            }
            };

            mainRecyclerView.addOnScrollListener(scrollListener);
        }
    }




    private void GetFollowersPost(final List<String> PostID) {
        final List<String> deletePostID = new ArrayList<String>();
        final ArrayList<Posts> listLoadMore = new ArrayList<>();
        PostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i;
                listLoadMore.clear();
                boolean dataExists = true;
                for (i = 0; i < PostID.size(); i++) {
                    if (dataSnapshot.hasChild(PostID.get(i))) {
                        Posts all = new Posts(Posts.MAIN_POST_TYPE, 0, "", "", "", "", "", "", "", "", "","");
                        //String username = dataSnapshot.child(PostID.get(i)).child("username").getValue().toString();
                        String title = dataSnapshot.child(PostID.get(i)).child("title").getValue().toString();
                        String time = dataSnapshot.child(PostID.get(i)).child("time").getValue().toString();
                        ;
                        String date = dataSnapshot.child(PostID.get(i)).child("date").getValue().toString();
                        String description = dataSnapshot.child(PostID.get(i)).child("description").getValue().toString();
                        //String profileImage = dataSnapshot.child(PostID.get(i)).child("profileImage").getValue().toString();
                        String recipeImage = "0";
                        String mediaType = "";
                        if (dataSnapshot.child(PostID.get(i)).child("recipeMainImage").exists()) {
                            recipeImage = dataSnapshot.child(PostID.get(i)).child("recipeMainImage").getValue().toString();
                        }
                        if (dataSnapshot.child(PostID.get(i)).child("mediaType").exists()){
                            mediaType = dataSnapshot.child(PostID.get(i)).child("mediaType").getValue().toString(); // // todo edited 30/07/2019
                        }
                        String uid = dataSnapshot.child(PostID.get(i)).child("uid").getValue().toString();
                        String status = "";
                        if (dataSnapshot.child("Status").exists()){
                            status = dataSnapshot.child("Status").getValue().toString();
                        }

                        if (dataSnapshot.child("timestamp").exists()) {
                            sortByValue.add(dataSnapshot.child("timestamp").getValue().toString());
                        }


                        if (!status.equals("suspended")) {
                            //all.setUsername(username);
                            all.setTitle(title);
                            all.setTime(time);
                            all.setDate(date);
                            all.setDescription(description);
                            //all.setProfileImage(profileImage);
                            all.setRecipeMainImage(recipeImage);
                            all.setMediaType(mediaType); // todo edited 30/07/2019
                            all.setPostKey(PostID.get(i));
                            all.setUid(uid);
                            listLoadMore.add(all);
                        }

                    }
                    else{
                        deletePostID.add(PostID.get(i));
                    }
                }
                Collections.reverse(listLoadMore);
                list.addAll(listLoadMore);
                listSize[0] = list.size();
                adapter.notifyDataSetChanged();
                DeleteUnknownFollowersPost(deletePostID);
                getFollowersPostComplete = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void DeleteUnknownFollowersPost(final List<String> deletePostID) {
        FollowersPost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (int i = 0; i < deletePostID.size(); i++){
                        dataSnapshot.child(CurrentUserID).child(deletePostID.get(i)).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    public class PostsViewHolder extends RecyclerView.Adapter {

        private ArrayList<Posts> dataSet;
        Context mContext;
        int total_types;

        public class LoadMoreRecipes extends RecyclerView.ViewHolder{

            ProgressBar progressBar;

            public LoadMoreRecipes(@NonNull View itemView) {
                super(itemView);
                progressBar = (ProgressBar) itemView.findViewById(R.id.progress_loadMore);
            }
        }

        public class PostsLayoutHolder extends RecyclerView.ViewHolder {


            TextView UserName, mainTime, mainDate, mainTitle, mainDescription;
            ImageView mainProfileImage, mainRecipeImage;
            ImageButton likePostButton, commentPostButton, playVideoButton;
            TextView displayNumberOfLikes, displayNumberOfComments;
            LinearLayout linearLayoutProfile, llMainStars, llPostComment;
            RelativeLayout relativeLayoutMain;
            ProgressBar imageProgress;
          //  YouTubeThumbnailView youTubeThumbnail;


            DatabaseReference LikesRef, CommentsStatusRef, PostRef, UserRef;
            String CurrentUserID;

            StarController starController = new StarController();


            public PostsLayoutHolder(View itemView) {
                super(itemView);

                this.UserName = (TextView) itemView.findViewById(R.id.TV_MainUsername);
                this.mainTime = (TextView) itemView.findViewById(R.id.TV_MainTime);
                this.mainDate = (TextView) itemView.findViewById(R.id.TV_MainDate);
                this.mainTitle = (TextView) itemView.findViewById(R.id.TV_Title);
                this.mainDescription = (TextView) itemView.findViewById(R.id.TV_Description);
                this.mainProfileImage = (ImageView) itemView.findViewById(R.id.IV_MainUserImage);
                this.mainRecipeImage = (ImageView) itemView.findViewById(R.id.IV_MainPicture);
                this.commentPostButton = (ImageButton) itemView.findViewById(R.id.IB_MainPostMessage);
                this.displayNumberOfComments = (TextView) itemView.findViewById(R.id.TV_CommentsNumber);
                this.likePostButton = (ImageButton) itemView.findViewById(R.id.IB_MainLikeButton);
                this.displayNumberOfLikes = (TextView) itemView.findViewById(R.id.TV_MainNumberOfLikes);
                this.linearLayoutProfile = (LinearLayout) itemView.findViewById(R.id.LL_MainUserProfile);
                this.imageProgress = (ProgressBar) itemView.findViewById(R.id.progressBarImage);
                this.relativeLayoutMain = (RelativeLayout) itemView.findViewById(R.id.RL_Main);
                this.llPostComment = (LinearLayout) itemView.findViewById(R.id.LL_MainComments);
                this.llMainStars = (LinearLayout) itemView.findViewById(R.id.LL_MainStars);
                this.playVideoButton = (ImageButton) itemView.findViewById(R.id.IB_MainPlayButton);
               // this.youTubeThumbnail = (YouTubeThumbnailView) itemView.findViewById(R.id.main_youTubeThumbnail);


                LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                CommentsStatusRef = FirebaseDatabase.getInstance().getReference().child("CommentsStatus");
                PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
                UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
                try {
                    CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                catch (Exception e){
                    signOut();
                }
                imageProgress.getIndeterminateDrawable().setColorFilter(0XFFFF0000, PorterDuff.Mode.SRC_IN);


            }
        }

        public PostsViewHolder(ArrayList<Posts> data, Context context) {
            this.dataSet = data;
            this.mContext = context;
            total_types = dataSet.size();

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            switch (viewType) {
                case Posts.MAIN_POST_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
                    return new PostsLayoutHolder(view);

                case Posts.LOAD_MORE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_progress_bar, parent, false);
                    return new LoadMoreRecipes(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            switch (dataSet.get(position).type) {
                case 0:
                    return Posts.MAIN_POST_TYPE;
                case 2:
                    return Posts.LOAD_MORE;

                default:
                    return -1;
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            final Posts object = dataSet.get(position);


            if (object != null) {
                switch (object.type) {
                    case Posts.MAIN_POST_TYPE:
                        ((PostsLayoutHolder) holder).imageProgress.setVisibility(View.VISIBLE);
                        ((PostsLayoutHolder) holder).mainTitle.setText(object.getTitle());
                        //((PostsLayoutHolder) holder).UserName.setText(object.getUsername());
                        ((PostsLayoutHolder) holder).mainDate.setText("Posted: " + object.getDate() + " ");
                        ((PostsLayoutHolder) holder).mainTime.setText("at " + object.getTime());
                        ((PostsLayoutHolder) holder).mainDescription.setText(object.getDescription());

                        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if(dataSnapshot.child(object.getUid()).exists()){
                                        String username = dataSnapshot.child(object.getUid()).child("username").getValue().toString();
                                        ((PostsLayoutHolder) holder).UserName.setText(username);
                                    }
                                    else{
                                        ((PostsLayoutHolder) holder).UserName.setText("");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        Glide.with(mContext).load(object.recipeMainImage).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ((PostsLayoutHolder) holder).imageProgress.setVisibility(View.GONE);
                                ((PostsLayoutHolder) holder).mainRecipeImage.setVisibility(View.GONE);
                                ((PostsLayoutHolder) holder).playVideoButton.setVisibility(View.GONE); // todo edited 30/07/2019
                                // ((PostsLayoutHolder)holder).relativeLayoutMain.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((PostsLayoutHolder) holder).imageProgress.setVisibility(View.GONE);
                                ((PostsLayoutHolder) holder).playVideoButton.setVisibility(View.GONE);
                                ((PostsLayoutHolder) holder).mainRecipeImage.setVisibility(View.VISIBLE);
                                ///////////////////////// todo edited 30/07/2019
                                if (object.getMediaType().equals("video")){
                                    ((PostsLayoutHolder) holder).playVideoButton.setVisibility(View.VISIBLE);
                                }
                                ///////////////////////
                                return false;
                            }
                        }).into(((PostsLayoutHolder) holder).mainRecipeImage);
/*
                        ((PostsLayoutHolder) holder).youTubeThumbnail.initialize(Constants.DEVELOPER_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {
                                //youTubeThumbnailLoader.setVideo(put youtube link id here);

                                youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                                    @Override
                                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                                        youTubeThumbnailLoader.release(); // release thumbnail loader as it is in an adapter
                                    }

                                    @Override
                                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                                        //Log.e (TAG, "Youtube thumbnail error");
                                    }
                                });
                            }

                            @Override
                            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                            }
                        });
*/
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(20));
                        String UID = object.getUid();
                        final RequestOptions finalRequestOptions = requestOptions;
                        ((PostsLayoutHolder) holder).UserRef.child(UID).child("profileimage").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    object.setProfileImage(dataSnapshot.getValue().toString());
                                    Glide.with(mContext).load(object.profileImage).apply(finalRequestOptions).into(((PostsLayoutHolder) holder).mainProfileImage);
                                }
                                else{
                                    Drawable image = (Drawable)getResources().getDrawable(R.drawable.icon_person);
                                    ((PostsLayoutHolder) holder).mainProfileImage.setImageDrawable(image);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        ((PostsLayoutHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent clickPostIntent = new Intent(MainActivity.this, DisplayFullRecipeActivity.class);
                                clickPostIntent.putExtra("PostKey", object.getPostKey());

                                if (adCounter == 2 && mInterstitialAd.isLoaded() ){

                                    mInterstitialAd.show();
                                    adOpenedChecker = 1;



                                    mInterstitialAd.setAdListener(new AdListener(){
                                        @Override
                                        public void onAdClosed() {
                                            adOpenedChecker = 0;
                                            adCounter = 0;
                                            mInterstitialAd.loadAd(request);
                                            startActivity(clickPostIntent);
                                        }
                                    });
                                }

                               /* else if(adCounter == 2 && mRewardedVideoAd.isLoaded()){
                                    mRewardedVideoAd.show();

                                    mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                                        @Override
                                        public void onRewardedVideoAdLoaded() {

                                        }

                                        @Override
                                        public void onRewardedVideoAdOpened() {

                                        }

                                        @Override
                                        public void onRewardedVideoStarted() {

                                        }

                                        @Override
                                        public void onRewardedVideoAdClosed() {
                                            mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
                                            startActivity(clickPostIntent);
                                        }

                                        @Override
                                        public void onRewarded(RewardItem rewardItem) {

                                        }

                                        @Override
                                        public void onRewardedVideoAdLeftApplication() {

                                        }

                                        @Override
                                        public void onRewardedVideoAdFailedToLoad(int i) {
                                            startActivity(clickPostIntent);
                                        }

                                        @Override
                                        public void onRewardedVideoCompleted() {

                                        }
                                    });

                                }*/
                                else {
                                    startActivity(clickPostIntent);
                                }

                                adCounter++;
                            }
                        });

                        ((PostsLayoutHolder)holder).linearLayoutProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (object.getUid().equals(CurrentUserID)) {
                                    Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                                    startActivity(profileIntent);

                                } else {
                                    PostsRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                final String userID = dataSnapshot.child(object.getPostKey()).child("uid").getValue().toString();
                                                UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()){
                                                            if (dataSnapshot.child(userID).exists()){
                                                                Intent profileIntent = new Intent(MainActivity.this, OthersProfileActivity.class);
                                                                profileIntent.putExtra("visit__user_id", userID);
                                                                startActivity(profileIntent);
                                                            }
                                                            else{
                                                                Toast.makeText(MainActivity.this, "Unable to find users profile", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
                        });

                        ((PostsLayoutHolder)holder).commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey", object.getPostKey());
                                startActivity(commentsIntent);
                            }
                        });

                        ((PostsLayoutHolder)holder).llPostComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey", object.getPostKey());
                                startActivity(commentsIntent);
                            }
                        });


                        ((PostsLayoutHolder)holder).LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(object.getPostKey()).hasChild(((PostsLayoutHolder)holder).CurrentUserID)){
                                    int countLikes = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                    ((PostsLayoutHolder)holder).likePostButton.setImageResource(R.drawable.icon_star_checked);
                                    ((PostsLayoutHolder)holder).displayNumberOfLikes.setText(("Stars: ")+ Integer.toString(countLikes));
                                }
                                else {
                                    int countLikes = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                    ((PostsLayoutHolder)holder).likePostButton.setImageResource(R.drawable.icon_star_unchecked);
                                    ((PostsLayoutHolder)holder).displayNumberOfLikes.setText(("Stars: ")+ Integer.toString(countLikes));
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        ((PostsLayoutHolder)holder).CommentsStatusRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int countComments = (int) dataSnapshot.child(object.getPostKey()).getChildrenCount();
                                ((PostsLayoutHolder)holder).displayNumberOfComments.setText(("Comments: ")+ Integer.toString(countComments));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        ///////////////////////////////////
                        ((PostsLayoutHolder)holder).likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((PostsLayoutHolder)holder).starController.updateStarCount(MainActivity.this, PostsRef, LikesRef, CurrentUserID, object.getPostKey());
                            }
                        });


                        ///////////////////////////////////
/*
                        ((PostsLayoutHolder)holder).likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                likeChecker = true;

                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (likeChecker.equals(true)) {
                                            if (dataSnapshot.child(object.getPostKey()).hasChild(CurrentUserID)) {
                                                LikesRef.child(object.getPostKey()).child(CurrentUserID).removeValue();
                                                likeChecker = false;

                                                PostsRef.child(object.getPostKey()).child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()){
                                                            int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                                            numberOfLikes = numberOfLikes - 1;
                                                            String numberLikes = String.valueOf(numberOfLikes);
                                                            PostsRef.child(object.getPostKey()).child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(MainActivity.this, "Star removed", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    else{
                                                                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            });
                                                        }
                                                        else{
                                                            Toast.makeText(MainActivity.this, "Does not exist", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            } else {
                                                LikesRef.child(object.getPostKey()).child(CurrentUserID).setValue(true);
                                                likeChecker = false;
                                                PostsRef.child(object.getPostKey()).child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()){
                                                            int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                                            numberOfLikes = numberOfLikes + 1;
                                                            String numberLikes = String.valueOf(numberOfLikes);

                                                            PostsRef.child(object.getPostKey()).child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(MainActivity.this, "You gave this recipe a star", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    else{
                                                                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        else {
                                                            PostsRef.child(object.getPostKey()).child("likesnumber").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(MainActivity.this, "You gave this recipe a star", Toast.LENGTH_SHORT).show();
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
*/
                        ((PostsLayoutHolder)holder).llMainStars.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                        break;
                    case Posts.LOAD_MORE:

                        ((LoadMoreRecipes)holder).progressBar.setVisibility(View.VISIBLE);
                        break;

                }

            }
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }


    }



    private void signOut(){         //procedure to Sign User Out
        finish();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();  //Logout from facebook login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //Does not allow the user to come back to this class if the back button is pressed
        startActivity(intent);

    }

    private void createNewRecipe(){
        startActivity(new Intent(MainActivity.this, AddRecipeActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Boolean emailVerified;
        String providerID = "";
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        try {
            for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                providerID = userInfo.getProviderId();
            }
        }
        catch (Exception e){}

        if (currentUser == null){
            SendUserToLoginActivity();
        }
        else{
            emailVerified = currentUser.isEmailVerified();
            if (emailVerified) {
                CheckUserExistence();
            }

            else{

                    if (providerID.equals("facebook.com")){
                        CheckUserExistence();
                    }
                    else if (!providerID.equals("facebook.com")){
                        SendUserToLoginActivity();
                    }
                }
        }
    }


    private void CheckUserExistence()       // Checks if the user already has an account and brings back their UID from firebase
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)){
                      SendUserToSetupActivity();
                }
                if (dataSnapshot.child(current_user_id).hasChild("accountStatus")){
                    String status = dataSnapshot.child(CurrentUserID).child("accountStatus").getValue().toString();
                    if (status.equals("Suspended")){
                        signOut();
                        finish();
                        Toast.makeText(MainActivity.this, "Your account has been temporarily suspended", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetupActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();
        Intent setupIntent = new Intent(MainActivity.this, FirstTimeUserActivity.class);
        setupIntent.putExtra("EMAIL", email);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();           //Does not allow the user to come back to this class if the back button is pressed
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_NETWORK_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DisplayAllUsersPosts(PostRefByTime, 0, SORT_BY_NEWEST_RECIPE);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(this, "Internet Permission not Granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }
}
