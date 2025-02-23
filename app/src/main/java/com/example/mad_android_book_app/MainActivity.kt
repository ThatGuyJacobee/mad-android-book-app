package com.example.mad_android_book_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_android_book_app.ui.theme.MADAndroidBookAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise database instance
        val db = DatabaseInstance.getDatabase(applicationContext)
        val bookDao = db.bookDao()

        enableEdgeToEdge()
        setContent {
            MADAndroidBookAppTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    BookListScreen(
                        bookDao = bookDao,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BookListScreen(
    bookDao: BookDao,
    modifier: Modifier = Modifier,
) {
    // Coroutine scope for async db operations (without blocking main thread)
    val coroutineScope = rememberCoroutineScope()

    // Books state as a list, storing all of the books (mutable)
    val books = remember { mutableStateListOf<Book>() }

    // Load all of the books from the database upon first load
    LaunchedEffect(Unit) {
        books.addAll(bookDao.getAllBooks())
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // App Heading
        Text(
            text = "Book Management System",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add New Book Button
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Add a New Book",
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List each book object as scrollable cards
        LazyColumn() {
            items(books) { book ->
                BookCard(
                    book = book
                )
            }
        }
    }
}

@Composable
fun BookCard(book: Book) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = book.title)
            Text(text = book.author)
            Text(text = book.genre)
        }
    }
}