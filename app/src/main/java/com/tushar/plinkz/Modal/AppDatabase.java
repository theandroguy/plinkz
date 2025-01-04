package com.tushar.plinkz.Modal;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

// Annotate the class with @Database and provide the entities and version
@Database(entities = {Link.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Define the DAO interface
    public abstract LinkDao linkDao();

    private static AppDatabase INSTANCE;

    // Singleton pattern to get the database instance
    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            // Create the database instance
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "link_database") // Database name
                    .fallbackToDestructiveMigration() // Handle migrations
                    .build();
        }
        return INSTANCE;
    }
}
