package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.domain.model.CityModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CityTrieTest {
    private lateinit var trie: OptimizedCityTrie

    private val denverCity =
        CityModel(
            id = 1,
            name = "Denver",
            country = "US",
            latitude = 39.7392,
            longitude = -104.9903,
        )

    private val dallasCity =
        CityModel(
            id = 2,
            name = "Dallas",
            country = "US",
            latitude = 32.7767,
            longitude = -96.7970,
        )

    private val sydneyCity =
        CityModel(
            id = 3,
            name = "Sydney",
            country = "AU",
            latitude = -33.8688,
            longitude = 151.2093,
        )

    @Before
    fun setup() {
        trie = OptimizedCityTrie()
        trie.insert(denverCity)
        trie.insert(dallasCity)
        trie.insert(sydneyCity)
    }

    @Test
    fun `search with prefix D returns Denver and Dallas`() {
        val results = trie.search("D")
        assertEquals(2, results.size)
        assertTrue(results.containsAll(listOf(dallasCity, denverCity)))
    }

    @Test
    fun `search with prefix Den returns only Denver`() {
        val results = trie.search("Den")
        assertEquals(1, results.size)
        assertEquals("Denver", results.first().name)
    }

    @Test
    fun `search is case insensitive`() {
        val results = trie.search("den")
        assertEquals(1, results.size)
        assertEquals("Denver", results.first().name)
    }

    @Test
    fun `search with prefix S returns Sydney`() {
        val results = trie.search("S")
        assertEquals(1, results.size)
        assertEquals("Sydney", results.first().name)
    }

    @Test
    fun `search with empty prefix returns all cities`() {
        val results = trie.search("")
        assertEquals(3, results.size)
    }

    @Test
    fun `search with non-existing prefix returns empty`() {
        val results = trie.search("XYZ")
        assertEquals(0, results.size)
    }

    @Test
    fun `search results are sorted by name then country`() {
        val results = trie.search("D")
        assertEquals("Dallas", results[0].name)
        assertEquals("Denver", results[1].name)
    }

    @Test
    fun `search by country code`() {
        val results = trie.search("AU")
        assertEquals(1, results.size)
        assertEquals("Sydney", results.first().name)
    }

    @Test
    fun `search is case insensitive for country`() {
        val results = trie.search("au")
        assertEquals(1, results.size)
        assertEquals("Sydney", results.first().name)
    }

    @Test
    fun `insert duplicate city does not create duplicates`() {
        trie.insert(denverCity)
        val results = trie.search("Den")
        assertEquals(1, results.size)
    }

    @Test
    fun `search with prefix Alb returns Albuquerque only`() {
        val albuquerque = CityModel(4, "Albuquerque", "US", 35.0844, -106.6504)
        trie.insert(albuquerque)

        val results = trie.search("Alb")
        assertEquals(1, results.size)
        assertEquals("Albuquerque", results.first().name)
    }

    @Test
    fun `search with prefix A returns all cities starting with A`() {
        val anaheim = CityModel(5, "Anaheim", "US", 33.8366, -117.9143)
        val arizona = CityModel(6, "Arizona", "US", 34.0489, -111.0937)
        trie.insert(anaheim)
        trie.insert(arizona)

        val results = trie.search("Ana")
        assertEquals(1, results.size)
        assertEquals("Anaheim", results.first().name)
    }

    @Test
    fun `search with special characters returns empty`() {
        val results = trie.search("@#\$")
        assertEquals(0, results.size)
    }

    @Test
    fun `search with single character returns matching cities`() {
        val results = trie.search("D")
        assertEquals(2, results.size)
    }

    @Test
    fun `search with full city name returns that city`() {
        val results = trie.search("Denver")
        assertEquals(1, results.size)
        assertEquals("Denver", results.first().name)
    }

    @Test
    fun `search with prefix longer than any city returns empty`() {
        val results = trie.search("Denverxyz")
        assertEquals(0, results.size)
    }

    @Test
    fun `insert multiple cities with same prefix`() {
        val denver2 = CityModel(7, "Denver", "CA", 49.8880, -119.4960)
        trie.insert(denver2)

        val results = trie.search("Den")
        assertEquals(2, results.size)
        assertTrue(results.any { it.country == "US" })
        assertTrue(results.any { it.country == "CA" })
    }
}
