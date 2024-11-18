/*      ___  _______  ___    _______
 *     |   ||       ||   |  |       |
 *     |   ||   _   ||   |  |_     _|
 *     |   ||  | |  ||   |    |   |
 *  ___|   ||  |_|  ||   |___ |   |
 * |       ||       ||       ||   |
 * |_______||_______||_______||___|
 *         SCRIPTING LANGUAGE
 */
package kakkoiichris.jolt.parser

import kakkoiichris.jolt.lexer.Context
import kakkoiichris.jolt.lexer.TokenType

data class Type(val context: Context, val value: DataType)

interface DataType {
    fun matches(other: DataType): Boolean
}

data object Inferred : DataType {
    override fun matches(other: DataType) = true
}

enum class Primitive : DataType {
    NUM;

    override fun matches(other: DataType) =
        equals(other)

    companion object {
        operator fun get(keyword: TokenType.Keyword) =
            entries.firstOrNull { it.name == keyword.name }
    }

    override fun toString() =
        name.lowercase()
}
