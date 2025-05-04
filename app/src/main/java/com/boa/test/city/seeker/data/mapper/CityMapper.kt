package com.boa.test.city.seeker.data.mapper

import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.domain.model.CityModel

fun CityEntity.toDomainModel(): CityModel {
    return CityModel(
        id = id,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude
    )
}

fun CityModel.toEntity(): CityEntity {
    return CityEntity(
        id = id,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude
    )
}
