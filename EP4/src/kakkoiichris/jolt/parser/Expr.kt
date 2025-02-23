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

/**
 * Interface to represent all valid program expressions.
 */
sealed interface Expr {
    /**
     * The location data of important portion of this expression.
     */
    val context: Context

    /**
     * Delegates calls to the visitor's visit function to the method designated for this particular expression.
     *
     * @param X The type of values to be produced by this expression
     * @param visitor The visitor to visit
     */
    fun <X> accept(visitor: Visitor<X>): X

    /**
     * Represents a single value expression.
     *
     * @property value The value of this expression
     */
    data class Value(override val context: Context, val value: Double) : Expr {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitValueExpr(this)
    }

    /**
     * An interface that facilitates walking the expression tree.
     *
     * @param X The type of values to be produced by this visitor
     */
    interface Visitor<X> {
        /**
         * The default visit method that delegates to all expressions.
         *
         * @param expr The expression to visit
         */
        fun visit(expr: Expr) =
            expr.accept(this)

        /**
         * Visits a value expression.
         *
         * @param expr The expression to visit
         */
        fun visitValueExpr(expr: Value): X
    }
}
