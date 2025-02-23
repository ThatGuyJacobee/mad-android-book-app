package com.example.mad_android_book_app

import androidx.room.Database
import androidx.room.RoomDatabase

// Link the entities and DAO via abstract class
@Database(entities = [Book::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao() : BookDao
}