package com.example.m.recipebook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddDirections extends AppCompatActivity {

    private Button save, stepByStep, add;
    private ListView lvAddDirections;
    private EditText writeDirections;
    private int trackStep = 1, callingClass = 0, EDIT_POST_ACTIVITY = 5, savedRecipeTracker = 0;

    private String title;
    private String description;
    private String postKey;
    private String current_user_id;
    private String  recipeImage;
    private String savedTableID;
    private int editFinishLater;
    private List<String> ingredients, equipment, directions  = new ArrayList<String>();
    private List<String> dirRet = new ArrayList<String>();
    private List<String> tempDir = new ArrayList<String>();
    private List<String> category = new ArrayList<String>();

    private DatabaseReference postRef;
    private FirebaseAuth mAuth;

    private ArrayAdapter<String> aAddAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_directions);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarMethod);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        lvAddDirections = (ListView) findViewById(R.id.add_directions_listview);
        save = (Button) findViewById(R.id.B_SaveDirections);
        //stepByStep = (Button) findViewById(R.id.B_StepByStep);
        add = (Button) findViewById(R.id.B_AddDirectionsPopup);
        writeDirections = (EditText) findViewById(R.id.ET_addDirectionsPopup);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();

        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        //Retrieve information that was sent from the AddRecipe Activity
        final Intent intent = getIntent();
        title = intent.getStringExtra("TITLE");
        recipeImage = intent.getStringExtra("RECIPEIMAGE");
        description = intent.getStringExtra("DESCRIPTION");
        ingredients = intent.getStringArrayListExtra("INGREDIENTS");
        equipment = intent.getStringArrayListExtra("EQUIPMENT");
        directions = intent.getStringArrayListExtra("DIRECTIONS");
        category = intent.getStringArrayListExtra("CATEGORY");
        callingClass = intent.getIntExtra("CLASS",0);
        postKey = intent.getStringExtra("PostKey");
        savedRecipeTracker = intent.getIntExtra("SavedRecipeTracker",0);
        editFinishLater = intent.getIntExtra("EDIT", 0);
        savedTableID = intent.getStringExtra("TABLE_ID");

        if (callingClass == EDIT_POST_ACTIVITY){
            postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        }
        //Save array list line by line, otherwise both variable will be automatically updated whenever one is adjusted
        if (directions != null) {

        }

        if (directions != null){
            dirRet = directions;
        }
        //List Adapter
        aAddAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dirRet);
        lvAddDirections.setAdapter(aAddAdapter);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = addPressed();
                if (text != null){              //if text entered add to variable
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDirections();
            }
        });
        lvAddDirections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                editLine(view, position);
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

    private String addPressed(){

        String text = writeDirections.getText().toString();
        int rowLimit = 15;
        if (!text.matches("")){
            int adapterSize = aAddAdapter.getCount();
            if (adapterSize < rowLimit) {
                String trial = "Step " + (dirRet.size() + 1) + ":  " + text;       //add text to list
                trackStep++;

                aAddAdapter.add(trial);
                aAddAdapter.notifyDataSetChanged();
                writeDirections.setText("");
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                scrollMyListViewToBottom();
                return text;
            }
            else
            {
                Toast.makeText(this, "Limit reached", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return null;
    }

    private void scrollMyListViewToBottom() {
        lvAddDirections.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                lvAddDirections.setSelection(aAddAdapter.getCount() - 1);
            }
        });
    }

    private void saveDirections(){                  //Gets all the values and rearranges them back into the right order to be sent back
        if(directions != null){
            tempDir.addAll(dirRet);
            dirRet.clear();
            dirRet.addAll(tempDir);
        }
        if (callingClass != EDIT_POST_ACTIVITY) {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            intent.putExtra("TITLEret", title);
            intent.putExtra("RECIPEIMAGE", recipeImage);
            intent.putExtra("DESCRIPTIONret", description);
            intent.putStringArrayListExtra("EQUIPret", (ArrayList<String>) equipment);
            intent.putStringArrayListExtra("INGREDret", (ArrayList<String>) ingredients);
            intent.putStringArrayListExtra("CATEGORY", (ArrayList<String>) category);
            intent.putStringArrayListExtra("DIRret", (ArrayList<String>) dirRet);
            intent.putExtra("SavedRecipeTracker", savedRecipeTracker);
            intent.putExtra("EDIT", editFinishLater);
            intent.putExtra("TABLE_ID", savedTableID);
            startActivity(intent);
            finish();
        }

        else if (callingClass == EDIT_POST_ACTIVITY){
            HashMap postMap = new HashMap();
            postMap.put("Directions", dirRet);
            postRef.updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddDirections.this, "Directions Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(AddDirections.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Intent intent = new Intent(this, EditPostActivity.class);
            intent.putExtra("PostKey",postKey);
            startActivity(intent);
            finish();
        }
    }

    private void editLine(final View view, final int position){
        //This part is only temporary and will be changed.  Will use an xml layout to make a popup window
       // Toast.makeText(AddDirections.this,"row selected",Toast.LENGTH_SHORT).show();
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        TextView titl= new TextView(this);
        titl.setText("Edit");
        titl.setPadding(10,10,10,10);
        titl.setGravity(Gravity.CENTER);
        titl.setTextColor(Color.BLACK);
        titl.setTextSize(20);
        alertDialog.setCustomTitle(titl);

        //EditText
        // Copies and Fills in writing to the EditText popup box from the list so that the user can adjust the writing.
        final EditText newText = new EditText(this);
        newText.setText(dirRet.get(position));
        newText.setGravity(Gravity.CENTER_HORIZONTAL);
        newText.setTextColor(Color.BLACK);
        alertDialog.setView(newText);

        // Set Button
        // you can more buttons
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String temp = newText.getText().toString();
                dirRet.get(position);
                dirRet.set(position, temp);
                aAddAdapter.notifyDataSetChanged();
            }

        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"CANCEL", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Perform Action on Button
            }

        });

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dirRet.remove(position);
                aAddAdapter.notifyDataSetChanged();
            }
        });


        new Dialog(getApplicationContext());
        alertDialog.show();
        // Set Properties for OK Button
        final Button okBT = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams neutralBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        neutralBtnLP.gravity = Gravity.FILL_HORIZONTAL;
        okBT.setPadding(50, 10, 10, 10);   // Set Position
        okBT.setTextColor(Color.BLUE);
        okBT.setLayoutParams(neutralBtnLP);

        final Button cancelBT = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams negBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        negBtnLP.gravity = Gravity.FILL_HORIZONTAL;
        cancelBT.setTextColor(Color.RED);
        cancelBT.setLayoutParams(negBtnLP);
    }
}
