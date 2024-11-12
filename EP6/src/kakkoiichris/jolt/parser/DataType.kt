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

interface DataType

enum class Primitive : DataType {
    NUM;

    companion object {
        operator fun get(keyword: TokenType.Keyword) =
            entries.firstOrNull { it.name == keyword.name }
    }

    override fun toString() =
        name.lowercase()
}
