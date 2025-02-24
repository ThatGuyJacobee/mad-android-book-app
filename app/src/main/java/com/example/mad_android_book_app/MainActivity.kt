package com.example.mad_android_book_app

import android.graphics.Paint.Align
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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

    // Setup states for searching and sorting
    var searchQuery by remember { mutableStateOf("") }

    val sortingOptions = mapOf(
        "title" to "Title",
        "author" to "Author",
        "genre" to "Genre",
        "dateAdded" to "Date Added",
        "totalPages" to "Total Pages",
        "readingProgress" to "Reading Progress",
    )
    var sortExpanded by remember { mutableStateOf(false) }
    var selectedSortType by remember { mutableStateOf("dateAdded") }

    // Pick direction, by default "descending"
    var isSortAscending by remember { mutableStateOf(false) }

    // Create a filtered state that is derived based on the searchQuery
    val filteredBooks by remember {
        derivedStateOf {
            // If search is empty, return the full list
            if (searchQuery.isBlank()) {
                books
            }

            // Otherwise, filter the book list based on the title
            else {
                books.filter { it.title.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    // And now setup another state which is derived from isSorted
    // and sorts the filteredBooks by the selected selectedSortType
    val sortedBooks by remember {
        derivedStateOf {
            var finalList = filteredBooks
            // Sort by the currently selected value
            if (selectedSortType == "title") {
                finalList = finalList.sortedBy { it.title }
            }

            else if (selectedSortType == "author") {
                finalList = finalList.sortedBy { it.author }
            }

            else if (selectedSortType == "genre") {
                finalList = finalList.sortedBy { it.genre }
            }

            else if (selectedSortType == "totalPages") {
                finalList = finalList.sortedBy { it.totalPages }
            }

            else if (selectedSortType == "readingProgress") {
                // Calculate Progress
                finalList = finalList.sortedBy { ((it.readingProgress / it.totalPages) * 100) }
            }

            // Otherwise, sort by the dateAdded
            else {
                finalList = finalList.sortedBy { it.dateAdded }
            }

            // Now either reverse the list (if ascending is true) or return the list as it is
            if (isSortAscending) {
                finalList.reversed()
            }

            else {
                finalList
            }
        }
    }

    // Load all of the books from the database upon first load
    LaunchedEffect(Unit) {
        books.addAll(bookDao.getAllBooks())
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Heading
        Text(
            text = "Book Management System",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )

        // Add New Book Button
        Button(
            onClick = { addBook = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF737ADA)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Add a New Book",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Add Book",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search for Books by Name...") },
            modifier = Modifier.fillMaxWidth()
        )

        // Button & Dropdown Menu for sorting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Button toggle for the sorting direction
            Button (
                onClick = { isSortAscending = !isSortAscending },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF737ADA)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSortAscending) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Filter Button",
                            modifier = Modifier.size(28.dp)
                        )
                        Text ("Sort Descending")
                    }

                    else {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Filter Button",
                            modifier = Modifier.size(28.dp)
                        )
                        Text (
                            "Sort Ascending",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Button displaying selected filter & paired with dropdown to change
            Box {
                Button (
                    onClick = { sortExpanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF737ADA)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort Button",
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = sortingOptions[selectedSortType] ?: "Date Added",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    sortingOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.value) },
                            onClick = {
                                selectedSortType = type.key
                                sortExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // List each book object as scrollable cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedBooks) { book ->
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
            Button(
                onClick = {
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
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF737ADA)
                )
            ) { Text("Add Book") }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF737ADA)
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookCard(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Main Content (right)
                Column(
                    modifier = Modifier.weight(1f) // Allow it to fill space, but don't squish img
                ) {
                    Text(text = book.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = book.author)
                }

                // Book Preview Image (top)
                Image(
                    painter = painterResource(id = R.drawable.books_image),
                    contentDescription = "Book Icon",
                    modifier = Modifier
                        .size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Flow Row for each bubble, to allow for "flex wrapping"
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                        .background(Color(0xFF55B0DD))
                        .padding(12.dp, 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tag,
                        contentDescription = "Genre Tag",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = book.genre)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                        .background(Color(0xFF55B0DD))
                        .padding(12.dp, 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoStories,
                        contentDescription = "Total Pages",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = book.totalPages.toString())
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                        .background(Color(0xFF55B0DD))
                        .padding(12.dp, 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Progress",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = (book.readingProgress / book.totalPages * 100).toString() + "%")
                }
            }
        }
    }
}