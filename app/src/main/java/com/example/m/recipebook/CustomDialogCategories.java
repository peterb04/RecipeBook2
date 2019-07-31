package com.example.m.recipebook;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class CustomDialogCategories {

    public void showCategoriesDialog(Activity activity, String dialogTitle, List<String> dialogMessage){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_categories);

        TextView titleText = (TextView)dialog.findViewById(R.id.dialog_CategoriesTitle);
        titleText.setText(dialogTitle);
        TextView messageText = (TextView) dialog.findViewById(R.id.dialog_CategoriesMessage);
        if (dialogMessage!= null) {
            messageText.setText("");
            for (int i = 0; i < dialogMessage.size(); i++) {
                messageText.setText(messageText.getText() + dialogMessage.get(i) + "\n");
            }
        }

        LinearLayout dialogButton = (LinearLayout) dialog.findViewById(R.id.dialog_CategoriesButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
