package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.lexer.Context
import kakkoiichris.jolt.parser.Expr

interface Invocable {
    val scope: Memory.Scope
    val name: Expr.Name
    val isVariadic: Boolean
    val params: List<Parameter>

    data class Parameter(
        val context: Context,
        val isConstant: Boolean?,
        val name: Expr.Name,
        val default: Expr?
    )
}