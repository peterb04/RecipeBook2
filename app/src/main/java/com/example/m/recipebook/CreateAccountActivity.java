package com.example.m.recipebook;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.m.recipebook.CustomDialogues.CustomDialogVerificationEmail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText createUsername;
    private EditText createPassword, confirmPassword;

    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertDialog;

    String TAG= CreateAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        alertDialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        createUsername = (EditText)findViewById(R.id.ET_CreateUserName);
        createPassword = (EditText)findViewById(R.id.ET_CreatePassword);
        confirmPassword = (EditText) findViewById(R.id.ET_ConfirmPassword);
    }

    public void createAccount(View view) {  //XML onclick listener for create account pushbutton

        final String email = createUsername.getText().toString();
        String password = createPassword.getText().toString();
        String confirmUserPassword = confirmPassword.getText().toString();

        progressDialog = new ProgressDialog(this, R.style.MyDialogTheme);

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";                               // test to make sure user has written email address in correct format
        String emailPattern2 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+\\.+[a-z]+";                               // test to make sure user has written email address in correct format
        if (!email.matches(emailPattern) && !email.matches(emailPattern2)){
            Toast.makeText(CreateAccountActivity.this,"Incorrect Email format",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(confirmUserPassword)){
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmUserPassword)){
            Toast.makeText(this, "Password does not match confirm password", Toast.LENGTH_SHORT).show();
        }

        else {
            //(email.length()>0 && password.length()>0) {
            progressDialog.setTitle("Creating Account");
            progressDialog.setMessage("We are now creating your account...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);


            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                //FirebaseUser user = mAuth.getCurrentUser();
                                //SendUserToSetupActivity(email);
                                //startActivity(new Intent(CreateAccount.this,MainActivity.class)); //tobe completed
                               //Toast.makeText(CreateAccountActivity.this,"Account Created", Toast.LENGTH_SHORT).show();  //toast for testing purposes

                                SendEmailVerificationMessage();
                                progressDialog.dismiss();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(CreateAccountActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                //updateUI(null);
                            }
                        }
                    });
        }
    }
/*
    private void SendUserToSetupActivity(String email) {
        Intent setupIntent = new Intent(CreateAccountActivity.this, FirstTimeUserActivity.class);
        setupIntent.putExtra("EMAIL", email);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
*/


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){               //If user already Registered in then send them to the MainActivity
            SendUserToMainActivity();
        }
    }

    private void SendEmailVerificationMessage() {
        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        String title = "Email Verification";
                        String message = "A verification link has been sent to your email address.  Please follow the instructions in the email to verify your account.";
                        final CustomDialogVerificationEmail customDialogVerificationEmail  = new CustomDialogVerificationEmail(CreateAccountActivity.this, title, message) ;
                        customDialogVerificationEmail.setCancelable(false);
                        customDialogVerificationEmail.show();
                        customDialogVerificationEmail.resendEmailText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(CreateAccountActivity.this, "New verification email sent", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                        customDialogVerificationEmail.verifyDoneText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAuth.signOut();
                                SendUserToLoginActivity();

                            }
                        });
                        /*alertDialog.setTitle("Verify Account");
                        alertDialog.setMessage("A verification link has been sent to your email address.  Email must be verified before you can login");
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(CreateAccountActivity.this, "Please verify your account", Toast.LENGTH_SHORT).show();
                                SendUserToLoginActivity();
                                mAuth.signOut();
                            }
                        });
                        alertDialog.show();
                    */
                    }
                    else {
                        String error = task.getException().getMessage();
                        Toast.makeText(CreateAccountActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(CreateAccountActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //Does not allow the user to come back to this class if the back button is pressed
        startActivity(mainIntent);
        finish();
    }
}
