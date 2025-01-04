package com.tushar.plinkz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SaveLinkDialog {

    private Context context;
    private String sharedLink; // To store the shared link passed from the activity
    private SaveLinkListener listener;

    // Interface definition for callback
    public interface SaveLinkListener {
        void onSave(String title); // Callback method for saving the title

        void onCancel();
    }



    // Constructor - accepts context, the shared link, and listener

    public SaveLinkDialog(Context context, String sharedLink, SaveLinkListener listener) {
        this.context = context;
        this.sharedLink = sharedLink; // Initialize the shared link
        this.listener = listener; // Initialize the listener
    }

    public void show() {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_save_link, null);

        // Find views in the dialog
        EditText editTitle = dialogView.findViewById(R.id.etLinkTitle);
        TextView linkUrl = dialogView.findViewById(R.id.tvLinkUrl);
        linkUrl.setText(sharedLink); // Set the shared link URL in the TextView

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set up button listeners
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                // Notify the listener that the link should be saved
                listener.onSave(title);
                dialog.dismiss();
                ((Activity) context).finish();

            } else {
                Toast.makeText(context, "Title cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnCancel = dialogView.findViewById(R.id.btnCancel); // Ensure the button exists in the layout
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }
}
