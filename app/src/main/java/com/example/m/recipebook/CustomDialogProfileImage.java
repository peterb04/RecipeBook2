package com.example.m.recipebook;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialogProfileImage extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog dialog;
    public String dialogtitle, dialogMessage;
    public String dialogProfilePicture;
    public TextView dialogProfileImageTitle, dialogProfileImageMessage, dialogProfileImageCancelButton, dialogProfileImageOkButton;
    public ImageView dialogProfileImage;

    public CustomDialogProfileImage(Activity activity, String dialogtitle, String dialogMessage, String dialogProfilePicture){
        super(activity);
        this.c = activity;
        this.dialogtitle = dialogtitle;
        this.dialogMessage = dialogMessage;
        this.dialogProfilePicture = dialogProfilePicture;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_profileimage);
        dialogProfileImageTitle = (TextView) findViewById(R.id.profileImageDialogTitle);
        dialogProfileImageMessage = (TextView) findViewById(R.id.profileImageDialogMessage);
        dialogProfileImageCancelButton = (TextView) findViewById(R.id.profileImageDialogCancel);
        dialogProfileImageOkButton = (TextView) findViewById(R.id.profileImageDialogAccept);
        dialogProfileImage = (ImageView) findViewById(R.id.profileImageDialogImage);

        dialogProfileImageTitle.setText(dialogtitle);
        dialogProfileImageMessage.setText(dialogMessage);
        dialogProfileImage.setImageURI(Uri.parse(dialogProfilePicture));

        dialogProfileImageCancelButton.setOnClickListener(this);
        dialogProfileImageOkButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.profileImageDialogCancel:
                dismiss();
                break;

            case R.id.profileImageDialogAccept:
                dismiss();
                break;

            default:
                break;
        }
        dismiss();

    }

}
