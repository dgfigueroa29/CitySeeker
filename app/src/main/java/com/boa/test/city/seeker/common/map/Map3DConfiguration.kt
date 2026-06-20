package com.boa.test.city.seeker.common.map

import timber.log.Timber

object Map3DConfiguration {
    private const val TAG = "Map3D"

    fun apply() {
        Timber.tag(TAG).d("3D: using LIGHT/DARK style with building extrusion at pitch > 0")
    }
}
