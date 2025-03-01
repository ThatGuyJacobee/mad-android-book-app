package com.example.mad_android_book_app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookViewScreen(
    navController: NavHostController,
    bookDao: BookDao,
    bookTitle: String
) {
    // Create a Snack barHostState state for displaying messages
    val snackBarHostState = remember { SnackbarHostState() }

    // States for the edit and delete buttons/dialogs
    var editBook by remember { mutableStateOf(false) }
    var deleteBook by remember { mutableStateOf(false) }

    // Coroutine scope for async db operations (without blocking main thread)
    val coroutineScope = rememberCoroutineScope()

    // Books state as a list, storing the book (mutable)
    val books = remember { mutableStateListOf<Book>() }

    // Prepare a date formatter for the creation date
    val dateFormatter = SimpleDateFormat("HH:mm / dd-MM-yyyy", Locale.getDefault())

    // Fetch the Book record by the name and add it to the state
    // Upon screen rendering
    LaunchedEffect(Unit) {
        books.add(bookDao.getBook(bookTitle))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "View & Manage Book",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                tint = Color(0xFF55B0DD),
                                contentDescription = "Heading Book",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        content = { innerPadding ->
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp, 0.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (books.size > 0) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = books[0].title, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Author: ${books[0].author}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        }

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
                                Text(text = books[0].genre)
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
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = "Date Tag",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = dateFormatter.format(Date(books[0].dateAdded)))
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
                                    contentDescription = "Pages Tag",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Read: ${books[0].readingProgress} / Total: ${books[0].totalPages}")
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Sub-Title for the Reading Progress items
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Reading Progress", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    tint = Color(0xFF228B22),
                                    contentDescription = "Progress",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Main pie chart and counter
                            Row(
                                modifier = Modifier.padding(8.dp, 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Create a pie chart with the percent read
                                Column() {
                                    PieChart(
                                        percentage = (books[0].readingProgress.toDouble() / books[0].totalPages.toDouble()).toFloat(),
                                        innerText = "${floor((books[0].readingProgress.toDouble() / books[0].totalPages.toDouble()) * 100).toInt()}%",
                                        radius = 40.dp
                                    )
                                }

                                // And besides create a counter to allow for easy incrementing/decrementing
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        onClick = {
                                            // Check to ensure that the value doesn't go above book pages
                                            if (books[0].readingProgress < books[0].totalPages) {
                                                // Make a temporary copy and adjust the value
                                                val updateBook = books[0].copy()
                                                updateBook.readingProgress += 1

                                                // Update it in the database
                                                coroutineScope.launch {
                                                    bookDao.updateBook(updateBook)
                                                }

                                                // And update it in the local state
                                                books[0] = updateBook
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropUp,
                                            contentDescription = "Increment Page Progress",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Text(books[0].readingProgress.toString(), fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold)

                                    IconButton(
                                        onClick = {
                                            // Check to ensure that the value doesn't go below 0
                                            if (books[0].readingProgress > 0) {
                                                // Make a temporary copy and adjust the value
                                                val updateBook = books[0].copy()
                                                updateBook.readingProgress -= 1

                                                // Update it in the database
                                                coroutineScope.launch {
                                                    bookDao.updateBook(updateBook)
                                                }

                                                // And update it in the local state
                                                books[0] = updateBook
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Decrement Page Progress",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Edit Book Button
                            Button(
                                onClick = { editBook = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF737ADA)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Edit Book",
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit Book",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Delete Book Button
                            Button(
                                onClick = { deleteBook = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF737ADA)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Delete Book",
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Icon(
                                        imageVector = Icons.Filled.DeleteForever,
                                        contentDescription = "Delete Book",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                    }

                    else {
                        Text("Fetching Book data...")
                    }
                }

                // Display the Edit Book Dialog when editBook is true
                if (editBook) {
                    EditBookDialog(
                        onDismiss = { editBook = false },
                        dbScope = coroutineScope,
                        bookDao = bookDao,
                        books = books,
                        snackBarHostState = snackBarHostState,
                    )
                }

                // Display the Delete Book Dialog when deleteBook is true
                if (deleteBook) {
                    DeleteBookDialog(
                        onDismiss = { deleteBook = false },
                        dbScope = coroutineScope,
                        bookDao = bookDao,
                        book = books[0],
                        navController = navController
                    )
                }
            }
        }
    )
}

// Edit Book Dialog
@Composable
fun EditBookDialog(
    onDismiss: () -> Unit,
    dbScope: CoroutineScope,
    bookDao: BookDao,
    books: SnapshotStateList<Book>,
    snackBarHostState: SnackbarHostState
) {
    // Prepare states for each of the Book class attributes
    var newTitle by remember { mutableStateOf(books[0].title) }
    var newAuthor by remember { mutableStateOf(books[0].author) }
    var newGenre by remember { mutableStateOf(books[0].genre) }
    var newTotalPages by remember { mutableStateOf(books[0].totalPages.toString()) }
    var newReadingProgress by remember { mutableStateOf(books[0].readingProgress.toString()) }

    // Prepare an error message state which is empty by default
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit a Book") },
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

                // Pages Read input
                TextField(
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number // Set to Number Input Keyboard
                    ),
                    value = newReadingProgress,
                    onValueChange = { newValue ->
                        // Filter the input so that it only accepts integers explicitly
                        val filterIntegers = newValue.filter { it.isDigit() }
                        newReadingProgress = filterIntegers
                    },
                    label = { Text("Pages Read") }
                )

                // Display an error message only if it is set
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color(0xFFBE292F),
                        modifier = Modifier.padding(0.dp, 6.dp, 0.dp, 0.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    dbScope.launch {
                        // Ensure that all of the fields are not empty
                        if (newTitle.isEmpty() || newAuthor.isEmpty() || newGenre.isEmpty()
                            || newTotalPages.isEmpty() || newReadingProgress.isEmpty()) {
                            errorMessage = "Error: All fields must be populated."
                        }

                        // Ensure that the page progress is not greater than total pages
                        else if (newReadingProgress.toInt() > newTotalPages.toInt()) {
                            errorMessage = "Error: Page progress cannot be greater than the total pages."
                        }

                        else {
                            // Create new book object
                            val newBook = Book(id = books[0].id, title = newTitle, author = newAuthor,
                                genre = newGenre, dateAdded = books[0].dateAdded, totalPages = newTotalPages.toInt(),
                                readingProgress = newReadingProgress.toInt())

                            // Run the update function from the DAO
                            bookDao.updateBook(newBook)

                            // Refresh the current local state and add the updated book details
                            books.clear()
                            books.add(newBook)

                            // Lastly close the dialog & display a snack bar message
                            onDismiss()
                            snackBarHostState.showSnackbar("Success: The Book has been edited.")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF228B22)
                )
            ) { Text("Edit Book") }
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

// Delete Book Dialog
@Composable
fun DeleteBookDialog(
    onDismiss: () -> Unit,
    dbScope: CoroutineScope,
    bookDao: BookDao,
    book: Book,
    navController: NavHostController
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete a Book") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Confirm that you wish to delete the book from your library?")
                Row() {
                    Text("Selected Book: ", fontWeight = FontWeight.SemiBold)
                    Text(book.title)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    dbScope.launch {
                        // Delete the book through the coroutine scope
                        bookDao.deleteBook(book)

                        // And return back to the main list screen (list will be auto-refreshed)
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBE292F)
                )
            ) { Text("Delete Book") }
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