package com.example.myproject

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val MAIN = "main"
    const val PLACE_ID_ARG = "placeId"
    const val DETAILS = "details/{$PLACE_ID_ARG}"

    fun details(placeId: Int) = "details/$placeId"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(
                onItemClick = { place -> navController.navigate(Routes.details(place.id)) }
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument(Routes.PLACE_ID_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt(Routes.PLACE_ID_ARG) ?: -1
            DetailsScreen(
                placeId = placeId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
