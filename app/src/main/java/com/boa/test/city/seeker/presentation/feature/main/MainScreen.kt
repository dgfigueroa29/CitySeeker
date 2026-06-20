package com.boa.test.city.seeker.presentation.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Route
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.presentation.component.ConsentDialog
import com.boa.test.city.seeker.presentation.component.rememberOnlineState
import com.boa.test.city.seeker.presentation.feature.city.detail.DetailScreen
import com.boa.test.city.seeker.presentation.feature.city.list.ListScreen
import com.boa.test.city.seeker.presentation.navigation.Screen
import com.boa.test.city.seeker.presentation.ui.theme.Dimens

/**
 * Composable function that displays the main screen of the application.
 *
 * It checks the device orientation and displays the appropriate layout (Landscape or Portrait).
 *
 * @param navController The NavHostController used for navigation.
 */
@Composable
fun MainScreen(
    navController: NavHostController? = null,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val showConsent by viewModel.showConsent.collectAsState()
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    if (showConsent) {
        ConsentDialog(
            onAccept = { viewModel.acceptConsent() },
            onDecline = { viewModel.declineConsent() },
        )
    }

    if (isExpanded) {
        LandscapeLayout(navController)
    } else {
        PortraitLayout(navController)
    }
}

/**
 * Composable function for the portrait layout of the main screen.
 * It displays a list of cities and navigates to the map screen when a city is clicked.
 *
 * @param navController The NavHostController for navigation.
 */
@Composable
fun PortraitLayout(navController: NavHostController? = null) {
    val isOnline by rememberOnlineState()

    Column(modifier = Modifier.fillMaxSize()) {
        ListScreen(
            onCityClick = {
                navController?.navigate("${Screen.MAP.endpoint}/$it")
            },
        )
        Card(
            onClick = { navController?.navigate(Screen.REGIONS.endpoint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceM),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = stringResource(R.string.region_selector_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(start = Dimens.SpaceXL),
                )
            }
        }
        Card(
            onClick = { navController?.navigate(Screen.JOURNALS.endpoint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceM),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = stringResource(R.string.journal_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(start = Dimens.SpaceXL),
                )
            }
        }
        if (isOnline) {
            Card(
                onClick = { navController?.navigate(Screen.ROUTE.endpoint) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceM),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.route_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(start = Dimens.SpaceXL),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.SpaceM))
    }
}

/**
 * Composable function for displaying the main screen in landscape orientation.
 * It shows a list of cities on the left and the details of the selected city on the right.
 *
 * @param navController The navigation controller for handling navigation events.
 */
@Composable
fun LandscapeLayout(navController: NavHostController? = null) {
    val isOnline by rememberOnlineState()
    var cityId by rememberSaveable { mutableStateOf("0") }
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
        ) {
            ListScreen(
                onCityClick = {
                    cityId = it
                },
            )
            if (isOnline) {
                Card(
                    onClick = { navController?.navigate(Screen.ROUTE.endpoint) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpaceM),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = stringResource(R.string.route_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = Dimens.SpaceXL),
                        )
                    }
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
        ) {
            DetailScreen(navController = navController, cityId = cityId)
        }
    }
}
