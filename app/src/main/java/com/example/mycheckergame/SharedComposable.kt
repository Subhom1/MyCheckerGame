package com.example.mycheckergame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.min

enum class Player {
    PLAYER1,
    PLAYER2
}
@Composable
fun DraughtsCanvas(currentPlayer: MutableState<Player>,player1Point:MutableState<Int>,player2Point:MutableState<Int>,onPlayerChanged: (Player) -> Unit) {
    var selectedPiece: Pair<Int, Int>? by remember { mutableStateOf(null) }
    var player1Positions by remember {mutableStateOf(listOf(0 to 5,2 to 5,4 to 5,6 to 5,1 to 6,3 to 6,5 to 6,7 to 6,0 to 7,2 to 7,4 to 7,6 to 7))}
    var player2Positions by remember {mutableStateOf(listOf(1 to 0,3 to 0,5 to 0,7 to 0,0 to 1,2 to 1,4 to 1,6 to 1,1 to 2,3 to 2,5 to 2,7 to 2))}
    Box(
            modifier =
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val col = (offset.x / size.width * 8).toInt()
                            val row = (offset.y / size.height * 8).toInt()
                            val isBlackSquare = (col + row) % 2 != 0
                            // Checks if the tapped position is in a black square
                            if (isBlackSquare) {
                                if (selectedPiece == null) {
                                    // If no piece is selected, checks if there's a piece at the tapped location for the current player
                                    val validPlayerPositions =
                                            if (currentPlayer.value == Player.PLAYER1)
                                                    player1Positions
                                            else player2Positions
                                    if (validPlayerPositions.contains(col to row)) {
                                        selectedPiece = col to row
                                    }
                                } else {
                                    // If a piece is already selected, check if the move is valid
                                    selectedPiece?.let { selected ->
                                        val selectedRow = selected.second
                                        val selectedCol = selected.first
                                        val oneHopDiagonal = ((col - selected.first).absoluteValue == 1 && (row - selected.second).absoluteValue == 1)
                                        val twoHopDiagonal = ((col - selected.first).absoluteValue == 2 && (row - selected.second).absoluteValue == 2)
                                        val ply1MoveUpward = ((currentPlayer.value == Player.PLAYER1 && row < selected.second) && !player2Positions.contains(col to row) && !player1Positions.contains(col to row))
                                        val ply2MoveDownward = ((currentPlayer.value == Player.PLAYER2 && row > selected.second) && !(player1Positions).contains(col to row) && !player2Positions.contains(col to row))
                                        val isMovable = (ply1MoveUpward||ply2MoveDownward)
                                        var isValidMove = false
                                        if(oneHopDiagonal && isMovable){
                                            isValidMove = true
                                        }else if(twoHopDiagonal && isMovable){
                                            if(currentPlayer.value == Player.PLAYER1){
                                                if (
                                                    player2Positions.contains((selectedCol + 1) to (selectedRow - 1)) ||
                                                    player2Positions.contains((selectedCol - 1) to (selectedRow - 1))
                                                ) {
                                                    val cord = if (player2Positions.contains((selectedCol + 1) to (selectedRow - 1))) ( (selectedCol + 1) to (selectedRow - 1))else ((selectedCol - 1) to (selectedRow - 1))
                                                    isValidMove = true
                                                    player2Positions = player2Positions.toMutableList().apply {
                                                        removeIf { it == cord }
                                                    }
                                                    player1Point.value+=1
                                                }

                                            }
                                            else{
                                                if (
                                                    player1Positions.contains((selectedCol + 1) to (selectedRow + 1)) ||
                                                    player1Positions.contains((selectedCol - 1) to (selectedRow + 1))
                                                ) {
                                                    val cord = if (player1Positions.contains((selectedCol + 1) to (selectedRow + 1))) ( (selectedCol + 1) to (selectedRow + 1))else ((selectedCol - 1) to (selectedRow + 1))
                                                    isValidMove = true
                                                    player1Positions = player1Positions.toMutableList().apply {
                                                        removeIf { it == cord }
                                                    }
                                                    player2Point.value+=1
                                                }
                                            }
                                        }
                                        if (isValidMove) {
                                            // Updates the positions based on the selected piece for the current player
                                            if (currentPlayer.value == Player.PLAYER1) {
                                                player1Positions = player1Positions - selected + (col to row)
                                            } else {
                                                    player2Positions = player2Positions - selected + (col to row)
                                            }

                                            // Reset the selected piece and switch the current player
                                            selectedPiece = null
                                            onPlayerChanged(
                                                    if (currentPlayer.value == Player.PLAYER1)
                                                            Player.PLAYER2
                                                    else Player.PLAYER1
                                            )
                                        }else {
                                            // If the move is not valid, check if there's another piece for the current player at the tapped location
                                            val validPlayerPositions =
                                                if (currentPlayer.value == Player.PLAYER1) player1Positions else player2Positions
                                            if (validPlayerPositions.contains(col to row)) {
                                                // Select the new piece for the current player
                                                selectedPiece = col to row
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
    ) {
        Canvas(modifier = Modifier.aspectRatio(1f)) {
            // The checkerboard
            drawCheckerboard()
            // Drawing pieces
            drawPieces(selectedPiece, player1Positions, player2Positions)
        }
    }
}

private fun DrawScope.drawCheckerboard() {
    val squareSize = min(size.width, size.height) / 8
    for (row in 0 until 8) {
        for (col in 0 until 8) {
            drawRect(
                    color = if ((row + col) % 2 == 0) Color.LightGray else Color.DarkGray,
                    topLeft = Offset(col * squareSize, row * squareSize),
                    size = Size(squareSize, squareSize)
            )
        }
    }
}

private fun DrawScope.drawPieces(
        selectedPiece: Pair<Int, Int>?,
        player1Positions: List<Pair<Int, Int>>,
        player2Positions: List<Pair<Int, Int>>
) {
    val squareSize = size.width / 8
    val pieceRadius = squareSize / 3

    player1Positions.forEach { (col, row) ->
        drawCircle(
                color = Color.Red,
                center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
                radius = pieceRadius
        )
    }
    player2Positions.forEach { (col, row) ->
        drawCircle(
                color = Color.Green,
                center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
                radius = pieceRadius
        )
    }
    selectedPiece?.let { (col, row) ->
        drawCircle(
                color = Color.Yellow,
                center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
                radius = pieceRadius,
        )
    }
}

var currentPlayer = mutableStateOf(Player.PLAYER1)
var player1Point = mutableStateOf(0)
var player2Point = mutableStateOf(0)
@Composable
fun DraughtsGameScreen() {
    Column(
            modifier =
            Modifier
                .background(color = Color.Gray)
                .fillMaxSize()
                .padding(top = 55.dp, start = 20.dp, end = 25.dp)
    ) {
        Text(
                "Game of Draughts",
                modifier = Modifier.padding(bottom = 25.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
        )
        Row(modifier = Modifier.padding(bottom = 15.dp)) {
            Text("Current Player: ", fontWeight = FontWeight.Bold)
            Text(
                    if (currentPlayer.value == Player.PLAYER1) "Red" else "Green",
                    fontWeight = FontWeight.Bold,
                    color = if (currentPlayer.value == Player.PLAYER1) Color.Red else Color.Green
            )
        }
        DraughtsCanvas(currentPlayer = currentPlayer, player1Point= player1Point, player2Point= player2Point) { newPlayer ->
            currentPlayer.value = newPlayer
        }
Row(modifier = Modifier.padding(top=20.dp)) {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Player ")
            }
            withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                append("Red  ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${player1Point.value}")
            }
        }
    )
    Spacer(modifier = Modifier.width(20.dp))
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Player ")
            }
            withStyle(style = SpanStyle(color = Color.Green, fontWeight = FontWeight.Bold)) {
                append("Green ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(" ${player2Point.value}")
            }
        }
    )

}
        ResetButton()
    }
}

@Composable
fun ResetButton() {
    Button(modifier = Modifier.padding(top = 35.dp), onClick = {}) { Text("Reset Game") }
}
