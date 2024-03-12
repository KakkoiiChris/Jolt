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
import kakkoiichris.jolt.lexer.TokenType

/**
 * Convenient type alias for a list of expressions.
 */
typealias Exprs = List<Expr>

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
     * Represents a single unary operator expression.
     *
     * @property operator The operator for this expression
     * @property expr The operand of this expression
     */
    data class Unary(override val context: Context, val operator: Operator, val expr: Expr) : Expr {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitUnaryExpr(this)

        /**
         *
         */
        enum class Operator(val symbol: TokenType.Symbol) {
            /**
             *
             */
            NEGATE(TokenType.Symbol.DASH);

            companion object {
                /**
                 *
                 */
                operator fun get(symbol: TokenType.Symbol) =
                    entries.first { it.symbol == symbol }
            }
        }
    }

    /**
     * Represents a single binary operator expression.
     *
     * @property operator The operator for this expression
     * @property left The left operand of this expression
     * @property right The right operand of this expression
     */
    data class Binary(override val context: Context, val operator: Operator, val left: Expr, val right: Expr) : Expr {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitBinaryExpr(this)

        /**
         *
         */
        enum class Operator(val symbol: TokenType.Symbol) {
            /**
             *
             */
            ADD(TokenType.Symbol.PLUS),

            /**
             *
             */
            SUBTRACT(TokenType.Symbol.DASH);

            companion object {
                /**
                 *
                 */
                operator fun get(symbol: TokenType.Symbol) =
                    entries.first { it.symbol == symbol }
            }
        }
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
         * @param expr The statement to visit
         */
        fun visitValueExpr(expr: Value): X

        /**
         * Visits a value expression.
         *
         * @param expr The statement to visit
         */
        fun visitUnaryExpr(expr: Unary): X

        /**
         * Visits a value expression.
         *
         * @param expr The statement to visit
         */
        fun visitBinaryExpr(expr: Binary): X
    }
}