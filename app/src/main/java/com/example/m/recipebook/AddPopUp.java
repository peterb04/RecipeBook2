package com.example.m.recipebook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
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
import java.util.LinkedHashSet;
import java.util.List;

public class AddPopUp extends AppCompatActivity {

    private ListView addListview;
    private EditText newItem;
    private int i = 0, CLASS = 0, EDIT_POST_ACTIVITY = 5, savedRecipeTracker = 0;
    private int INGREDIENTS = 0, EQUIPMENT = 1, EDIT_POST_ACT_REC = 2, EDIT_POST_ACT_EQUIP = 3, option;
    List<String> userInput = new ArrayList<String>();                       //Make these variables as a List
    List<String> addEquipment = new ArrayList<String>();
    List<String> ingredients = new ArrayList<String>();
    List<String> directions = new ArrayList<String>();
    List<String> category = new ArrayList<String>();
    private Button addButton, save, cancel;
    private ArrayAdapter<String> addAdapter;
    private String title;
    private String description;
    private String PostKey;
    private String current_user_id;
    private String  recipeImage;
    private String savedTableID;
    private int editFinishLater;

    private DatabaseReference postRef;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_recipe_popup);
        this.setFinishOnTouchOutside(false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (Build.VERSION.SDK_INT == 26){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();

        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        addListview = (ListView)findViewById(R.id.add_listview);
        newItem = (EditText)findViewById(R.id.ET_addIngredientPopup);
        addButton = (Button)findViewById(R.id.B_AddIngredientPopup);
        save = (Button)findViewById(R.id.B_Save);
        cancel = (Button)findViewById(R.id.B_PopupCancel);

        //Retrieve information that was sent from the AddRecipe Activity
        final Intent intent = getIntent();
        title = intent.getStringExtra("TITLE");
        recipeImage = intent.getStringExtra("RECIPEIMAGE");
        description = intent.getStringExtra("DESCRIPTION");
        ingredients = intent.getStringArrayListExtra("INGREDIENTS");
        addEquipment = intent.getStringArrayListExtra("EQUIPMENT");
        directions = intent.getStringArrayListExtra("DIRECTIONS");
        category = intent.getStringArrayListExtra("CATEGORY");
        option = intent.getIntExtra("OPTION",0);
        PostKey = intent.getStringExtra("PostKey");
        CLASS = intent.getIntExtra("CLASS", 0);

        savedRecipeTracker = intent.getIntExtra("SavedRecipeTracker",0);
        editFinishLater = intent.getIntExtra("EDIT", 0);
        savedTableID = intent.getStringExtra("TABLE_ID");

        if (CLASS == EDIT_POST_ACTIVITY){
            postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        }

        if (option == INGREDIENTS  || option == EDIT_POST_ACT_REC){
            setTitle("Ingredients");

            if (ingredients != null){
                userInput = ingredients;
            }
        }

        if (option == EQUIPMENT  || option == EDIT_POST_ACT_EQUIP){
            setTitle("Equipment");
            if (addEquipment != null){
                userInput = addEquipment;
            }
        }
        addAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,userInput);
        addListview.setAdapter(addAdapter);

        //OnClickListener for when the add button is pressed
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String val = addButtonPressed();
                if (val != null) {
                }
            }
        });

        //OnclickListener for when save button is pressed
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveItems();      //Retrieve Items from listView
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRoutine();
            }
        });

        addListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                editList(view, position);
            }
        });

    }


    private void cancelRoutine() {
        finish();
    }

    //Subroutine for when the add button is pressed
    private String addButtonPressed(){


        String item = newItem.getText().toString();
        int rowLimit = 15;
        if (!item.matches("")){             //ignore if no text is entered
            int adapterSize = addAdapter.getCount();
            if (adapterSize < rowLimit) {


                addAdapter.add(item);                      //Procedure to display the item into the list
                addAdapter.notifyDataSetChanged();
                newItem.setText("");                    //Empties the textView

                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                scrollMyListViewToBottom();

                return item;
            }
            else{
                Toast.makeText(this, "Limit reached.", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        else
            {
            return null;                //return nothin if no text was entered
        }

    }

    //Subroutine for when the saved button is pressed
    private void retrieveItems(){
        LinkedHashSet<String> hashSet = new LinkedHashSet<String>();            // remove any duplicates in list
        if (option == INGREDIENTS  && ingredients != null){
            hashSet.addAll(ingredients);
        }
        else if (option == EQUIPMENT && addEquipment != null){
            hashSet.addAll(addEquipment);
        }

        hashSet.addAll(userInput);              //Will check and erase any duplicates so that no duplicates are sent back to the AddRecipe Activity
        userInput.clear();
        userInput.addAll(hashSet);

        if (CLASS != EDIT_POST_ACTIVITY) {


            Intent intent = new Intent(this, AddRecipeActivity.class);              //Procedure to send all data back to the AddRedip Activity
            intent.putExtra("TITLEret", title);
            intent.putExtra("RECIPEIMAGE", recipeImage);
            intent.putExtra("DESCRIPTIONret", description);
            intent.putStringArrayListExtra("DIRret", (ArrayList<String>) directions);
            intent.putStringArrayListExtra("CATEGORY", (ArrayList<String>) category);
            intent.putExtra("OPTION", option);
            intent.putExtra("SavedRecipeTracker", savedRecipeTracker);
            intent.putExtra("EDIT", editFinishLater);
            intent.putExtra("TABLE_ID", savedTableID);
            if (option == INGREDIENTS) {
                intent.putStringArrayListExtra("EQUIPret", (ArrayList<String>) addEquipment);       //Sends back information for equipment if Ingredients was selected
                intent.putStringArrayListExtra("INGREDret", (ArrayList<String>) userInput);
            } else if (option == EQUIPMENT) {
                intent.putStringArrayListExtra("INGREDret", (ArrayList<String>) ingredients);        //Sends back information for Ingredients if equipment was selected
                intent.putStringArrayListExtra("EQUIPret", (ArrayList<String>) userInput);
            }
            startActivity(intent);              //Go to the AddRecipe Activity
            finish();
        }
        else if (CLASS == EDIT_POST_ACTIVITY){
            if (option == EDIT_POST_ACT_REC) {
                HashMap postMap = new HashMap();
                postMap.put("Ingredients", userInput);
                postRef.updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(AddPopUp.this, "Recipe uploaded successfully ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddPopUp.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else if (option == EDIT_POST_ACT_EQUIP){
                HashMap postMap = new HashMap();
                postMap.put("Equipment", userInput);
                postRef.updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(AddPopUp.this, "Equipment uploaded successfully ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddPopUp.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            Intent intent = new Intent(this, EditPostActivity.class);
            intent.putExtra("PostKey",PostKey);
            startActivity(intent);
            finish();
        }
    }

    private void scrollMyListViewToBottom() {
        addListview.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                addListview.setSelection(addAdapter.getCount() - 1);
            }
        });
    }

    private void editList(final View view, final int position){
        //Toast.makeText(AddPopUp.this,"row selected",Toast.LENGTH_SHORT).show();
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        TextView titl= new TextView(this);
        titl.setText("Edit");
        titl.setPadding(10,10,10,10);
        titl.setGravity(Gravity.CENTER);
        titl.setTextColor(Color.BLACK);
        titl.setTextSize(20);
        alertDialog.setCustomTitle(titl);

        TextView msg = new TextView(this);

        // Message Properties
        msg.setText("I am a Custom Dialog Box. \n Please Customize me.");
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        msg.setTextColor(Color.BLACK);
        alertDialog.setView(msg);

        //EditText
        final EditText newText = new EditText(this);
        newText.setText(userInput.get(position));
        newText.setGravity(Gravity.CENTER_HORIZONTAL);
        newText.setTextColor(Color.BLACK);
        alertDialog.setView(newText);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"DELETE", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                userInput.remove(position);
                addAdapter.notifyDataSetChanged();
            }

        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"CANCEL", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Perform Action on Button
            }

        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String temp = newText.getText().toString();
                userInput.get(position);
                List<String> temp2 = new ArrayList<String>();
                userInput.set(position, temp);
                for (int i = 0; i < userInput.size(); i++) {
                    temp2.add(userInput.get(i));
                }
                addAdapter.notifyDataSetChanged();
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
