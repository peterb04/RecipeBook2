package com.example.m.recipebook.CustomDialogues;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.m.recipebook.R;

public class CustomDialogVerificationEmail extends Dialog implements View.OnClickListener {

    public Activity c;
    public TextView verifyTitleText, verifyMessageText, resendEmailText, verifyDoneText;
    public String verifyTitle, verifyMessage;

    public CustomDialogVerificationEmail(Activity activity, String verifyTitle, String verifyMessage) {
        super(activity);
        this.c = activity;
        this.verifyTitle = verifyTitle;
        this.verifyMessage = verifyMessage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.message_verification_email);

        verifyTitleText = (TextView) findViewById(R.id.message_verificationTitle);
        verifyMessageText = (TextView) findViewById(R.id.message_verificationMessage);
        resendEmailText = (TextView) findViewById(R.id.message_verificationResendEmailLink);
        verifyDoneText = (TextView) findViewById(R.id.message_verificationDone);

        verifyTitleText.setText(verifyTitle);
        verifyMessageText.setText(verifyMessage);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.message_verificationResendEmailLink:
                break;

            case R.id.message_verificationDone:
                dismiss();
                break;

            default:
                break;

        }

    }
}
