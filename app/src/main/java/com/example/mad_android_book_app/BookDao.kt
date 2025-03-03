package com.example.mad_android_book_app

import androidx.room.*

// Create DAO (Data Access Object) with methods
@Dao
interface BookDao {
    // Return all books as a List of Book objects
    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    // Insert a new Book record
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBook(book: Book)

    // Return a specific Book object by PK (id)
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBook(bookId: Int): Book

    // Delete a specific Book object
    @Delete()
    suspend fun deleteBook(book: Book)

    // Update a specific Book object
    @Update()
    suspend fun updateBook(book: Book)
}