package com.boa.test.city.seeker.presentation.util

import android.app.Activity
import android.content.Context
import android.view.HapticFeedbackConstants

object HapticFeedbackManager {
    fun performClick(context: Context) {
        val view = (context as? Activity)?.window?.decorView
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun performConfirm(context: Context) {
        val view = (context as? Activity)?.window?.decorView
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun performReject(context: Context) {
        val view = (context as? Activity)?.window?.decorView
        view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    fun performToggle(context: Context) {
        val view = (context as? Activity)?.window?.decorView
        view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}
