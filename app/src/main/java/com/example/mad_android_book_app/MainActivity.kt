package com.example.mad_android_book_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        // Here, the book id is passed as a String, which is used in the screen function
        composable(
            "book.view/{bookId}",
            arguments = listOf(navArgument("bookId")
            { type = NavType.StringType })
        ) { navBackStackEntry ->
            // Get the passed through argument
            val bookId = navBackStackEntry.arguments?.getString("bookId")

            // If it's set, render the book view screen
            if (bookId != null) {
                BookViewScreen(navController, bookDao, bookId.toInt())
            }

            // Otherwise, render that an error has occurred
            else {
                Text("Error: Book parameter was not fulfilled with the navigation request.")
            }
        }
    }
}