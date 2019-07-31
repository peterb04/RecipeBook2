package com.example.m.recipebook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String,List<String>> expandableListDetail;
    private int CLASS = 0, index = 0, savedRecipeTracker = 0;
    private String title;
    private String description;
    private String postKey;
    private String recipeImage;
    private String savedTableID;
    private int editFinishLater;
    private List<String> ingredients = new ArrayList<String>();
    private List<String> equipment = new ArrayList<String>();
    private List<String> directions = new ArrayList<String>();
    private List<String> categories = new ArrayList<String>();
    private List<String> savedCategories = new ArrayList<String>();
    private Integer [][] item = new Integer[10][10];
    private LinearLayout llChildItem;


    private Button saveCategoriesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        llChildItem = (LinearLayout) findViewById(R.id.LL_ChildItem);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarCategories);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Categories");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        final Intent intent = getIntent();
        CLASS = getIntent().getIntExtra("CLASS",0);
        title = intent.getStringExtra("TITLE");
        recipeImage = intent.getStringExtra("RECIPEIMAGE");
        description = intent.getStringExtra("DESCRIPTION");
        ingredients = intent.getStringArrayListExtra("INGREDIENTS");
        equipment = intent.getStringArrayListExtra("EQUIPMENT");
        directions = intent.getStringArrayListExtra("DIRECTIONS");
        postKey = intent.getStringExtra("PostKey");
        savedCategories = intent.getStringArrayListExtra("CATEGORIES");

        savedRecipeTracker = intent.getIntExtra("SavedRecipeTracker",0);
        editFinishLater = intent.getIntExtra("EDIT", 0);
        savedTableID = intent.getStringExtra("TABLE_ID");

        saveCategoriesButton = (Button) findViewById(R.id.B_SaveCategoriesButton) ;
        if (CLASS == 0){
            saveCategoriesButton.setEnabled(false);
            saveCategoriesButton.setVisibility(View.INVISIBLE);
        }

        if (CLASS == 5){
            if (savedCategories != null){
                categories.addAll(savedCategories);
            }
        }

        expandableListView = (ExpandableListView) findViewById(R.id.expanableListViewCat);
        expandableListDetail = com.example.m.recipebook.ExpandableListAdapter.getData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new com.example.m.recipebook.ExpandableListAdapter(this, expandableListTitle, expandableListDetail);

        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
               // Toast.makeText(getApplicationContext(),expandableListTitle.get(i) + "ListView Open", Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                //Toast.makeText(getApplicationContext(),expandableListTitle.get(i) + "ListView Closed", Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {

                final String category = (String) expandableListAdapter.getChild(groupPosition, childPosition);
                view.setSelected(true);

                //new KeepTrackOfSelectedItems(groupPosition, childPosition, view);
                if (!view.isActivated()) {
                    view.setActivated(true);
                   // Toast.makeText(getApplicationContext(), expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)+ " selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    view.setActivated(false);
                   // Toast.makeText(getApplicationContext(), expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition)+ " removed", Toast.LENGTH_SHORT).show();
                }

                if (CLASS == 0) {
                    FindRecipesInSelecectedCategory(category);
                }
                else if (CLASS == 1 || CLASS == 5){
                    if (categories.contains(category)){
                        categories.remove(category);
                        if (CLASS == 5){
                            Query CategoriesQuery = FirebaseDatabase.getInstance().getReference().child("Categories");
                            CategoriesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        String check = dataSnapshot.child(category).child(postKey).getKey();
                                        if (check != null) {
                                            if (check.equals(postKey)) {
                                                dataSnapshot.child(category).child(postKey).getRef().removeValue();
                                            }
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
                        int numberOfCategories = categories.size();
                        if (numberOfCategories < 4){
                            categories.add(category);
                        }
                        else {
                            Toast.makeText(CategoriesActivity.this, "Number of categories limit reached", Toast.LENGTH_SHORT).show();
                        }



                    }

                    final String selectedCategories = "";
                    final List<String> categoriesMessage = new ArrayList<String>();

                    if (categories != null){
                        for (int i = 0; i < categories.size(); i++) {
                            //selectedCategories.equals(selectedCategories + categories.get(i) + "\n");
                            categoriesMessage.add(i,categories.get(i));
                        }
                    }
                    CustomDialogCategories alert = new CustomDialogCategories();
                    alert.showCategoriesDialog(CategoriesActivity.this,"Selected Categories",categoriesMessage);
                }
                return true;
            }
        });

        saveCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CLASS == 1) {
                    AddCategories(categories);
                }
                else{
                    EditActivity(categories);
                }
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

    private void AddCategories(List<String> categories) {
        Intent intent = new Intent(CategoriesActivity.this, AddRecipeActivity.class);                     //Sends all these values to the next activity
        intent.putStringArrayListExtra("CATEGORY", (ArrayList<String>) categories);
        intent.putExtra("TITLEret", title);
        intent.putExtra("RECIPEIMAGE", recipeImage);
        intent.putExtra("DESCRIPTIONret", description);
        intent.putStringArrayListExtra("INGREDret", (ArrayList<String>) ingredients);//ingredient);
        intent.putStringArrayListExtra("EQUIPret", (ArrayList<String>) equipment);//equipments);
        intent.putStringArrayListExtra("DIRret", (ArrayList<String>) directions);
        intent.putExtra("EDIT", editFinishLater);
        intent.putExtra("TABLE_ID", savedTableID);
        CategoriesActivity.this.startActivity(intent);
        finish();
    }

    private void EditActivity(List<String> categories) {
        final ProgressDialog progressDialog = new ProgressDialog(CategoriesActivity.this);
        progressDialog.setTitle("Uploading Categories");
        progressDialog.setMessage("Saving categories to database");
        progressDialog.show();
        DatabaseReference CategoriesRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        for (int i=0 ; i < categories.size(); i++){
            CategoriesRef.child(categories.get(i)).child(postKey).child("category").setValue(categories.get(i)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CategoriesActivity.this, "Categories uploaded successfully ", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        Toast.makeText(CategoriesActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
        DatabaseReference PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        HashMap postmap = new HashMap();
        postmap.put("Categories", categories);
        PostRef.updateChildren(postmap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                }
                else {
                    Toast.makeText(CategoriesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("PostKey",postKey);
        startActivity(intent);
        finish();
    }

    private void FindRecipesInSelecectedCategory(String category) {
        Intent intent = new Intent(CategoriesActivity.this, FindRecipeActivity.class);                     //Sends all these values to the next activity
        intent.putExtra("Category", category);
        intent.putExtra("CLASS", 1);  // 1 represents this CategoriesActivity class
        CategoriesActivity.this.startActivity(intent);
        finish();
    }
}
