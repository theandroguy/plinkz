package com.tushar.plinkz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tushar.plinkz.Modal.AppDatabase;
import com.tushar.plinkz.Modal.Link;
import com.tushar.plinkz.Modal.LinkDao;

import java.util.HashMap;
import java.util.Map;

public class LinkViewRecyclerActivity extends AppCompatActivity {

    private static final String TAG = "LinkViewRecyclerActivity";

    // Firebase instances
    private FirebaseFirestore firebaseDb;
    private FirebaseAuth mAuth;

    // Room database instance
    private AppDatabase localDb;
    private LinkDao linkDao;

    private boolean isLinkBeingSaved = false;
    private boolean isIntentHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_link_view_recyler);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase components
        firebaseDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Room Database
        localDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "links-database").build();
        linkDao = localDb.linkDao();

        // Button to view saved links
        Button viewLinksButton = findViewById(R.id.btn_view_links);
        viewLinksButton.setOnClickListener(v -> {
            // Navigate to the activity that shows saved links
            Intent intent = new Intent(LinkViewRecyclerActivity.this, LinkListActivity.class);
            startActivity(intent);
        });

        // Handle incoming share intent
        handleIncomingShareIntent();
    }




    private void handleIncomingShareIntent() {
        if (isIntentHandled) return;
        isIntentHandled = true;

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        Log.d("ShareIntent", "Action: " + action + ", Type: " + type);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    showSaveLinkDialog(sharedText);
                }
            }
        }
    }

    private void showSaveLinkDialog(final String url) {
        if (isLinkBeingSaved) return;
        isLinkBeingSaved = true;

        SaveLinkDialog dialog = new SaveLinkDialog(this, url, new SaveLinkDialog.SaveLinkListener() {
            @Override
            public void onSave(String title) {
                saveLinkToFirebase(title, url);
            }

            @Override
            public void onCancel() {
                isLinkBeingSaved = false;
            }
        });
        dialog.show();
    }

    private void saveLinkToFirebase(String title, String url) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to save links", Toast.LENGTH_SHORT).show();
            isLinkBeingSaved = false;
            return;
        }

        String userId = currentUser.getUid();

        // Check if the link already exists for this user
        firebaseDb.collection("users").document(userId).collection("links")
                .whereEqualTo("url", url)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Link doesn't exist for this user, proceed with saving
                        Map<String, Object> linkData = new HashMap<>();
                        linkData.put("title", title);
                        linkData.put("url", url);

                        firebaseDb.collection("users").document(userId).collection("links")
                                .add(linkData)
                                .addOnSuccessListener(documentReference -> {
                                    String id = documentReference.getId();
                                    saveLinkLocally(id, title, url,userId);
                                    Log.d("Firebase", "Link saved with ID: " + id);
                                    Toast.makeText(this, "Link saved successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error saving link", e);
                                    Toast.makeText(this, "Failed to save link", Toast.LENGTH_SHORT).show();
                                    isLinkBeingSaved = false;
                                });
                    } else {
                        Log.d("Firebase", "Link already exists for this user: " + url);
                        Toast.makeText(this, "This link is already saved", Toast.LENGTH_SHORT).show();
                        isLinkBeingSaved = false;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error checking for existing link", e);
                    Toast.makeText(this, "Error checking for existing link", Toast.LENGTH_SHORT).show();
                    isLinkBeingSaved = false;
                });
    }

    private void saveLinkLocally(String id, String title, String url, String userID) {
        new Thread(() -> {
            try {
                Link link = new Link(id, title, url, userID );
                linkDao.insert(link);
                Log.d("Local DB", "Link saved locally: " + title);
            } catch (Exception e) {
                Log.e("Local DB", "Error saving link locally", e);
            }
            runOnUiThread(() -> isLinkBeingSaved = false);
        }).start();
    }
}