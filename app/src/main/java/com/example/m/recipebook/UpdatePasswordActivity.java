package com.example.m.recipebook;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdatePasswordActivity extends AppCompatActivity {

    private EditText oldPassword, newPassword, repeatedNewPassword;
    private Button cancelButton, confirmButton;
    private FirebaseUser User;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        User = FirebaseAuth.getInstance().getCurrentUser();
        final String Email = User.getEmail();

        oldPassword = (EditText) findViewById(R.id.ET_UpdatePasswordCurrent);
        newPassword = (EditText) findViewById(R.id.ET_UpdatePasswordNew);
        repeatedNewPassword = (EditText) findViewById(R.id.ET_UpdatePasswordRepeat);
        cancelButton = (Button) findViewById(R.id.B_UpdatePasswordCancel);
        confirmButton = (Button) findViewById(R.id.B_UpdatePasswordConfirm);

        progressDialog = new ProgressDialog(UpdatePasswordActivity.this);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPass = "", newPass = "", confirmPass = "";
                oldPass = oldPassword.getText().toString();
                newPass = newPassword.getText().toString();
                confirmPass = repeatedNewPassword.getText().toString();

                if (TextUtils.isEmpty(oldPass)){
                    Toast.makeText(UpdatePasswordActivity.this, "Current password required.", Toast.LENGTH_LONG).show();
                }
                else if (TextUtils.isEmpty(newPass) || newPass.length() < 6){
                    Toast.makeText(UpdatePasswordActivity.this, "New password must have at least six characters", Toast.LENGTH_LONG).show();
                }
                else if (!newPass.equals(confirmPass)){
                    Toast.makeText(UpdatePasswordActivity.this, "Confirm password does not match your new password.", Toast.LENGTH_LONG).show();
                }
                else{
                    progressDialog.setTitle("Change Password");
                    progressDialog.setMessage("Password is being updated");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();
                    ChangePassword(Email, oldPass, newPass);
                }

            }
        });
    }

    private void ChangePassword(String email, String oldPassword, final String NewPassword) {

        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        User.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    User.updatePassword(NewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(UpdatePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                finish();
                            }
                            else{
                                Toast.makeText(UpdatePasswordActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }

                else{
                    Toast.makeText(UpdatePasswordActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }
}
