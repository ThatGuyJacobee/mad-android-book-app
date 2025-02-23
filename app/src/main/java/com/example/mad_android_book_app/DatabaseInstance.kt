package com.example.mad_android_book_app

import android.content.Context
import androidx.room.Room

// Prepare a singleton for database access
object DatabaseInstance {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        // If it's not set, build an instance
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "book-database"
            ).build()
        }
        return INSTANCE!!
    }
}