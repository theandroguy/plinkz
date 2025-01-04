package com.tushar.plinkz;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tushar.plinkz.Adapter.LinkAdapter;
import com.tushar.plinkz.Modal.AppDatabase;
import com.tushar.plinkz.Modal.Link;
import com.tushar.plinkz.Modal.LinkDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LinkListActivity extends AppCompatActivity implements LinkAdapter.OnLinkDeleteListener {

    private RecyclerView recyclerView;
    private LinkAdapter linkAdapter;
    private List<Link> linkList = new ArrayList<>();
    private List<Link> originalLinkList = new ArrayList<>();
    private Set<String> uniqueUrls = new HashSet<>();

    private AppDatabase localDb;
    private LinkDao linkDao;

    private FirebaseFirestore firebaseDb;
    private FirebaseAuth mAuth;
    private String userId;

    private EditText searchEditText;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_list);

        fab=findViewById(R.id.fab);



        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "Please log in to access your links", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if not logged in
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        linkAdapter = new LinkAdapter(linkList, this);
        recyclerView.setAdapter(linkAdapter);

        localDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "links-database").build();
        linkDao = localDb.linkDao();

        firebaseDb = FirebaseFirestore.getInstance();

        searchEditText = findViewById(R.id.searchBar);

        fetchLinks();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LinkListActivity.this,ManualSaveLinkActivity.class);
                startActivity(intent);
            }
        });



        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLinks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected called with item id: " + item.getItemId());
        if (item.getItemId() == R.id.action_sign_out) {
            Log.d(TAG, "Sign out menu item clicked");
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Sign Out")
                .setMessage("Do you really want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User clicked Yes, proceed with sign out
                    Log.d(TAG, "signOut method called");
                    FirebaseAuth.getInstance().signOut();
                    Log.d(TAG, "Firebase sign out called");

                    Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to login activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "Redirected to MainActivity and finished current activity");
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User clicked No, dismiss the dialog
                    dialog.dismiss();
                });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    private void fetchLinks() {
        fetchLocalLinks();
        // We'll fetch Firebase links after local links are loaded
    }

    private void fetchLocalLinks() {
        new Thread(() -> {
            List<Link> localLinks = linkDao.getLinksByUserId(userId);
            runOnUiThread(() -> {
                for (Link link : localLinks) {
                    addLinkIfUnique(link);
                }
                fetchFirebaseLinks(); // Fetch Firebase links after local links are loaded
            });
        }).start();
    }

    private void fetchFirebaseLinks() {
        firebaseDb.collection("users").document(userId).collection("links")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String title = document.getString("title");
                            String url = document.getString("url");
                            Link link = new Link(id, title, url, userId);
                            addLinkIfUnique(link);
                        }
                        linkAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("Firebase", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void addLinkIfUnique(Link link) {
        if (uniqueUrls.add(link.getUrl())) {
            linkList.add(link);
            originalLinkList.add(link);
        }
    }

    @Override
    public void onDelete(Link link) {
        deleteLinkLocally(link);
        deleteLinkFromFirebase(link);
    }

    private void deleteLinkLocally(Link link) {
        new Thread(() -> {
            linkDao.delete(link);
            runOnUiThread(() -> {
                linkList.remove(link);
                originalLinkList.remove(link);
                uniqueUrls.remove(link.getUrl());
                linkAdapter.notifyDataSetChanged();
                Toast.makeText(LinkListActivity.this, "Link deleted successfully!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void deleteLinkFromFirebase(Link link) {
        firebaseDb.collection("users").document(userId).collection("links")
                .document(link.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Link successfully deleted from Firestore!"))
                .addOnFailureListener(e -> Log.w("Firebase", "Error deleting link from Firestore", e));
    }

    private void filterLinks(String query) {
        linkList.clear();
        if (query.isEmpty()) {
            linkList.addAll(originalLinkList);
        } else {
            for (Link link : originalLinkList) {
                if (link.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    linkList.add(link);
                }
            }
        }
        linkAdapter.notifyDataSetChanged();
    }

    public void saveLink(String title, String url) {
        new Thread(() -> {
            Link existingLink = linkDao.getLinkByUrlAndUserId(url, userId);

            if (existingLink == null) {
                checkFirebaseForDuplicateAndInsert(title, url);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(LinkListActivity.this, "Link already exists in local storage!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void checkFirebaseForDuplicateAndInsert(String title, String url) {
        firebaseDb.collection("users").document(userId).collection("links")
                .whereEqualTo("url", url)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            insertNewLink(title, url);
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(LinkListActivity.this, "Link already exists in Firebase!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Log.w("Firebase", "Error getting documents.", task.getException());
                    }
                });
    }

    private void insertNewLink(String title, String url) {
        Map<String, Object> linkData = new HashMap<>();
        linkData.put("title", title);
        linkData.put("url", url);

        firebaseDb.collection("users").document(userId).collection("links")
                .add(linkData)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    Link newLink = new Link(id, title, url, userId);
                    insertLinkLocally(newLink);
                    runOnUiThread(() -> {
                        // Immediately show the new link in the UI after saving to Firebase
                        Toast.makeText(LinkListActivity.this, "Link saved successfully!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(LinkListActivity.this, "Failed to save link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void insertLinkLocally(Link link) {
        new Thread(() -> {
            linkDao.insert(link); // Insert into local database
            runOnUiThread(() -> {
                addLinkIfUnique(link); // Add the link to the UI list
                linkAdapter.notifyDataSetChanged(); // Notify adapter to update the UI
            });
        }).start();
    }


}