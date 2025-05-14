package com.boa.test.city.seeker.data.mapper

import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.domain.base.BaseMapper
import com.boa.test.city.seeker.domain.model.CityModel

/**
 * A mapper class responsible for transforming data between [CityEntity] (data layer)
 * and [CityModel] (domain layer).
 *
 * This class extends [BaseMapper] with [CityEntity] as the input type and [CityModel] as the output type,
 * providing a concrete implementation for the `map` function.
 */
class CityMapper : BaseMapper<CityEntity, CityModel>() {
    /**
     * Maps a [CityEntity] to a [CityModel].
     *
     * This function takes a [CityEntity] object as input and transforms it into a [CityModel] object.
     * It extracts the relevant data from the [CityEntity] and creates a new [CityModel] instance
     * with the same data.
     *
     * @param input The [CityEntity] object to be mapped.
     * @return A new [CityModel] object containing the mapped data.
     */
    override fun map(input: CityEntity): CityModel = CityModel(
        id = input.id,
        name = input.name,
        country = input.country,
        latitude = input.latitude,
        longitude = input.longitude
    )
}
