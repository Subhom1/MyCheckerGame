package com.example.mycheckergame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun DraughtsCanvas(
    drawContent: DrawScope.() -> Unit
) {
    Box{
        Canvas(
            modifier = Modifier
                .aspectRatio(1f)
        ) {
            // The checkerboard
            drawCheckerboard()
            // Additional content using the provided lambda
            drawContent()
        }
    }
}

private fun DrawScope.drawCheckerboard() {
    val squareSize = min(size.width, size.height) / 8
    for (row in 0 until 8) {
        for (col in 0 until 8) {
            drawRect(
                color = if ((row + col) % 2 == 0) Color.White else Color.Black,
                topLeft = Offset(col * squareSize, row * squareSize),
                size = Size(squareSize, squareSize)
            )
        }
    }
}
private fun DrawScope.drawPieces() {
    val squareSize = size.width / 8
    val pieceRadius = squareSize / 3
    val player1Positions = listOf(1 to 0, 3 to 0, 5 to 0, 7 to 0, 0 to 1, 2 to 1, 4 to 1, 6 to 1, 1 to 2, 3 to 2, 5 to 2, 7 to 2)
    val player2Positions = listOf(0 to 5, 2 to 5, 4 to 5, 6 to 5, 1 to 6, 3 to 6, 5 to 6, 7 to 6, 0 to 7, 2 to 7, 4 to 7, 6 to 7)
    //Player 1 pieces in red
    player1Positions.forEach { (col, row) ->
        drawCircle(
            color = Color.Red,
            center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
            radius = pieceRadius
        )
    }
    //Player 2 pieces in blue
    player2Positions.forEach { (col, row) ->
        drawCircle(
            color = Color.Blue,
            center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
            radius = pieceRadius
        )
    }


}


@Composable
fun DraughtsGameScreen() {
    Column(
        modifier = Modifier
            .background(color = Color.Gray)
            .fillMaxSize()
            .padding(top = 55.dp, start = 20.dp, end = 25.dp)
    ) {
        Text("Game of Draughts", modifier = Modifier.padding(bottom = 25.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        DraughtsCanvas {
            drawPieces()
        }
        ResetButton()
    }
}
@Composable
fun ResetButton(){
    Button(modifier = Modifier.padding(top=35.dp), onClick = {}) {
        Text( "Reset Game")
    }
}


