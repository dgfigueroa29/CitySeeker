package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.domain.model.CityModel

/**
 * An optimized trie data structure for storing and searching [CityModel] objects.
 *
 * This implementation stores city IDs instead of full objects to reduce memory usage,
 * which is especially important when dealing with large datasets (200k+ cities).
 *
 * The trie allows for efficient prefix-based searching, meaning you can quickly find
 * all cities whose names or countries start with a given string.
 *
 * @property citiesMap A map of city IDs to CityModel objects for quick lookup
 */
class OptimizedCityTrie {
    /**
     * Represents a node in the Trie data structure.
     *
     * Each node stores a map of its children (keyed by character) and a set of city IDs
     * that are associated with the prefix represented by the path from the root to this node.
     *
     * @property children A mutable map where keys are characters and values are child nodes.
     * @property cityIds A mutable set of city IDs associated with this node's prefix.
     */
    private data class Node(
        val children: MutableMap<Char, Node> = mutableMapOf(),
        val cityIds: MutableSet<Long> = mutableSetOf(),
    )

    private val root = Node()
    private val citiesMap = mutableMapOf<Long, CityModel>()

    /**
     * Inserts a city into the trie.
     * The city will be indexed by its name and its country.
     *
     * @param city The [CityModel] to insert.
     */
    fun insert(city: CityModel) {
        citiesMap[city.id] = city
        root.cityIds.add(city.id)
        insertWord(city.name, city.id)
        insertWord(city.country, city.id)
    }

    /**
     * Inserts a given word into the trie, associating it with the provided city ID.
     *
     * For each character in the word, this function traverses or creates nodes in the trie.
     * At each node along the path, it adds the city ID to the list of city IDs
     * associated with that node. This allows for efficient retrieval of cities
     * based on prefixes.
     *
     * @param word The word (e.g., city name or country name) to insert into the trie.
     * @param cityId The ID of the city to associate with the word.
     */
    private fun insertWord(
        word: String,
        cityId: Long,
    ) {
        var current = root
        word.lowercase().forEach { char ->
            current = current.children.getOrPut(char) { Node() }
            current.cityIds.add(cityId)
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

        return current.cityIds
            .mapNotNull { citiesMap[it] }
            .sortedWith(compareBy({ it.name }, { it.country }))
    }

    /**
     * Returns the total number of cities stored in the trie.
     *
     * @return The number of cities in the trie.
     */
    fun size(): Int = citiesMap.size

    /**
     * Clears all data from the trie.
     */
    fun clear() {
        root.children.clear()
        root.cityIds.clear()
        citiesMap.clear()
    }
}
