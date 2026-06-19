package com.boa.test.city.seeker.common.analytics

sealed interface AnalyticsEvent {
    val name: String
    val properties: Map<String, Any>

    sealed interface Onboarding : AnalyticsEvent {
        data object Started : Onboarding {
            override val name get() = "onboarding_started"
            override val properties get() = emptyMap<String, Any>()
        }

        data class Completed(
            val slideCount: Int,
        ) : Onboarding {
            override val name get() = "onboarding_completed"
            override val properties get() = mapOf("slide_count" to slideCount)
        }

        data class Skipped(
            val currentSlide: Int,
        ) : Onboarding {
            override val name get() = "onboarding_skipped"
            override val properties get() = mapOf("current_slide" to currentSlide)
        }
    }

    sealed interface Search : AnalyticsEvent {
        override val name get() = "search_executed"

        data class Query(
            val query: String,
            val resultCount: Int,
            val durationMs: Long,
        ) : Search {
            override val properties
                get() =
                    mapOf(
                        "query" to query,
                        "result_count" to resultCount,
                        "duration_ms" to durationMs,
                    )
        }
    }

    sealed interface City : AnalyticsEvent {
        data class View(
            val cityId: String,
            val cityName: String,
        ) : City {
            override val name get() = "city_viewed"
            override val properties get() = mapOf("city_id" to cityId, "city_name" to cityName)
        }

        data class ToggleFavorite(
            val cityId: String,
            val isFavorite: Boolean,
        ) : City {
            override val name get() = "favorite_toggled"
            override val properties
                get() =
                    mapOf(
                        "city_id" to cityId,
                        "is_favorite" to isFavorite,
                    )
        }
    }

    sealed interface Scroll : AnalyticsEvent {
        data class Depth(
            val index: Int,
        ) : Scroll {
            override val name get() = "scroll_depth"
            override val properties get() = mapOf("index" to index)
        }
    }

    sealed interface Error : AnalyticsEvent {
        data class Occurred(
            val throwable: Throwable,
            val context: String,
        ) : Error {
            override val name get() = "error_occurred"
            override val properties
                get() =
                    mapOf(
                        "context" to context,
                        "message" to (throwable.message ?: ""),
                    )
        }
    }

    sealed interface Performance : AnalyticsEvent {
        data class Trace(
            val operation: String,
            val durationMs: Long,
        ) : Performance {
            override val name get() = "performance_trace"
            override val properties
                get() =
                    mapOf(
                        "operation" to operation,
                        "duration_ms" to durationMs,
                    )
        }
    }
}
