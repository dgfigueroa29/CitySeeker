package com.boa.test.city.seeker.domain.util

/**
 * Removes special characters from a string.
 *
 * This extension function removes any characters that are not alphanumeric or spaces from a given string.
 * If the input string is null or blank, it returns an empty string.
 *
 * @receiver The string to remove special characters from. Can be null.
 * @return A new string with all special characters removed, or an empty string if the input was null or blank.
 *
 * Example:
 * ```kotlin
 * val str1 = "Hello, World! 123"
 * val str2 = null
 * val str3 = ""
 * val str4 = "Test@#$"
 *
 * println(str1.removeSpecialCharacters()) // Output: Hello World 123
 * println(str2.removeSpecialCharacters()) // Output:
 * println(str3.removeSpecialCharacters()) // Output:
 * println(str4.removeSpecialCharacters()) // Output: Test
 * ```
 */
fun String?.removeSpecialCharacters(): String {
    return if (this.isNullOrBlank()) {
        ""
    } else {
        this.replace(Regex("[^A-Za-z0-9 -]"), "")
    }
}
