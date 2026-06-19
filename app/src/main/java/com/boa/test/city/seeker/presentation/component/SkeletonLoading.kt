package com.boa.test.city.seeker.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.boa.test.city.seeker.presentation.ui.theme.Dimens

@Composable
fun CityItemSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel0),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(Dimens.SpaceM))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusSmall))
                            .shimmerEffect(),
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusSmall))
                            .shimmerEffect(),
                )
            }
            Box(
                modifier =
                    Modifier
                        .size(Dimens.IconL)
                        .clip(CircleShape)
                        .shimmerEffect(),
            )
        }
    }
}

@Composable
fun CityListSkeleton(itemCount: Int = 8) {
    LazyColumn(
        contentPadding =
            PaddingValues(
                horizontal = Dimens.SpaceM,
                vertical = Dimens.SpaceXS,
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
    ) {
        items(itemCount, key = { it }) {
            CityItemSkeleton()
        }
    }
}
