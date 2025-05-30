/*      ___  _______  ___    _______
 *     |   ||       ||   |  |       |
 *     |   ||   _   ||   |  |_     _|
 *     |   ||  | |  ||   |    |   |
 *  ___|   ||  |_|  ||   |___ |   |
 * |       ||       ||       ||   |
 * |_______||_______||_______||___|
 *         SCRIPTING LANGUAGE
 */
package kakkoiichris.jolt.lexer

/**
 * A container for location data related to tokens.
 *
 * @property name The name of the file this token originated from
 * @property row The row this token originated from
 * @property column The column this token originated from
 * @property start The starting position of the token
 * @property end The ending position of the token
 */
data class Context(
    val name: String,
    val row: Int,
    val column: Int,
    val start: Int,
    val end: Int
) {
    /**
     * @return The horizontal length of this token
     */
    val length get() = end - start

    /**
     * @param other The end [Context] to encompass
     *
     * @return A new [Context] instance spanning from this token to the other token
     */
    operator fun rangeTo(other: Context) =
        Context(name, row, column, start, other.end - 1)
}

/**
 * A fixed set of forms that tokens can take.
 */
sealed interface TokenType {
    /**
     * Token type that represents a single value.
     *
     * @property value The value of the token
     */
    data class Value(val value: Double) : TokenType

    /**
     * Token type that represent the end of the file.
     */
    data object EndOfFile : TokenType
}

/**
 * A class to represent the smallest lexical units of the source code.
 *
 * @param T The token type to be associated with this token
 * @property context The location data for this token
 * @property type The type of this token
 */
data class Token<T : TokenType>(
    val context: Context,
    val type: T,
)