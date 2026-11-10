package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kampplus.hava.data.PinnedCityRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CityRoute
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.WeatherUiTokens
import com.kampplus.hava.ui.forecast.ForecastRoute
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.map.WeatherMapRoute
import kotlinx.coroutines.launch

@Composable
fun HavaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val pinnedCityViewModel: PinnedCityViewModel = viewModel()
    val defaultCity by pinnedCityViewModel.defaultCity.collectAsStateWithLifecycle()
    val saveError by pinnedCityViewModel.saveError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(saveError) {
        saveError?.let {
            snackbarHostState.showSnackbar(it)
            pinnedCityViewModel.clearSaveError()
        }
    }

    val onCitySelected: (City) -> Unit = { city ->
        coroutineScope.launch {
            if (pinnedCityViewModel.pinCity(city)) {
                navController.navigate(forecastRoute(city)) {
                    popUpTo(FORECAST_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.background(Brush.verticalGradient(WeatherUiTokens.skyGradient)),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = START_ROUTE,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(START_ROUTE) {
                when (val state = defaultCity) {
                    LoadState.Loading, LoadState.Empty -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is LoadState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ErrorView(state.message, onRetry = pinnedCityViewModel::loadDefaultCity)
                        }
                    }
                    is LoadState.Success -> {
                        LaunchedEffect(state.data) {
                            navController.navigate(forecastRoute(state.data)) {
                                popUpTo(START_ROUTE) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            composable(CITY_ROUTE) {
                CityRoute(
                    onBack = { navController.popBackStack() },
                    onCityClick = onCitySelected,
                    favoritesViewModel = favoritesViewModel
                )
            }

            composable(FAVORITES_ROUTE) {
                FavoritesRoute(
                    onBack = { navController.popBackStack() },
                    onCityClick = onCitySelected,
                    favoritesViewModel = favoritesViewModel
                )
            }

            composable(MAP_ROUTE) {
                WeatherMapRoute(
                    defaultCity = (defaultCity as? LoadState.Success)?.data
                        ?: PinnedCityRepository.DEFAULT_CITY,
                    onBack = { navController.popBackStack() },
                    onCityClick = onCitySelected
                )
            }

            composable(
                route = FORECAST_ROUTE,
                arguments = listOf(
                    navArgument(ForecastViewModel.ARG_NAME) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(ForecastViewModel.ARG_SUBTITLE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(ForecastViewModel.ARG_LATITUDE) {
                        type = NavType.StringType
                        defaultValue = "0"
                    },
                    navArgument(ForecastViewModel.ARG_LONGITUDE) {
                        type = NavType.StringType
                        defaultValue = "0"
                    }
                )
            ) {
                ForecastRoute(
                    onSearch = {
                        navController.navigate(CITY_ROUTE) {
                            launchSingleTop = true
                        }
                    },
                    onOpenMap = {
                        navController.navigate(MAP_ROUTE) {
                            launchSingleTop = true
                        }
                    },
                    onOpenFavorites = {
                        navController.navigate(FAVORITES_ROUTE) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

private const val START_ROUTE = "startup"
private const val CITY_ROUTE = "city_search"
private const val FAVORITES_ROUTE = "favorites"
private const val MAP_ROUTE = "map"

private const val FORECAST_ROUTE =
    "forecast?name={name}&subtitle={subtitle}&latitude={latitude}&longitude={longitude}"

private fun forecastRoute(city: City): String = buildString {
    append("forecast")
    append("?name=").append(Uri.encode(city.name))
    append("&subtitle=").append(Uri.encode(city.subtitle))
    append("&latitude=").append(city.latitude)
    append("&longitude=").append(city.longitude)
}
