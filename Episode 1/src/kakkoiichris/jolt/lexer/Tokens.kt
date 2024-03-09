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

data class Context(
    val name: String,
    val row: Int,
    val column: Int,
    val start: Int,
    val end: Int,
) {
    operator fun rangeTo(other: Context) =
        Context(name, row, column, start, other.end)
}

sealed interface TokenType {
    data class Value(val value: Double) : TokenType

    data object EndOfFile : TokenType
}

data class Token(
    val context: Context,
    val type: TokenType,
)