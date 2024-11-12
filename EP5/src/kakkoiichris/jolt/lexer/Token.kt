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
 * @property length The length of the token
 */
data class Context(
    val name: String,
    val row: Int,
    val column: Int,
    val length: Int,
) {
    /**
     * @param other The end [Context] to encompass
     *
     * @return A new [Context] instance spanning from this token to the other token
     */
    operator fun rangeTo(other: Context) =
        Context(name, row, column, other.length - length + 1)
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
     * Token type that represents a unique single or multiple character symbol.
     *
     * @property rep The string representation of the token
     */
    enum class Symbol(val rep: String) : TokenType {
        /**
         * A plus sign « `+` » used for addition.
         */
        PLUS("+"),
        /**
         * A dash « `-` » used for negation and subtraction.
         */
        DASH("-"),
        /**
         * An asterisk « `*` » used for multiplication.
         */
        STAR("*"),
        /**
         * A forward slash « `/` » used for division.
         */
        SLASH("/"),
        /**
         * A percent sign « `%` » used for remainders.
         */
        PERCENT("%"),
        /**
         * A left parenthesis « `(` » used for nested expressions.
         */
        LEFT_PAREN("("),
        /**
         * A right parenthesis « `)` » used for nested expressions.
         */
        RIGHT_PAREN(")"),
        /**
         * A semicolon « `;` » used for the ends of statements.
         */
        SEMICOLON(";");

        /**
         * @return The [rep] of this entry
         */
        override fun toString() = rep
    }

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