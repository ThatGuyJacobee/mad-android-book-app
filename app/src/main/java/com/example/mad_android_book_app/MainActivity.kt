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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_android_book_app.ui.theme.MADAndroidBookAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

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
    // Prepare state for the add book dialog
    var addBook by remember { mutableStateOf(false) }

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
            onClick = { addBook = true },
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

    // Display the Add Book Dialog when addBook is true
    if (addBook) {
        AddBookDialog(
            onDismiss = { addBook = false },
            dbScope = coroutineScope,
            bookDao = bookDao,
            books = books
        )
    }
}

// Add Book Dialog
@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    dbScope: CoroutineScope,
    bookDao: BookDao,
    books: SnapshotStateList<Book>
) {
    // Prepare states for each of the Book class attributes
    var newTitle by remember { mutableStateOf("") }
    var newAuthor by remember { mutableStateOf("") }
    var newGenre by remember { mutableStateOf("") }
    var newTotalPages by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a New Book") },
        text = {
            Column {
                // Title input
                TextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Book Title") }
                )

                // Author input
                TextField(
                    value = newAuthor,
                    onValueChange = { newAuthor = it },
                    label = { Text("Book Author") }
                )

                // Genre input
                TextField(
                    value = newGenre,
                    onValueChange = { newGenre = it },
                    label = { Text("Book Genre") }
                )

                // Total Pages input
                TextField(
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number // Set to Number Input Keyboard
                    ),
                    value = newTotalPages,
                    onValueChange = { newValue ->
                        // Filter the input so that it only accepts integers explicitly
                        val filterIntegers = newValue.filter { it.isDigit() }
                        newTotalPages = filterIntegers
                    },
                    label = { Text("Total Pages") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Ensure that all of the fields are not empty
                if (newTitle.isNotEmpty() && newAuthor.isNotEmpty() &&
                    newGenre.isNotEmpty() && newTotalPages.isNotEmpty()) {
                    dbScope.launch {
                        // Create new book object
                        val newBook = Book(title = newTitle, author = newAuthor, genre = newGenre,
                            dateAdded = Date().time, totalPages = newTotalPages.toInt(),
                            readingProgress = 0)

                        // Run the insert function from the DAO
                        bookDao.insertBook(newBook)

                        // Refresh the current local state
                        books.clear()
                        books.addAll(bookDao.getAllBooks())

                        // And close the dialog
                        onDismiss()
                    }
                }
            }) { Text("Add Book") }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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