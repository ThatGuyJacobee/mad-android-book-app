package com.example.mad_android_book_app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.floor

// Reusable component for creating a pie chart
@Composable
fun PieChart(
    percentage: Float, // Between 0.0 - 1.0
    innerText: String = "", // Optional
    radius: Dp = 30.dp,
    strokeWidth: Dp = 8.dp
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(radius * 2)) {
            // Prepare values for the arcs
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val center = Offset(size.width / 2, size.height / 2)
            val diameter = size.minDimension

            // Draw background arc/circle
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - diameter / 2, center.y - diameter / 2),
                size = Size(diameter, diameter),
                style = stroke
            )

            // Draw the active arc/circle
            val sweepAngle = 360 * percentage // Calculate the arc angle
            drawArc(
                color = Color(0xFF737ADA),
                startAngle = -90f, // Start from the top
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - diameter / 2, center.y - diameter / 2),
                size = Size(diameter, diameter),
                style = stroke
            )
        }

        // Add text to the middle of the pie chart if it is set
        if (innerText.isNotEmpty()) {
            Text(
                text = innerText,
                fontSize = (radius.value / 2).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Function for generating a book summary
fun generateBookSummary(book: Book): String {
    var fullString = "----- Book Summary -----\n"

    // Prepare a date formatter for the creation date
    val dateFormatter = SimpleDateFormat("HH:mm / dd-MM-yyyy", Locale.getDefault())

    // Add new lines which cover all of the book's data
    fullString += "Title: ${book.title}\n"
    fullString += "Author: ${book.author}\n"
    fullString += "Genre: ${book.genre}\n"
    fullString += "Date Added: ${dateFormatter.format(book.dateAdded)}\n"
    fullString += "Total Pages: ${book.totalPages}\n"
    fullString += "Progress: ${book.readingProgress} / ${book.totalPages} (${floor((book.readingProgress.toDouble() / book.totalPages.toDouble()) * 100).toInt()}%)"
    return fullString
}