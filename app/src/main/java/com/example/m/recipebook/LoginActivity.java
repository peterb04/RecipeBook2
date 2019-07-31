package com.example.m.recipebook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.m.recipebook.CustomDialogues.CustomDialogVerificationEmail;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private boolean emailAddressVeified;
    EditText username;
    EditText password;
    Button login;
    Button signUp;
    LoginButton facebookLoginButton;

    CallbackManager callbackManager;
    TextView forgotPasswordLink;

    private ProgressDialog progressDialog;

    String TAG= LoginActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                progressDialog.setTitle("Sign In");
                progressDialog.setMessage("Signing in to your account...");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);

                final String[] fbEmail = new String[1];
                final String[] firstName = new String[1];
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                try {
                                    fbEmail[0] = object.getString("email");
                                    firstName[0] = object.getString("first_name");
                                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                                    handleFacebookAccessToken(loginResult.getAccessToken(), fbEmail[0], firstName[0]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    handleFacebookAccessToken(loginResult.getAccessToken(), fbEmail[0], firstName[0]);
                                }

                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email, first_name");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        mAuth = FirebaseAuth.getInstance();
        username = (EditText) findViewById(R.id.ET_Username);
        password = (EditText) findViewById(R.id.ET_Password);
        login = (Button) findViewById(R.id.B_Login);
        signUp = (Button) findViewById(R.id.B_SignUp);
        forgotPasswordLink = (TextView) findViewById(R.id.TV_LoginForgotPassword);

        progressDialog = new ProgressDialog(this, R.style.MyDialogTheme);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getEmail = username.getText().toString().trim();
                String getPassword = password.getText().toString().trim();
                signIn(getEmail,getPassword);

            }
        });


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToCreateAccountActivity();
            }
        });

        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token, final String email, final String name) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            progressDialog.dismiss();
                            if (user != null){
                                SendUserToMainActivity();
                            }
                            else {
                                SendUserToSetupActivity(email, name);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                        // ...
                    }
                });
    }

    private void SendUserToSetupActivity(String email, String name) {
        Intent setupIntent = new Intent(LoginActivity.this, FirstTimeUserActivity.class);
        setupIntent.putExtra("EMAIL", email);
        setupIntent.putExtra("FIRST_NAME", name);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void SendUserToCreateAccountActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(registerIntent);
    }

    // Sign in Process Firebase
    private void signIn(String email, String password) {
        if (email.length() > 0 && password.length() > 0) {

            progressDialog.setTitle("Sign In");
            progressDialog.setMessage("Signing in to your account...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);


            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                //FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                                //Toast.makeText(LoginActivity.this, "Authentication Success", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                VerifyEmailAddress();
                                progressDialog.dismiss();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                //updateUI(null);
                            }
                        }
                    });


        } else {
            Toast.makeText(this,"Re-Enter Email address and Password",Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        String providerID = null;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        try {
            for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                providerID = userInfo.getProviderId();
            }
            if (currentUser != null) {               //If user already logged in then send them to the MainActivity
                emailAddressVeified = currentUser.isEmailVerified();
                if (emailAddressVeified) {
                    SendUserToMainActivity();
                } else {
                    if (providerID.equals("facebook.com")) {
                        SendUserToMainActivity();
                    }
                }
            }
            /*
            UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("accountStatus").exists()){
                        String status = dataSnapshot.child("accountStatus").getValue().toString();
                        if (status.equals("Suspended")){
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this, "Your account has been temporarily suspended", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            */


        }
        catch (Exception e){
           // Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }

    }

    private void VerifyEmailAddress() {
        final FirebaseUser user = mAuth.getCurrentUser();
        emailAddressVeified = user.isEmailVerified();

        if (emailAddressVeified){
            SendUserToMainActivity();
        }

        else {
            Toast.makeText(this, "Email address must be verified before you can log in.", Toast.LENGTH_SHORT).show();
            mAuth.signOut();

            if (user != null){
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            String title = "Email Verification";
                            String message = "A verification link has been sent to your email address.  Please follow the instructions in the email to verify your account.";
                            final CustomDialogVerificationEmail customDialogVerificationEmail  = new CustomDialogVerificationEmail(LoginActivity.this, title, message) ;
                            customDialogVerificationEmail.setCancelable(false);
                            customDialogVerificationEmail.show();
                            customDialogVerificationEmail.resendEmailText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(LoginActivity.this, "New verification email sent", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                            customDialogVerificationEmail.verifyDoneText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    recreate();
                                    mAuth.signOut();
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
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    }
                });
            }
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  //Does not allow the user to come back to this class if the back button is pressed
        startActivity(mainIntent);
        finish();
    }


}
