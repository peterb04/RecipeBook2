package com.example.m.recipebook;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialogRecipeImage extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog dialog;
    public String dialogtitle, dialogMessage;
    public String dialogRecipePicture;
    public TextView dialogRecipeImageTitle, dialogRecipeImageMessage, dialogRecipeImageCancelButton, dialogRecipeImageOkButton;
    public ImageView dialogRecipeImage;

    public CustomDialogRecipeImage(Activity activity, String dialogtitle, String dialogMessage, String dialogRecipePicture){
        super(activity);
        this.c = activity;
        this.dialogtitle = dialogtitle;
        this.dialogMessage = dialogMessage;
        this.dialogRecipePicture = dialogRecipePicture;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_recipeimage);
        dialogRecipeImageTitle = (TextView) findViewById(R.id.recipeImageDialogTitle);
        dialogRecipeImageMessage = (TextView) findViewById(R.id.recipeImageDialogMessage);
        dialogRecipeImage = (ImageView) findViewById(R.id.recipeImageDialogImage);
        dialogRecipeImageCancelButton = (TextView) findViewById(R.id.recipeImageDialogCancel);
        dialogRecipeImageOkButton = (TextView) findViewById(R.id.recipeImageDialogAccept);

        dialogRecipeImageTitle.setText(dialogtitle);
        dialogRecipeImageMessage.setText(dialogMessage);
        dialogRecipeImage.setImageURI(Uri.parse(dialogRecipePicture));

        dialogRecipeImageCancelButton.setOnClickListener(this);
        dialogRecipeImageOkButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.recipeImageDialogCancel:
                dismiss();
                break;
            case R.id.recipeImageDialogAccept:
                dismiss();
                break;

            default:
                break;
        }
        dismiss();

    }
}
