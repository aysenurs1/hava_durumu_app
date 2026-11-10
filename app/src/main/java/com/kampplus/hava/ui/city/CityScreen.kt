package com.kampplus.hava.ui.city

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.FavoritesViewModel
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.LocationHelper
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.ShimmerList
import com.kampplus.hava.ui.component.WeatherUiTokens
import com.kampplus.hava.ui.theme.HavaTheme
import kotlinx.coroutines.launch

@Composable
fun CityRoute(
    onBack: () -> Unit,
    onCityClick: (City) -> Unit,
    favoritesViewModel: FavoritesViewModel,
    modifier: Modifier = Modifier,
    viewModel: CityViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()

    CityScreen(
        onBack = onBack,
        query = query,
        uiState = uiState,
        favorites = favorites,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onCityClick = onCityClick,
        onFavoriteClick = favoritesViewModel::toggleFavorite,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    onBack: () -> Unit = {},
    query: String,
    uiState: LoadState<List<City>>,
    favorites: List<City>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    onFavoriteClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    var isLocating by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            isLocating = true
            scope.launch {
                val coords = locationHelper.getCurrentCoordinates()
                isLocating = false
                if (coords != null) {
                    val locationCity = City(
                        id = 0,
                        name = "Mevcut Konumum",
                        latitude = coords.first,
                        longitude = coords.second,
                        countryCode = "TR",
                        country = "Türkiye",
                        admin1 = null
                    )
                    onCityClick(locationCity)
                } else {
                    Toast.makeText(context, "Konum alınamadı. GPS'in açık olduğundan emin olun.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Hava durumu için konum izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.background(Brush.verticalGradient(WeatherUiTokens.skyGradient)),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name), color = Color.White)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                query = query,
                isLocating = isLocating,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onLocationClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    LoadState.Loading -> ShimmerList()

                    is LoadState.Success -> CityList(
                        cities = uiState.data,
                        favorites = favorites,
                        onCityClick = onCityClick,
                        onFavoriteClick = onFavoriteClick
                    )

                    LoadState.Empty -> EmptyView(
                        title = "Şehir bulunamadı",
                        message = "Yazımı kontrol et ya da başka bir şehir dene."
                    )

                    is LoadState.Error -> ErrorView(
                        message = uiState.message,
                        onRetry = onSearch
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    isLocating: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.20f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.20f),
            focusedBorderColor = Color.White.copy(alpha = 0.75f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
            cursorColor = Color.White,
            focusedLeadingIconColor = Color.White,
            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.85f),
            focusedTrailingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.White.copy(alpha = 0.85f)
        ),
        label = {
            Text(stringResource(R.string.search_hint), color = Color.White.copy(alpha = 0.8f))
        },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") }
                    ) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.action_clear)
                        )
                    }
                }

                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onLocationClick) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Mevcut Konumum",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

@Composable
private fun CityList(
    cities: List<City>,
    favorites: List<City>,
    onCityClick: (City) -> Unit,
    onFavoriteClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds = favorites.map { it.id }.toSet()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = cities,
            key = { it.id }
        ) { city ->
            CityCard(
                city = city,
                onClick = { onCityClick(city) },
                isFavorite = city.id in favoriteIds,
                onFavoriteClick = {
                    onFavoriteClick(city)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenPreview() {
    val city = City(
        323786,
        "Ankara",
        39.91987,
        32.85427,
        "TR",
        "Türkiye",
        "Ankara"
    )

    HavaTheme {
        CityScreen(
            onBack = {},
            query = "Ankara",
            uiState = LoadState.Success(listOf(city)),
            favorites = emptyList(),
            onQueryChange = {},
            onSearch = {},
            onCityClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenEmptyPreview() {
    HavaTheme {
        CityScreen(
            onBack = {},
            query = "xyz",
            uiState = LoadState.Empty,
            favorites = emptyList(),
            onQueryChange = {},
            onSearch = {},
            onCityClick = {},
            onFavoriteClick = {}
        )
    }
}
