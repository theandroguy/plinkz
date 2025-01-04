package com.tushar.plinkz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tushar.plinkz.Modal.AppDatabase;
import com.tushar.plinkz.Modal.Link;
import com.tushar.plinkz.Modal.LinkDao;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ManualSaveLinkActivity extends AppCompatActivity {

    private EditText etUrl;
    private TextInputEditText etTitle;
    private Button btnSave, btnCancel;
    private FirebaseFirestore firebaseDb;
    private FirebaseAuth mAuth;
    private AppDatabase localDb;
    private LinkDao linkDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_link_save);

        // Initialize Firebase and Room Database
        firebaseDb = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        localDb = AppDatabase.getInstance(this);
        linkDao = localDb.linkDao();

        // Bind views
        etTitle = findViewById(R.id.LinkTitle);
        etUrl = findViewById(R.id.LinkUrl);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Set up buttons
        btnSave.setOnClickListener(v -> saveLink());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveLink() {
        String title = Objects.requireNonNull(etTitle.getText()).toString().trim();
        String url = etUrl.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Both title and URL are required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to save links", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Map<String, Object> linkData = new HashMap<>();
        linkData.put("title", title);
        linkData.put("url", url);

        firebaseDb.collection("users").document(userId).collection("links")
                .add(linkData)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    saveLinkLocally(id, title, url, userId);
                    Toast.makeText(this, "Link saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving link", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveLinkLocally(String id, String title, String url, String userId) {
        new Thread(() -> {
            Link link = new Link(id, title, url, userId);
            linkDao.insert(link);
        }).start();
    }
}
