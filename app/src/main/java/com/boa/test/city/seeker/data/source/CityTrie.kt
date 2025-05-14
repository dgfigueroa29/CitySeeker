package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.domain.model.CityModel

/**
 * A trie data structure specifically designed for storing and searching [CityModel] objects
 * based on their names and countries.
 *
 * This trie allows for efficient prefix-based searching, meaning you can quickly find
 * all cities whose names or countries start with a given string.
 *
 * Each node in the trie can store a set of [CityModel] objects that are associated with
 * the path leading to that node.
 */
class CityTrie {
    /**
     * Represents a node in the Trie data structure.
     *
     * Each node stores a map of its children (keyed by character) and a set of cities
     * that are associated with the prefix represented by the path from the root to this node.
     *
     * @property children A mutable map where keys are characters and values are child nodes.
     * @property cities A mutable set of [CityModel] objects associated with this node's prefix.
     */
    private data class Node(
        val children: MutableMap<Char, Node> = mutableMapOf(),
        var cities: MutableSet<CityModel> = mutableSetOf()
    )

    private val root = Node()

    /**
     * Inserts a city into the trie.
     * The city will be indexed by its name and its country.
     *
     * @param city The [CityModel] to insert.
     */
    fun insert(city: CityModel) {
        insertWord(city.name, city)
        insertWord(city.country, city)
    }

    /**
     * Inserts a given word into the trie, associating it with the provided city.
     *
     * For each character in the word, this function traverses or creates nodes in the trie.
     * At each node along the path, it adds the associated city to the list of cities
     * associated with that node, if it's not already present. This allows for efficient
     * retrieval of cities based on prefixes.
     *
     * @param word The word (e.g., city name or country name) to insert into the trie.
     * @param city The [CityModel] to associate with the word.
     */
    private fun insertWord(word: String, city: CityModel) {
        var current = root
        word.forEach { char ->
            current = current.children.getOrPut(char) { Node() }

            if (current.cities.contains(city).not()) {
                current.cities.add(city)
            }
        }
    }

    /**
     * Searches for cities that have a name or country starting with the given prefix.
     *
     * @param prefix The prefix to search for. The search is case-insensitive.
     * @return A sorted list of [CityModel] objects matching the prefix. The list is sorted
     * first by city name and then by country.
     *         If no cities are found for the given prefix, an empty list is returned.
     */
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
