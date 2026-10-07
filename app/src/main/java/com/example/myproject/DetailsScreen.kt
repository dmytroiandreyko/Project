package com.example.myproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    placeId: Int,
    onBackClick: () -> Unit
) {
    val place = remember(placeId) { findPlaceById(placeId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(place?.title ?: "Не знайдено") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (place == null) {
            PlaceNotFound(modifier = Modifier.padding(padding))
        } else {
            PlaceDetails(place = place, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
fun PlaceDetails(place: Place, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = place.imageRes),
            contentDescription = place.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(
                onClick = {},
                label = { Text(place.category.label) }
            )
            Text(
                text = place.description,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = place.excerpt,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = place.details,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ключ статті: ${place.key}",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "ID: ${place.id}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun PlaceNotFound(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Місце не знайдено",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Можливо, його було видалено або посилання хибне",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
