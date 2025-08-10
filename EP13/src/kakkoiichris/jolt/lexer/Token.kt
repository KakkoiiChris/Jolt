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

import kakkoiichris.jolt.JoltValue

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

    companion object {
        val none = Context("", 0, 0, 0, 0)
    }
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
    data class Value(val value: JoltValue<*>) : TokenType

    /**
     * Token type that represents a name or variable.
     *
     * @property value The value of the token
     */
    data class Name(val value: String) : TokenType

    /**
     * Token type that represents a keyword.
     */
    enum class Keyword : TokenType {
        /**
         * Constant Declarations
         */
        LET,

        /**
         * Variable Declarations
         */
        VAR,

        /**
         * If Statements
         */
        IF,

        /**
         * Else Statements
         */
        ELSE,

        /**
         * While Statements
         */
        WHILE,

        /**
         * Do-While Statements
         */
        DO,

        /**
         * For Statements
         */
        FOR,

        /**
         * Break Statements
         */
        BREAK,

        /**
         * Continue Statements
         */
        CONTINUE,

        /**
         * Function Statements
         */
        FUN,

        /**
         * Return Statements
         */
        RETURN,
    }

    /**
     * Token type that represents a unique single or multiple character symbol.
     *
     * @property rep The string representation of the token
     */
    enum class Symbol(val rep: String) : TokenType {
        /**
         * A pipe « `|` » used for logical or.
         */
        PIPE("|"),

        /**
         * A caret « `^` » used for logical exclusive or.
         */
        CARET("^"),

        /**
         * An ampersand « `&` » used for logical and.
         */
        AMPERSAND("&"),

        /**
         * A double equal sign « `==` » used for equality.
         */
        DOUBLE_EQUAL("=="),

        /**
         * An equals sign « `=` » used for declaration, assignment, default parameters, and named arguments.
         */
        EQUAL("="),

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
         * A less than or equal sign « `<=` » used for order comparisons.
         */
        LESS_EQUAL("<="),

        /**
         * A less than sign « `<` » used for order comparisons.
         */
        LESS("<"),

        /**
         * A greater than or equal sign « `>=` » used for remainder order comparisons.
         */
        GREATER_EQUAL(">="),

        /**
         * A greater than sign « `>` » used for order comparisons.
         */
        GREATER(">"),

        /**
         * An exclamation point equal sign « `!=` » used for inequality.
         */
        EXCLAMATION_EQUAL("!="),

        /**
         * An exclamation mark « `!` » used for logical negation.
         */
        EXCLAMATION("!"),

        /**
         * A pound sign « `#` » used for lengths.
         */
        POUND("#"),

        /**
         * A left parenthesis « `(` » used for nested expressions.
         */
        LEFT_PAREN("("),

        /**
         * A right parenthesis « `)` » used for nested expressions.
         */
        RIGHT_PAREN(")"),

        /**
         * A left brace « `{` » used for block statements.
         */
        LEFT_BRACE("{"),

        /**
         * A right brace « `}` » used for block statements.
         */
        RIGHT_BRACE("}"),

        /**
         * A left square « `[` » used for indexing.
         */
        LEFT_SQUARE("["),

        /**
         * A right square « `]` » used for indexing.
         */
        RIGHT_SQUARE("]"),

        /**
         * An at sign « `@` » used for loop labels.
         */
        AT("@"),

        /**
         * A comma « `,` » used for separating list elements.
         */
        COMMA(","),

        /**
         * A colon « `:` » used in for loops.
         */
        COLON(":"),

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