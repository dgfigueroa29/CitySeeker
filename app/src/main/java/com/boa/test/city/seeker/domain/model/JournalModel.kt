package com.boa.test.city.seeker.domain.model

data class JournalModel(
    val id: Long = 0L,
    val cityId: Long = 0L,
    val cityName: String = "",
    val title: String = "",
    val notes: String = "",
    val rating: Int = 0,
    val photoUri: String? = null,
    val visitDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
)
