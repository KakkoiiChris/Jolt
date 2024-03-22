/********************************************
 * ::::::::::: ::::::::  :::    ::::::::::: *
 *     :+:    :+:    :+: :+:        :+:     *
 *     +:+    +:+    +:+ +:+        +:+     *
 *     +#+    +#+    +:+ +#+        +#+     *
 *     +#+    +#+    +#+ +#+        +#+     *
 * #+# #+#    #+#    #+# #+#        #+#     *
 *  #####      ########  ########## ###     *
 *            Scripting Language            *
 ********************************************/
package kakkoiichris.jolt.lexer

import kakkoiichris.jolt.JoltValue

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
    companion object {
        /**
         * A Context instance to be used for Exprs and Stmts that are not generated from code, and therefore do not have a physical location within a file.
         */
        val none = Context("", 0, 0, 0)
    }

    /**
     * @param other The end [Context] to encompass
     *
     * @return A new [Context] instance spanning from this token to the other token
     */
    operator fun rangeTo(other: Context) =
        Context(name, row, column, (other.column - column) + other.length - 1)
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
    data class Value<X : JoltValue<*>>(val value: X) : TokenType

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
        LET,
        VAR,
        IF,
        ELSE,
        LOOP,
        WHILE,
        DO,
        FOR,
        BREAK,
        CONTINUE,
    }

    data class OpenInterpolate(val value: JoltValue.String) : TokenType

    data class MidInterpolate(val value: JoltValue.String) : TokenType

    data class CloseInterpolate(val value: JoltValue.String) : TokenType

    /**
     * Token type that represents a unique single or multiple character symbol.
     *
     * @property rep The string representation of the token
     */
    enum class Symbol(private val rep: String) : TokenType {
        /**
         * An equals sign « `=` » used for declaration, assignment, default parameters, and named arguments.
         */
        EQUAL("="),

        /**
         * A double pipe « `||` » used for logical comparisons.
         */
        DOUBLE_PIPE("||"),

        /**
         * A double ampersand « `&&` » used for logical comparisons.
         */
        DOUBLE_AMPERSAND("&&"),

        /**
         * A double equal sign « `==` » used for equalities.
         */
        DOUBLE_EQUAL("=="),

        /**
         * An exclamation point equal sign « `!=` » used for equalities.
         */
        EXCLAMATION_EQUAL("!="),

        /**
         * A less than sign « `<` » used for comparisons.
         */
        LESS("<"),

        /**
         * A less than or equal sign « `<=` » used for comparisons.
         */
        LESS_EQUAL("<="),

        /**
         * A greater than sign « `>` » used for comparisons.
         */
        GREATER(">"),

        /**
         * A greater than or equal sign « `>=` » used for comparisons.
         */
        GREATER_EQUAL(">="),

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
         * A percent sign « `!` » used for logical comparisons.
         */
        EXCLAMATION("!"),

        /**
         * A pound sign « `#` » used for sizes.
         */
        POUND("#"),

        /**
         * A pound sign « `#` » used for indexing.
         */
        LEFT_SQUARE("["),

        /**
         * A pound sign « `#` » used for indexing.
         */
        RIGHT_SQUARE("]"),

        /**
         * A left parenthesis « `(` » used for nested expressions.
         */
        LEFT_PAREN("("),

        /**
         * A right parenthesis « `)` » used for nested expressions.
         */
        RIGHT_PAREN(")"),

        /**
         * A left brace « `{` » used for statement bodies and class definitions.
         */
        LEFT_BRACE("{"),

        /**
         * A right brace « `}` » used for statement bodies and class definitions.
         */
        RIGHT_BRACE("}"),

        /**
         * An at symbol « `@` » used for labels and redirects.
         */
        AT("@"),

        /**
         * A colon « `:` » used for for-loops and type annotations.
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