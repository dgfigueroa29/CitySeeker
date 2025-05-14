package com.boa.test.city.seeker.domain.base

/**
 * BaseMapper is an abstract class that defines a contract for mapping objects of type [I] to objects of type [O].
 *
 * It provides a basic implementation for mapping a collection of objects using the [mapAll] function.
 *
 * @param I The input type to be mapped.
 * @param O The output type after mapping.
 */
abstract class BaseMapper<I, O> {
    /**
     * Abstract function that defines the mapping logic from an object of type [I] to
     * an object of type [O].
     *
     * Subclasses must implement this function to provide the specific mapping transformation.
     *
     * @param input The object of type [I] to be mapped.
     * @return The mapped object of type [O].
     */
    abstract fun map(input: I): O

    /**
     * Maps a collection of input objects to a list of distinct output objects.
     *
     * This function takes an optional collection of input objects and applies the [map]
     * function to each non-null element.
     * It then filters out any null results from the mapping and returns a list containing
     * only the distinct non-null output objects.
     * If the input collection is null, an empty list is returned.
     *
     * @param input The collection of input objects to be mapped, can be null.
     * @return A list of distinct output objects, or an empty list if the input is null or all
     * mappings result in null.
     */
    fun mapAll(input: Collection<I>?): List<O> =
        input?.mapNotNull { map(it) }?.distinct() ?: emptyList()
}
