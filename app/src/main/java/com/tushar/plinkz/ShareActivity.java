package com.tushar.plinkz;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tushar.plinkz.Modal.Link;

public class ShareActivity extends AppCompatActivity {

    private DatabaseReference databaseReference; // Firebase database reference
    private FirebaseAuth mAuth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        // Initialize Firebase Database and Auth
        databaseReference = FirebaseDatabase.getInstance().getReference("links");
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        // Handle the received link
        handleIncomingIntent();
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendLink(intent); // Handle text being sent
            }
        }
    }

    private void handleSendLink(Intent intent) {
        String sharedLink = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedLink != null) {
            // Show loading UI
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            findViewById(R.id.sharing_text).setVisibility(View.VISIBLE);

            // Delay for demonstration (remove this in production)
            new Handler().postDelayed(() -> {
                showTitleInputDialog(sharedLink);
            }, 1000); // Simulated delay for loading
        }
    }

    private void showTitleInputDialog(final String sharedLink) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Title for the Link");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {
                    saveLink(title, sharedLink);
                } else {
                    Toast.makeText(ShareActivity.this, "Title cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveLink(String title, String url) {
        // Retrieve the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to save links.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid(); // Get the user ID
        String linkId = databaseReference.push().getKey();
        Link link = new Link(linkId, title, url, userId); // Include user ID in Link

        databaseReference.child(linkId).setValue(link).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // TODO: Save link locally in Room or SQLite
                // Example: linkDao.insert(link);
                Toast.makeText(ShareActivity.this, "Link saved successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity after saving
            } else {
                Toast.makeText(ShareActivity.this, "Failed to save link!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
