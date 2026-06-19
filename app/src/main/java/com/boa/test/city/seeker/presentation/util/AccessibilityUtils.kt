package com.boa.test.city.seeker.presentation.util

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Modifier extension that adds a content description for accessibility.
 * Use this to provide screen reader descriptions for composables.
 */
fun androidx.compose.ui.Modifier.accessibilityDescription(description: String): androidx.compose.ui.Modifier =
    this.semantics {
        contentDescription = description
    }

/**
 * Modifier extension that combines multiple accessibility descriptions.
 * Useful for composables that need to convey multiple pieces of information.
 */
fun androidx.compose.ui.Modifier.accessibilityDescriptions(vararg descriptions: String): androidx.compose.ui.Modifier =
    this.semantics {
        contentDescription = descriptions.joinToString(", ")
    }
