package com.example.myproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onItemClick: (Place) -> Unit,
    onBrowseClick: () -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.refreshError) {
        val refreshError = uiState.refreshError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(refreshError.message)
        viewModel.onRefreshErrorShown()
    }

    val onFavoriteClick: (Place) -> Unit = { place ->
        viewModel.onToggleFavorite(place)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "«${place.title}» прибрано з добірки",
                actionLabel = "Повернути",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.onRestore(place)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Моя добірка") }) }
    ) { padding ->
        val error = uiState.error
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
        ) {
            when {
                uiState.favoriteIds.isEmpty() && !uiState.isLoading ->
                    FavoritesEmptyState(onBrowseClick = onBrowseClick)
                uiState.isLoading && uiState.places.isEmpty() -> LoadingState()
                error != null && uiState.places.isEmpty() ->
                    ErrorState(message = error.message, onRetry = viewModel::onRetry)
                else -> PlacesList(
                    places = uiState.places,
                    showDetails = uiState.showDetails,
                    favoriteIds = uiState.favoriteIds,
                    onItemClick = onItemClick,
                    onFavoriteClick = onFavoriteClick,
                    bottomInset = padding.calculateBottomPadding()
                )
            }
        }
    }
}

@Composable
fun FavoritesEmptyState(onBrowseClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = "У добірці поки порожньо",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Торкніться сердечка на картці в каталозі або на сторінці місця, і воно чекатиме вас тут навіть після перезапуску.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedButton(
            onClick = onBrowseClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Перейти до каталогу")
        }
    }
}
