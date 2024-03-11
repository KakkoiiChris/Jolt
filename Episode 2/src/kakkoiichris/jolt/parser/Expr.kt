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
package kakkoiichris.jolt.parser

import kakkoiichris.jolt.lexer.Context

typealias Exprs = List<Expr>

sealed interface Expr {
    val context: Context

    fun <X> accept(visitor: Visitor<X>): X

    data class Value(override val context: Context, val value: Double) : Expr {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitValueExpr(this)
    }

    interface Visitor<X> {
        fun visit(expr: Expr) =
            expr.accept(this)

        fun visitValueExpr(expr: Value): X
    }
}