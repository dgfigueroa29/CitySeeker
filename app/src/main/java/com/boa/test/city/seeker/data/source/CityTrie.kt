package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.domain.model.CityModel

class CityTrie {
    private data class Node(
        val children: MutableMap<Char, Node> = mutableMapOf(),
        var cities: MutableSet<CityModel> = mutableSetOf()
    )

    private val root = Node()

    fun insert(city: CityModel) {
        insertWord(city.name, city)
        insertWord(city.country, city)
    }

    private fun insertWord(word: String, city: CityModel) {
        var current = root
        word.forEach { char ->
            current = current.children.getOrPut(char) { Node() }
            current.cities.add(city)
        }
    }

    fun search(prefix: String): List<CityModel> {
        var current = root

        if (prefix.isNotBlank()) {
            val normalizedPrefix = prefix.lowercase()
            for (char in normalizedPrefix) {
                current = current.children[char] ?: return emptyList()
            }
        }

        return current.cities
            .sortedWith(compareBy({ it.name }, { it.country }))
    }
}