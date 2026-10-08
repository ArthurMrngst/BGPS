package com.example.bgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bgps.ui.theme.BGPSTheme
import com.example.bgps.ui.transactions.TransactionListScreen
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint bắt buộc để hiltViewModel() bên trong Compose lấy được ViewModel
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BGPSTheme() {
                TransactionListScreen()
            }
        }
    }
}