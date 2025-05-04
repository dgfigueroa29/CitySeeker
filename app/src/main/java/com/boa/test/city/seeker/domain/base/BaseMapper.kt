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
    abstract fun map(input: I): O

    fun mapAll(input: Collection<I>?): List<O> =
        input?.mapNotNull { map(it) }?.distinct() ?: emptyList()
}
