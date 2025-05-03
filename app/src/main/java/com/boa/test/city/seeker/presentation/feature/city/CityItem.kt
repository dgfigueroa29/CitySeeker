package com.boa.test.city.seeker.presentation.feature.city

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.navigation.Screen
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryDark
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryLight
import com.boa.test.city.seeker.presentation.ui.theme.PrimaryOff

/**
 * A composable function that displays a single city item in a list.
 *
 * This function renders a card representing a city, including its name, country,
 * favorite status (with a star icon), and a details button. Clicking on the card
 * or the details button navigates the user to the city detail screen.
 * Clicking on the star icon toggles the city's favorite status.
 *
 * @param city The [CityModel] containing the data for the city to be displayed.
 * @param onToggleFavorite A lambda function that is called when the user toggles
 */
@Composable
fun CityItem(
    city: CityModel,
    onToggleFavorite: () -> Unit,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("${Screen.CITY}/${city.id}") },
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${city.name}, ${city.country}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lat: ${city.latitude}, Long: ${city.longitude}",
                    fontSize = 14.sp,
                    color = PrimaryOff
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onToggleFavorite) {
                if (city.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(R.string.favorite_selected),
                        tint = PrimaryDark
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = stringResource(R.string.favorite_unselected),
                        tint = PrimaryLight
                    )
                }
            }
            IconButton(onClick = { navController.navigate("${Screen.CITY}/${city.id}") }) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.details),
                    tint = PrimaryOff
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteCityItemPreview() {
    CityItem(
        city = CityModel(
            id = 1,
            name = "Mendoza",
            country = "AR",
            isFavorite = true,
            latitude = 0.0,
            longitude = 0.0
        ),
        onToggleFavorite = {},
        navController = NavHostController(LocalContext.current)
    )
}

@Preview(showBackground = true)
@Composable
fun CityItemPreview() {
    CityItem(
        city = CityModel(
            id = 2,
            name = "San Juan",
            country = "AR",
            isFavorite = false,
            latitude = 0.0,
            longitude = 0.0
        ),
        onToggleFavorite = {},
        navController = NavHostController(LocalContext.current)
    )
}
