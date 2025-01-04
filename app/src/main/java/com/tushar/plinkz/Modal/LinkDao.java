package com.tushar.plinkz.Modal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LinkDao {

    // Insert a link
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Link link);

    // Get all links for a specific user
    @Query("SELECT * FROM links WHERE userId = :userId")
    List<Link> getLinksByUserId(String userId);

    // Get a link by its URL for a specific user
    @Query("SELECT * FROM links WHERE url = :url AND userId = :userId LIMIT 1")
    Link getLinkByUrlAndUserId(String url, String userId);

    // Delete a specific link
    @Delete
    void delete(Link link);

    // Optionally, add a method to delete all links for a specific user (if needed)
    @Query("DELETE FROM links WHERE userId = :userId")
    void deleteAllLinksForUser(String userId);

    // Optionally, keep the method to get all links (for admin purposes or data migration)
    @Query("SELECT * FROM links")
    List<Link> getAllLinks();
}