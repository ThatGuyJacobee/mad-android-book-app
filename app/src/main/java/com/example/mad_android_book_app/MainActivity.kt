package com.example.mad_android_book_app

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mad_android_book_app.ui.theme.MADAndroidBookAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise database instance
        val db = DatabaseInstance.getDatabase(applicationContext)
        val bookDao = db.bookDao()

        enableEdgeToEdge()
        setContent {
            MADAndroidBookAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(bookDao)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(bookDao: BookDao) {
    // Create a new NavController
    val navController = rememberNavController()

    // Setup a NavHost, with all of the available screens
    NavHost(
        navController = navController,
        startDestination = "book.list" // Set the default to the list screen
    ) {
        composable("book.list") { BookListScreen(navController, bookDao) }

        // Setup a route, that includes an argument/prop that can be passed through
        // Here, the book title is passed as a String, which is used in the screen function
        composable(
            "book.view/{bookTitle}",
            arguments = listOf(navArgument("bookTitle")
            { type = NavType.StringType })
        ) { navBackStackEntry ->
            // Get the passed through argument
            val bookTitle = navBackStackEntry.arguments?.getString("bookTitle")

            // If it's set, render the book view screen
            if (bookTitle != null) {
                BookViewScreen(navController, bookDao, bookTitle)
            }

            // Otherwise, render that an error has occurred
            else {
                Text("Error: Book parameter was not fulfilled with the navigation request.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    navController: NavHostController,
    bookDao: BookDao,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Book Management App",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            tint = Color(0xFF55B0DD),
                            contentDescription = "Heading Book",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            )
        },
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

                    // Search field for searching by book name
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search for Books by Name...") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                // Setup an icon button and set field to empty on click
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear Field",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .border(3.dp, Color(0xFF737ADA), RoundedCornerShape(32.dp))
                    )

                    // Button & Dropdown Menu for sorting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                        fontWeight = FontWeight.SemiBold,
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

                    // If at least one book is present, return the cards
                    if (sortedBooks.isNotEmpty()) {
                        // List each book object as scrollable cards
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sortedBooks) { book ->
                                BookCard(
                                    navController = navController,
                                    book = book
                                )
                            }
                        }
                    }

                    // Otherwise return a message for no books found
                    else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp, 0.dp, 0.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("No Books found...", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
        }
    )
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
                    containerColor = Color(0xFF228B22)
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
fun BookCard(navController: NavHostController, book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            // On card click, navigate to the view screen and pass the target book as attribute
            navController.navigate("book.view/${book.title}")
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookViewScreen(
    navController: NavHostController,
    bookDao: BookDao,
    bookTitle: String
) {
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
                        Text(text = books[0].title, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Author: ${books[0].author}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Genre: ${books[0].genre}")
                        Text(text = "Date Added: ${dateFormatter.format(Date(books[0].dateAdded))}")
                        Text(text = "Pages: ${books[0].totalPages} / Read: ${books[0].readingProgress}")

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
                        books = books
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
    books: SnapshotStateList<Book>
) {
    // Prepare states for each of the Book class attributes
    var newTitle by remember { mutableStateOf(books[0].title) }
    var newAuthor by remember { mutableStateOf(books[0].author) }
    var newGenre by remember { mutableStateOf(books[0].genre) }
    var newTotalPages by remember { mutableStateOf(books[0].totalPages.toString()) }

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
                            val newBook = Book(id = books[0].id, title = newTitle, author = newAuthor,
                                genre = newGenre, dateAdded = books[0].dateAdded, totalPages = newTotalPages.toInt(),
                                readingProgress = books[0].readingProgress)

                            // Run the update function from the DAO
                            bookDao.updateBook(newBook)

                            // Refresh the current local state and add the updated book details
                            books.clear()
                            books.add(newBook)

                            // And close the dialog
                            onDismiss()
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