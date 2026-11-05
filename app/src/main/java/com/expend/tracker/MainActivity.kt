package com.expend.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expend.tracker.ui.MainApp
import com.expend.tracker.ui.theme.ExpendTheme
import com.expend.tracker.viewmodel.ExpendViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: ExpendViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            ExpendTheme(themeMode = themeMode) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}
