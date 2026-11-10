package com.kampplus.hava.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CityCard
import com.kampplus.hava.ui.component.WeatherUiTokens

@Composable
fun FavoritesRoute(
    onBack: () -> Unit,
    onCityClick: (City) -> Unit,
    favoritesViewModel: FavoritesViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()

    FavoritesScreen(
        onBack = onBack,
        favorites = favorites,
        onCityClick = onCityClick,
        onFavoriteClick = favoritesViewModel::toggleFavorite,
        modifier = modifier
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit = {},
    favorites: List<City>,
    onCityClick: (City) -> Unit,
    onFavoriteClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.background(Brush.verticalGradient(WeatherUiTokens.skyGradient)),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("Favori Şehirler", color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Hava durumu ekranına dön",
                            tint = Color.White
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz favori şehrin yok.\nŞehir arama ekranından kalbe dokunarak şehir ekleyebilirsin.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = favorites,
                    key = { it.id }
                ) { city ->
                    CityCard(
                        city = city,
                        onClick = { onCityClick(city) },
                        isFavorite = true,
                        onFavoriteClick = {
                            onFavoriteClick(city)
                        }
                    )
                }
            }
        }
    }
}
