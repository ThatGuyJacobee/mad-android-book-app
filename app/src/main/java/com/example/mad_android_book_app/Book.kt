package com.example.mad_android_book_app

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

// Define Book class, with a books table in Room
@Entity(tableName = "books")
data class Book(
    @PrimaryKey val title: String,
    val author: String,
    val genre: String,
    val dateAdded: Date,
    val readingProgress: Int,
)