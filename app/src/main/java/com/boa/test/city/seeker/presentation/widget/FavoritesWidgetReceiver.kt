package com.boa.test.city.seeker.presentation.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class FavoritesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: FavoritesWidget
        get() = widget

    companion object {
        private val widget = FavoritesWidget()
    }
}
