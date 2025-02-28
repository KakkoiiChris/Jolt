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
     * Represents a single nested value expression.
     *
     * @property expr The value of this expression
     */
    data class Nested(override val context: Context, val expr: Expr) : Expr {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNestedExpr(this)
    }

    /**
     * Represents a single unary operator expression.
     *
     * @property operator The operator for this expression
     * @property operand The operand of this expression
     */
    data class Unary(override val context: Context, val operator: Operator, val operand: Expr) : Expr {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitUnaryExpr(this)

        /**
         * An enum containing all possible unary operators.
         *
         * @property symbol The symbol token type associated with this operator
         */
        enum class Operator(val symbol: TokenType.Symbol) {
            /**
             * The negation operator.
             */
            NEGATE(TokenType.Symbol.DASH);

            companion object {
                /**
                 * @return The operator whose symbol equals the given symbol
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
     * @property operandLeft The left operand of this expression
     * @property operandRight The right operand of this expression
     */
    data class Binary(override val context: Context, val operator: Operator, val operandLeft: Expr, val operandRight: Expr) : Expr {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitBinaryExpr(this)

        /**
         * An enum containing all possible binary operators.
         *
         * @property symbol The symbol token type associated with this operator
         */
        enum class Operator(val symbol: TokenType.Symbol) {
            /**
             * The addition operator.
             */
            ADD(TokenType.Symbol.PLUS),

            /**
             * The subtraction operator.
             */
            SUBTRACT(TokenType.Symbol.DASH),

            /**
             * The multiplication operator.
             */
            MULTIPLY(TokenType.Symbol.STAR),

            /**
             * The division operator.
             */
            DIVIDE(TokenType.Symbol.SLASH),

            /**
             * The remainder operator.
             */
            REMAINDER(TokenType.Symbol.PERCENT);

            companion object {
                /**
                 * @return The operator whose symbol equals the given symbol
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
         * @param expr The expression to visit
         */
        fun visitValueExpr(expr: Value): X

        /**
         * Visits a nested expression.
         *
         * @param expr The expression to visit
         */
        fun visitNestedExpr(expr: Nested): X

        /**
         * Visits a unary operator expression.
         *
         * @param expr The expression to visit
         */
        fun visitUnaryExpr(expr: Unary): X


        /**
         * Visits a binary operator expression.
         *
         * @param expr The expression to visit
         */
        fun visitBinaryExpr(expr: Binary): X
    }
}
