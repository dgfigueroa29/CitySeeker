package com.boa.test.city.seeker.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.di.DataStoreEntryPoint
import com.boa.test.city.seeker.di.RepositoryEntryPoint
import com.boa.test.city.seeker.presentation.ui.theme.DarkColorScheme
import com.boa.test.city.seeker.presentation.ui.theme.LightColorScheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class FavoritesWidget : GlanceAppWidget() {
    companion object {
        private val FAVORITE_CITIES = stringSetPreferencesKey("favorite_cities")
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dataStoreEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                DataStoreEntryPoint::class.java,
            )
        val repositoryEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                RepositoryEntryPoint::class.java,
            )
        val dataStore = dataStoreEntryPoint.dataStore()
        val cityRepository = repositoryEntryPoint.cityRepository()
        val favoriteIds: Set<String> =
            dataStore.data.first()[FAVORITE_CITIES] ?: emptySet()

        val cityNames =
            favoriteIds.mapNotNull { id ->
                cityRepository
                    .getCityById(id.toLongOrNull() ?: return@mapNotNull null)
                    .mapNotNull { city -> city.name.ifBlank { null } }
                    .first()
            }

        val title = context.getString(R.string.widget_title)
        val noFavorites = context.getString(R.string.widget_no_favorites)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        provideContent {
            GlanceTheme(
                colors =
                    ColorProviders(
                        light = LightColorScheme,
                        dark = DarkColorScheme,
                    ),
            ) {
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(GlanceTheme.colors.surface)
                            .padding(16)
                            .clickable(
                                actionStartActivity(
                                    launchIntent ?: Intent(),
                                ),
                            ),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = title,
                        style =
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface,
                            ),
                    )
                    Spacer(modifier = GlanceModifier.height(8))

                    if (cityNames.isEmpty()) {
                        Text(
                            text = noFavorites,
                            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                        )
                    } else {
                        cityNames.take(5).forEach { name ->
                            Text(
                                text = name,
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2),
                                style = TextStyle(color = GlanceTheme.colors.onSurface),
                            )
                        }
                    }
                }
            }
        }
    }
}
