package com.example.myproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold { padding ->
                    BriefScreen(Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun BriefScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Міста й пам'ятки", style = MaterialTheme.typography.headlineLarge)
        Text("Застосунок шукає статті про міста й пам'ятки у Вікіпедії з витягом і фото та веде власний список місць.")
        Text("Екрани", style = MaterialTheme.typography.titleMedium)
        Text("1. Пошук\n2. Картка місця\n3. Мої місця\n4. Налаштування")
        Text("Джерело даних", style = MaterialTheme.typography.titleMedium)
        Text("Wikimedia REST API (українська Вікіпедія), ключ не потрібен")
        Text("Зберігається локально", style = MaterialTheme.typography.titleMedium)
        Text("Свій список місць і мова вікі")
    }
}