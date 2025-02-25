package com.example.mad_android_book_app

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define Book class, with a books table in Room
@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-increment PK
    val title: String,
    val author: String,
    val genre: String,
    val dateAdded: Long, // As a timestamp for date
    val totalPages: Int,
    var readingProgress: Int, // As a page number
)