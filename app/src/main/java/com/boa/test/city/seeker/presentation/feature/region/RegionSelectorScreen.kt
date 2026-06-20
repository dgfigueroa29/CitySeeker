package com.boa.test.city.seeker.presentation.feature.region

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.component.ErrorState
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.feature.city.CityItem
import com.boa.test.city.seeker.presentation.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectorScreen(
    onCityClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: RegionSelectorViewModel = hiltViewModel(),
) {
    val state = viewModel.regionState
    val countries by state.countries.collectAsState()
    val selectedCountry by state.selectedCountry.collectAsState()
    val cities by state.cities.collectAsState()
    val isLoading by state.loadingState.collectAsState()
    val error by state.errorState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = if (selectedCountry != null) selectedCountry!! else stringResource(R.string.region_selector_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCountry != null) {
                            viewModel.selectCountry(null)
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (isLoading) {
                LoadingIndicator(isLoading = true)
            } else if (error.isNotBlank()) {
                ErrorState(
                    message = error,
                    onRetry = { if (selectedCountry != null) viewModel.selectCountry(selectedCountry!!) },
                )
            } else if (selectedCountry == null) {
                CountryList(
                    countries = countries,
                    onCountryClick = { viewModel.selectCountry(it) },
                )
            } else {
                CityList(
                    cities = cities,
                    onCityClick = onCityClick,
                )
            }
        }
    }
}

@Composable
private fun CountryList(
    countries: List<String>,
    onCountryClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(countries, key = { it }) { country ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS)
                    .clickable { onCountryClick(country) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceM))
                    Text(
                        text = country,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun CityList(
    cities: List<CityModel>,
    onCityClick: (String) -> Unit,
) {
    if (cities.isEmpty()) {
        Text(
            text = stringResource(R.string.no_results),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceXXXL),
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(cities, key = { it.id }) { city ->
                CityItem(
                    city = city,
                    onCityClick = { onCityClick(city.id.toString()) },
                    onFavoriteClick = {},
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
                )
            }
        }
    }
}
