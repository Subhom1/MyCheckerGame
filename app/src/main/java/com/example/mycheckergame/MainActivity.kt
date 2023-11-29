package com.example.mycheckergame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.example.mycheckergame.ui.theme.MyCheckerGameTheme

enum class Player {
    PLAYER1,
    PLAYER2;
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCheckerGameTheme{
                DraughtsGameScreen(currentPlayer)
            }
        }
    }
    private var currentPlayer = mutableStateOf(Player.PLAYER1)
}
