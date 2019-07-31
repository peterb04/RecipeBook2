package com.example.m.recipebook;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class CustomDialogEditText extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public String dialogtitle, dialogEditText;
    public TextView dialogEditTextTitle, dialogEditTextCancelButton, dialogEditTextOkButton;
    public EditText dialogTextEditText;

    public CustomDialogEditText(Activity activity, String dialogtitle, String dialogEditText){

        super(activity);
        this.c = activity;
        this.dialogtitle = dialogtitle;
        this.dialogEditText = dialogEditText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_edittext);
        dialogEditTextTitle = (TextView) findViewById(R.id.editTextDialogTitle);
        dialogTextEditText = (EditText) findViewById(R.id.editTextDialogText);
        dialogEditTextCancelButton = (TextView) findViewById(R.id.editTextDialogCancel);
        dialogEditTextOkButton = (TextView) findViewById(R.id.editTextDialogAccept);

        dialogEditTextTitle.setText(dialogtitle);
        dialogTextEditText.setText(dialogEditText);

        dialogEditTextCancelButton.setOnClickListener(this);
        dialogEditTextOkButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.editTextDialogCancel:
                dismiss();
                break;
            case R.id.editTextDialogAccept:
                dismiss();
                break;

            default:
                break;
        }
        dismiss();

    }
}
