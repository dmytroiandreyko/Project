package com.example.myproject

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val MAIN = "main"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val PLACE_ID_ARG = "placeId"
    const val DETAILS = "details/{$PLACE_ID_ARG}"

    fun details(placeId: Int) = "details/$placeId"
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CATALOG(Routes.MAIN, "Каталог", Icons.Filled.Home, Icons.Outlined.Home),
    FAVORITES(Routes.FAVORITES, "Добірка", Icons.Filled.Favorite, Icons.Filled.FavoriteBorder)
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = TopLevelDestination.entries.any { currentDestination.isOn(it) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination.isOn(destination)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val bottomPadding = PaddingValues(bottom = padding.calculateBottomPadding())
        NavHost(
            navController = navController,
            startDestination = Routes.MAIN,
            modifier = Modifier
                .padding(bottomPadding)
                .consumeWindowInsets(bottomPadding)
        ) {
            composable(Routes.MAIN) {
                MainScreen(
                    onItemClick = { place -> navController.navigate(Routes.details(place.id)) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onFavoritesClick = { navController.navigateToTopLevel(TopLevelDestination.FAVORITES) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onItemClick = { place -> navController.navigate(Routes.details(place.id)) },
                    onBrowseClick = { navController.navigateToTopLevel(TopLevelDestination.CATALOG) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(
                route = Routes.DETAILS,
                arguments = listOf(navArgument(Routes.PLACE_ID_ARG) { type = NavType.IntType })
            ) {
                DetailsScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.route == destination.route } == true

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
