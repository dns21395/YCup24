package com.example.ycup24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ycup24.core.ui.theme.YCup24Theme
import com.example.ycup24.di.DaggerAppComponent
import com.example.ycup24.di.daggerViewModel
import com.example.ycup24.ui.Screen
import com.example.ycup24.ui.frames.FramesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YCup24Theme {
                val viewModel = daggerViewModel(key = "viewmodel") {
                    DaggerAppComponent.create().viewModel()
                }
                val state by viewModel.state.collectAsState()
                val colorScheme = MaterialTheme.colorScheme
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.background)
                ) { innerPadding ->
                    if (state.isShowFramesListScreen) {
                        FramesScreen(
                            state = state,
                            onAction = viewModel::onAction,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        Screen(
                            state = state,
                            onAction = viewModel::onAction,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YCup24Theme {
        Greeting("Android")
    }
}