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

    val type: DataType

    /**
     * Delegates calls to the visitor's visit function to the method designated for this particular expression.
     *
     * @param X The type of values to be produced by this expression
     * @param visitor The visitor to visit
     */
    fun <X> accept(visitor: Visitor<X>): X

    data object None:Expr{
        override val context = Context.none

        override val type = Inferred

        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNoneExpr(this)
    }

    /**
     * Represents a single value expression.
     *
     * @property value The value of this expression
     */
    data class Value(override val context: Context, val value: Double) : Expr {
        override val type get() = Primitive.NUM

        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitValueExpr(this)
    }

    /**
     * Represents a single name expression.
     *
     * @property value The name of this expression
     */
    data class Name(override val context: Context, val value: String) : Expr {
        override var type = Primitive.NUM

        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitNameExpr(this)
    }

    /**
     * Represents a single nested value expression.
     *
     * @property expr The value of this expression
     */
    data class Nested(override val context: Context, val expr: Expr) : Expr {
        override val type get() = expr.type

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
        override val type get() = operator.getType(operand)

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
            NEGATE(TokenType.Symbol.DASH) {
                override fun getType(operand: Expr) = when (operand.type) {
                    Primitive.NUM -> Primitive.NUM

                    else          -> TODO("NEGATE")
                }
            };

            abstract fun getType(operand: Expr): DataType

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
    data class Binary(
        override val context: Context,
        val operator: Operator,
        val operandLeft: Expr,
        val operandRight: Expr
    ) : Expr {
        override val type get() = operator.getType(operandLeft, operandRight)

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
            ADD(TokenType.Symbol.PLUS) {
                override fun getType(operandLeft: Expr, operandRight: Expr) = when (operandLeft.type) {
                    Primitive.NUM -> when (operandRight.type) {
                        Primitive.NUM -> Primitive.NUM

                        else          -> TODO("ADD RIGHT")
                    }

                    else          -> TODO("ADD LEFT")
                }
            },

            /**
             * The subtraction operator.
             */
            SUBTRACT(TokenType.Symbol.DASH) {
                override fun getType(operandLeft: Expr, operandRight: Expr) = when (operandLeft.type) {
                    Primitive.NUM -> when (operandRight.type) {
                        Primitive.NUM -> Primitive.NUM

                        else          -> TODO("SUBTRACT RIGHT")
                    }

                    else          -> TODO("SUBTRACT LEFT")
                }
            },

            /**
             * The multiplication operator.
             */
            MULTIPLY(TokenType.Symbol.STAR) {
                override fun getType(operandLeft: Expr, operandRight: Expr) = when (operandLeft.type) {
                    Primitive.NUM -> when (operandRight.type) {
                        Primitive.NUM -> Primitive.NUM

                        else          -> TODO("MULTIPLY RIGHT")
                    }

                    else          -> TODO("MULTIPLY LEFT")
                }
            },

            /**
             * The division operator.
             */
            DIVIDE(TokenType.Symbol.SLASH) {
                override fun getType(operandLeft: Expr, operandRight: Expr) = when (operandLeft.type) {
                    Primitive.NUM -> when (operandRight.type) {
                        Primitive.NUM -> Primitive.NUM

                        else          -> TODO("DIVIDE RIGHT")
                    }

                    else          -> TODO("DIVIDE LEFT")
                }
            },

            /**
             * The remainder operator.
             */
            REMAINDER(TokenType.Symbol.PERCENT) {
                override fun getType(operandLeft: Expr, operandRight: Expr) = when (operandLeft.type) {
                    Primitive.NUM -> when (operandRight.type) {
                        Primitive.NUM -> Primitive.NUM

                        else          -> TODO("REMAINDER RIGHT")
                    }

                    else          -> TODO("REMAINDER LEFT")
                }
            };

            abstract fun getType(operandLeft: Expr, operandRight: Expr): DataType

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
     * Represents a single assignment operator expression.
     *
     * @property name The variable name to assign to
     * @property value The value to assign
     */
    data class Assign(override val context: Context, val name: Name, val value: Expr) : Expr {
        override val type get() = value.type

        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitAssignExpr(this)
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
         * Visits a none expression.
         *
         * @param expr The expression to visit
         */
        fun visitNoneExpr(expr: None): X

        /**
         * Visits a value expression.
         *
         * @param expr The expression to visit
         */
        fun visitValueExpr(expr: Value): X

        /**
         * Visits a name expression.
         *
         * @param expr The expression to visit
         */
        fun visitNameExpr(expr: Name): X

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

        /**
         * Visits an assignment expression.
         *
         * @param expr The expression to visit
         */
        fun visitAssignExpr(expr: Assign): X
    }
}
