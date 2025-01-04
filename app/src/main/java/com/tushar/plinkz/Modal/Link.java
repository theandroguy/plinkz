package com.tushar.plinkz.Modal;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "links")
public class Link {
    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String title;

    @NonNull
    private String url;

    @NonNull
    private String userId;

    public Link(@NonNull String id, @NonNull String title, @NonNull String url, @NonNull String userId) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.userId = userId;
    }

    // Getters and setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    @NonNull
    public String getUrl() { return url; }
    public void setUrl(@NonNull String url) { this.url = url; }

    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }
}