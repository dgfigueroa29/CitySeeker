package com.boa.test.city.seeker.data.mapper

import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.domain.base.BaseMapper
import com.boa.test.city.seeker.domain.model.CityModel

class CityMapper : BaseMapper<CityEntity, CityModel>() {
    override fun map(input: CityEntity): CityModel = CityModel(
        id = input.id,
        name = input.name,
        country = input.country,
        latitude = input.latitude,
        longitude = input.longitude
    )
}

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
