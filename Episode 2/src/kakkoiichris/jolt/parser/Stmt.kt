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

typealias Stmts = List<Stmt>

sealed interface Stmt {
    val context: Context

    fun <X> accept(visitor: Visitor<X>): X

    data class Empty(override val context: Context) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitEmptyStmt(this)
    }

    data class Expression(override val context: Context, val expr: Expr) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitExpressionStmt(this)
    }

    interface Visitor<X> {
        fun visit(stmt: Expression) =
            stmt.accept(this)

        fun visitEmptyStmt(stmt: Empty): X

        fun visitExpressionStmt(stmt: Expression): X
    }
}