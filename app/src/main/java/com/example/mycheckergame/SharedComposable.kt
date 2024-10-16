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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.min

var player1Point = mutableStateOf(0)
var player2Point = mutableStateOf(0)
var player1Positions = mutableStateListOf(0 to 5,2 to 5,4 to 5,6 to 5,1 to 6,3 to 6,5 to 6,7 to 6,0 to 7,2 to 7,4 to 7,6 to 7)
var player2Positions = mutableStateListOf(1 to 0,3 to 0,5 to 0,7 to 0,0 to 1,2 to 1,4 to 1,6 to 1,1 to 2,3 to 2,5 to 2,7 to 2)
var selectedPiece = mutableStateOf<Pair<Int, Int>?>(null)
var ply1Kings = mutableStateListOf<Pair<Int, Int>>()
var ply2Kings = mutableStateListOf<Pair<Int, Int>>()
val player1KingPositions = listOf(1 to 0, 3 to 0,5 to 0, 7 to 0 )
val player2KingPositions = listOf(0 to 7,2 to 7,4 to 7,6 to 7)

var red = mutableStateOf(0)
var green =mutableStateOf(0)
var blue = mutableStateOf(0)

var redPiece = mutableStateOf(2)
var greenPiece =mutableStateOf(255)
var bluePiece = mutableStateOf(0)
fun resetPlayerPositions(currentPlayer: MutableState<Player>) {
    player1Positions.clear()
    player1Positions.addAll(listOf(0 to 5, 2 to 5, 4 to 5, 6 to 5, 1 to 6, 3 to 6, 5 to 6, 7 to 6, 0 to 7, 2 to 7, 4 to 7, 6 to 7))
    player2Positions.clear()
    player2Positions.addAll(listOf(1 to 0, 3 to 0, 5 to 0, 7 to 0, 0 to 1, 2 to 1, 4 to 1, 6 to 1, 1 to 2, 3 to 2, 5 to 2, 7 to 2))
    ply1Kings.clear()
    ply2Kings.clear()
    player1Point.value = 0
    player2Point.value = 0
    currentPlayer.value = Player.PLAYER1
    selectedPiece.value = null
    red.value=0
    green.value=0
    blue.value=0
    redPiece.value=2
    greenPiece.value=255
    bluePiece.value=0
}
fun gameLogic(col:Int, row: Int, onPlayerChanged: (Player)->Unit,currentPlayer: MutableState<Player>):Pair<Int,Int>?{
    val capturePos= mutableStateOf<Pair<Int, Int>?>(null)
    if (selectedPiece.value == null) {
        // If no piece is selected, checks if there's a piece at the tapped location for the current player
        val validPlayerPositions =
            if (currentPlayer.value == Player.PLAYER1)
                player1Positions
            else player2Positions
        if (validPlayerPositions.contains(col to row)) {
            selectedPiece.value = col to row
        }
    } else {
        // If a piece is already selected, check if the move is valid
        selectedPiece.value?.let { selected ->
            val selectedRow = selected.second
            val selectedCol = selected.first
            val oneHopDiagonal = ((col - selected.first).absoluteValue == 1 && (row - selected.second).absoluteValue == 1)
            val twoHopDiagonal = ((col - selected.first).absoluteValue == 2 && (row - selected.second).absoluteValue == 2)
            val player1Not = !player1Positions.contains(col to row)
            val player2Not = !player2Positions.contains(col to row)
            val ply1MoveUpward = (currentPlayer.value == Player.PLAYER1 &&  row < selected.second && player1Not && player2Not)
            val ply2MoveDownward = (currentPlayer.value == Player.PLAYER2 && row > selected.second && player1Not && player2Not)
            val isPiecePresentCordTwoHop = if(((col-1 to row+1)==(selectedCol+1 to selectedRow-1)) ){
                (selectedCol+1 to selectedRow-1)
            }else if ((col-1 to row-1)==(selectedCol+1 to selectedRow+1)){
                (selectedCol+1 to selectedRow+1)
            } else if((col+1 to row+1)==(selectedCol-1 to selectedRow-1)){
                (selectedCol-1 to selectedRow-1)
            } else if((col+1 to row-1)==(selectedCol-1 to selectedRow+1)){
                (selectedCol-1 to selectedRow+1)
            }else{
                null
            }
            val isKingMovable = if(currentPlayer.value == Player.PLAYER1){
                player2Positions.contains(isPiecePresentCordTwoHop)
            }else{
                player1Positions.contains(isPiecePresentCordTwoHop)
            }
            val isMovable = if((ply1Kings.contains(selectedCol to selectedRow) || ply2Kings.contains(selectedCol to selectedRow)) && (player1Not && player2Not))true else (ply1MoveUpward || ply2MoveDownward)

            var isValidMove = false
            if(oneHopDiagonal && isMovable){
                isValidMove = true
            }else if(twoHopDiagonal && isMovable){
                if(currentPlayer.value == Player.PLAYER1){
                    if (
                        (player2Positions.contains((selectedCol + 1) to (selectedRow - 1)) ||
                                player2Positions.contains((selectedCol - 1) to (selectedRow - 1))) && !ply1Kings.contains(selectedCol to selectedRow)
                    ) {
                        val cord = if (player2Positions.contains((selectedCol + 1) to (selectedRow - 1)) &&(((selectedCol + 1) to (selectedRow - 1)) == (col-1 to row+1)))
                            ((selectedCol + 1) to (selectedRow - 1))
                        else ((selectedCol - 1) to (selectedRow - 1))
                        isValidMove = true
                        player2Positions.apply {
                            removeIf { it == cord }
                        }
                        player1Point.value+=1
                    } //If the piece is king
                    else if(ply1Kings.contains(selectedCol to selectedRow)){
                        val cord = if (player2Positions.contains((selectedCol + 1) to (selectedRow - 1)) && (((selectedCol + 1) to (selectedRow - 1)) == (col-1 to row+1))) // Player2 situated top right
                            ((selectedCol + 1) to (selectedRow - 1))
                        else if (player2Positions.contains((selectedCol + 1) to (selectedRow + 1)) && (((selectedCol + 1) to (selectedRow + 1)) == (col-1 to row-1))) //Player2 situated bottom right
                            ((selectedCol + 1) to (selectedRow + 1))
                        else if (player2Positions.contains((selectedCol - 1) to (selectedRow + 1)) && (((selectedCol - 1) to (selectedRow + 1)) == (col+1 to row-1))) //Player2 situated bottom left
                            ((selectedCol - 1) to (selectedRow + 1))
                        else ((selectedCol - 1) to (selectedRow - 1))  //Player2 situated top left
                        isValidMove = true
                        player2Positions.apply {
                            removeIf { it == cord }
                        }
                        player1Point.value+=1
                    }
                }
                else{
                    if (
                        (player1Positions.contains((selectedCol + 1) to (selectedRow + 1)) ||
                                player1Positions.contains((selectedCol - 1) to (selectedRow + 1)))  && !ply2Kings.contains(selectedCol to selectedRow)
                    ) {
                        val cord = if (player1Positions.contains((selectedCol + 1) to (selectedRow + 1)) &&  (((selectedCol + 1) to (selectedRow + 1))== (col-1 to row-1)))
                            ((selectedCol + 1) to (selectedRow + 1))
                        else ((selectedCol - 1) to (selectedRow + 1))
                        isValidMove = true
                        player1Positions.apply {
                            removeIf { it == cord }
                        }
                        player2Point.value+=1
                    } //If the piece is king
                    else if(ply2Kings.contains(selectedCol to selectedRow)){
                        val cord = if (player1Positions.contains((selectedCol + 1) to (selectedRow - 1)) && (((selectedCol + 1) to (selectedRow - 1)) == (col-1 to row+1))) // Player2 situated top right
                            ((selectedCol + 1) to (selectedRow - 1))
                        else if (player1Positions.contains((selectedCol + 1) to (selectedRow + 1)) && (((selectedCol + 1) to (selectedRow + 1)) == (col-1 to row-1))) //Player2 situated bottom right
                            ((selectedCol + 1) to (selectedRow + 1))
                        else if (player1Positions.contains((selectedCol - 1) to (selectedRow + 1)) && (((selectedCol - 1) to (selectedRow + 1)) == (col+1 to row-1))) //Player2 situated bottom left
                            ((selectedCol - 1) to (selectedRow + 1))
                        else ((selectedCol - 1) to (selectedRow - 1)) //Player2 situated top left
                        isValidMove = true
                        player1Positions.apply {
                            removeIf { it == cord }
                        }
                        player2Point.value+=1
                    }
                }
            }
            if (isValidMove) {
                // Updates the positions based on the selected piece for the current player
                if (currentPlayer.value == Player.PLAYER1) {
                    player1Positions.remove(selected)
                    player1Positions.add(col to row)
                    if(player1KingPositions.contains(col to row)){
                        ply1Kings.add(col to row)
                    }
                    if (ply1Kings.contains(selectedCol to selectedRow)) {
                        ply1Kings.removeIf { it == (selectedCol to selectedRow) }
                        ply1Kings.addAll(listOf(col to row))

                    }
                    capturePos.value = checkChainCaptures((col to row), currentPlayer.value).firstOrNull()
                } else {
                    player2Positions.remove(selected)
                    player2Positions.add(col to row)
                    if(player2KingPositions.contains(col to row)){
                        ply2Kings.add(col to row)
                    }
                    if (ply2Kings.contains(selectedCol to selectedRow)) {
                        ply2Kings.removeIf { it == (selectedCol to selectedRow) }
                        ply2Kings.addAll(listOf(col to row))

                    }
                    capturePos.value = checkChainCaptures((col to row), currentPlayer.value).firstOrNull()
                }
                // Reset the selected piece and switch the current player
                selectedPiece.value = null
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
                    selectedPiece.value = col to row
                }
            }
        }
    }
    return capturePos.value
}
@Composable
fun DraughtsCanvas(currentPlayer: MutableState<Player>,
                   player1Positions:SnapshotStateList<Pair<Int, Int>> ,
                   player2Positions: SnapshotStateList<Pair<Int, Int>>,
                   ply1Kings: SnapshotStateList<Pair<Int, Int>>,
                   ply2Kings: SnapshotStateList<Pair<Int, Int>>,
                   selectedPiece: MutableState<Pair<Int, Int>?>,
                   onPlayerChanged: (Player) -> Unit) {
    Box(
            modifier =
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val col = (offset.x / size.width * 8).toInt()
                            val row = (offset.y / size.height * 8).toInt()
                            val isBlackSquare = (col + row) % 2 != 0
                            // Checks if the tapped position is in a black square
                            if (isBlackSquare) {
                                val capturePos = gameLogic(col,row,onPlayerChanged,currentPlayer)
                                if (capturePos != null) {
                                    gameLogic(capturePos.first, capturePos.second, onPlayerChanged,currentPlayer)
                                }
                            }
                        }
                    }
    ) {
        Canvas(modifier = Modifier.aspectRatio(1f)) {
            // The checkerboard
            drawCheckerboard()
            // Drawing pieces
            drawPieces(selectedPiece.value, player1Positions, player2Positions, ply1Kings, ply2Kings)
        }
    }
}

private fun DrawScope.drawCheckerboard() {
    val squareSize = min(size.width, size.height) / 8
    for (row in 0 until 8) {
        for (col in 0 until 8) {
            drawRect(
                    color = if ((row + col) % 2 == 0) Color(red.value-1, green.value-1, blue.value-1) else Color(red.value, green.value, blue.value),
                    topLeft = Offset(col * squareSize, row * squareSize),
                    size = Size(squareSize, squareSize)
            )
        }
    }
}

private fun DrawScope.drawPieces(
    selectedPiece: Pair<Int, Int>?,
    player1Positions: SnapshotStateList<Pair<Int, Int>>,
    player2Positions: SnapshotStateList<Pair<Int, Int>>,
    ply1Kings: SnapshotStateList<Pair<Int, Int>>,
    ply2Kings: SnapshotStateList<Pair<Int, Int>>,

) {
    val squareSize = size.width / 8
    val pieceRadius = squareSize / 3
    val kingSymbolRadius = pieceRadius / 2
    player1Positions.forEach { (col, row) ->
        drawCircle(
            color = Color(1 - redPiece.value, 1 - greenPiece.value, 1 - bluePiece.value),
            center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
            radius = pieceRadius
        )
        // Draw the king symbol if applicable
        if (ply1Kings.contains(col to row)) {
            drawKingSymbol((col + 0.5f) * squareSize, (row + 0.5f) * squareSize, kingSymbolRadius)
        }
    }


    player2Positions.forEach { (col, row) ->
        drawCircle(
            color = Color(redPiece.value, greenPiece.value, bluePiece.value),
            center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
            radius = pieceRadius
        )
        if(ply2Kings.contains(col to row)) drawKingSymbol((col + 0.5f) * squareSize, (row + 0.5f) * squareSize, kingSymbolRadius)

    }
    selectedPiece?.let { (col, row) ->
        drawCircle(
            color = Color.Yellow,
            center = Offset((col + 0.5f) * squareSize, (row + 0.5f) * squareSize),
            radius = pieceRadius,
        )
    }
}

private fun DrawScope.drawKingSymbol(x: Float, y: Float, radius: Float) {
    drawCircle(
        color = Color.White,
        center = Offset(x, y),
        radius = radius
    )
    drawLine(
        color = Color.Black,
        start = Offset(x - radius / 2, y - radius / 2),
        end = Offset(x + radius / 2, y + radius / 2),
        strokeWidth = radius / 10
    )
    drawLine(
        color = Color.Black,
        start = Offset(x - radius / 2, y + radius / 2),
        end = Offset(x + radius / 2, y - radius / 2),
        strokeWidth = radius / 10
    )
}

@Composable
fun DraughtsGameScreen(currentPlayer: MutableState<Player>) {
    Column(
            modifier =
            Modifier
                .background(color = Color.Gray)
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 20.dp)
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
                    if (currentPlayer.value == Player.PLAYER1) "One" else "Two",
                    fontWeight = FontWeight.Bold,
                    color = if (currentPlayer.value == Player.PLAYER1) Color(1 - redPiece.value, 1 - greenPiece.value, 1 - bluePiece.value) else Color(redPiece.value, greenPiece.value, bluePiece.value)
            )
        }
        DraughtsCanvas(currentPlayer,
            player1Positions,
            player2Positions,
            ply1Kings,
            ply2Kings,
            selectedPiece) { newPlayer ->
            currentPlayer.value = newPlayer
        }
Row(modifier = Modifier.padding(top=20.dp)) {
    Text("Player One ",fontWeight = FontWeight.Bold)
    Box(
        modifier = Modifier
            .width(17.dp)
            .height(17.dp)
            .background(
                Color(1 - redPiece.value, 1 - greenPiece.value, 1 - bluePiece.value),
                CircleShape
            )
    )
    Text(" :  ${player1Point.value}",fontWeight = FontWeight.Bold)

    Spacer(modifier = Modifier.width(20.dp))
    Text("Player Two ",fontWeight = FontWeight.Bold)
    Box(
        modifier = Modifier
            .width(17.dp)
            .height(17.dp)
            .background(Color(redPiece.value, greenPiece.value, bluePiece.value), CircleShape)
    )
    Text(" :  ${player2Point.value}",fontWeight = FontWeight.Bold)

}
        ResetButton( onReset = { resetPlayerPositions(currentPlayer) })

        Row{
            ColorPicker("Board")
            Spacer(modifier = Modifier.width(50.dp))
            ColorPickerPiece("Pieces")
        }

    }
}

@Composable
fun ResetButton(onReset: () ->Unit) {
    Button(modifier = Modifier.padding(top = 35.dp), onClick = {
        onReset()
    }) { Text("Reset Game") }
}

@Composable
fun ColorPicker(header:String) {
    Column{
        Text(header, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        // Red Slider
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Red", modifier = Modifier.width(50.dp))
            Slider(
                value = red.value.toFloat(),
                onValueChange = { red.value = it.toInt() },
                valueRange = 0f..255f,
                modifier = Modifier.width(100.dp)
            )
        }
        // Green Slider
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Green",modifier = Modifier.width(50.dp))
            Slider(
                value = green.value.toFloat(),
                onValueChange = { green.value = it.toInt() },
                valueRange = 0f..255f,
                modifier = Modifier.width(100.dp)
            )
        }
        // Blue Slider
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Blue",modifier = Modifier.width(50.dp))
            Slider(
                value = blue.value.toFloat(),
                onValueChange = { blue.value = it.toInt() },
                valueRange = 0f..255f,
                modifier = Modifier.width(100.dp)
            )
        }
    }
}

@Composable
fun ColorPickerPiece(header:String) {
    Column{
        Text(header, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        // Red Slider
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Red", modifier = Modifier.width(50.dp))
            Slider(
                value = redPiece.value.toFloat(),
                onValueChange = { redPiece.value = it.toInt() },
                valueRange = 0f..255f,
                modifier = Modifier.width(100.dp)
            )
        }
        // Green Slider
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Green",modifier = Modifier.width(50.dp))
            Slider(
                value = greenPiece.value.toFloat(),
                onValueChange = { greenPiece.value = it.toInt() },
                valueRange = 0f..255f,
                modifier = Modifier.width(100.dp)
            )
        }
        // Blue Slider
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Blue",modifier = Modifier.width(50.dp))
            Slider(
                value = bluePiece.value.toFloat(),
                onValueChange = { bluePiece.value = it.toInt() },
                valueRange = 0f..255f,
                modifier = Modifier.width(100.dp)
            )
        }
    }
}

private fun checkChainCaptures(position: Pair<Int, Int>, currentPlayer: Player): List<Pair<Int, Int>> {
    val captures = mutableListOf<Pair<Int, Int>>()
    // Check for chain captures in all possible directions
    captures.addAll(checkChainCaptureInDirection(position, currentPlayer, 1, 1)) // Top right
    captures.addAll(checkChainCaptureInDirection(position, currentPlayer, 1, -1)) // Bottom right
    captures.addAll(checkChainCaptureInDirection(position, currentPlayer, -1, 1)) // Top left
    captures.addAll(checkChainCaptureInDirection(position, currentPlayer, -1, -1)) // Bottom left

    return captures
}

private fun checkChainCaptureInDirection(
    position: Pair<Int, Int>,
    currentPlayer: Player,
    rowDirection: Int,
    colDirection: Int
): List<Pair<Int, Int>> {
    val captures = mutableListOf<Pair<Int, Int>>()

    val (startCol, startRow) = position
    var currentCol = startCol + colDirection
    var currentRow = startRow + rowDirection

    while (isValidPosition(currentCol, currentRow)) {
        val currentPosition = currentCol to currentRow

        if (isOpponentPiece(currentPosition, currentPlayer)) {
            // Check if there is an opponent piece in the current position
            val nextCol = currentCol + colDirection
            val nextRow = currentRow + rowDirection

            if (isValidPosition(nextCol, nextRow) && isEmptyPosition(nextCol, nextRow)) {
                // Check if the next position is empty for a potential capture
                captures.add(nextCol to nextRow)
                currentCol = nextCol
                currentRow = nextRow
            } else {
                // No more captures in this direction
                break
            }
        } else {
            // No opponent piece in the current position
            break
        }
    }

    return captures
}
private fun isValidPosition(col: Int, row: Int): Boolean {
    return col in 0 until 8 && row in 0 until 8
}
private fun isOpponentPiece(position: Pair<Int, Int>, currentPlayer: Player): Boolean {
    return if (currentPlayer == Player.PLAYER1) {
        player2Positions.contains(position)
    } else {
        player1Positions.contains(position)
    }
}
private fun isEmptyPosition(col: Int, row: Int): Boolean {
    return !player1Positions.contains(col to row) && !player2Positions.contains(col to row)
}

