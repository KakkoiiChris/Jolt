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
 * Convenient type alias for a list of statements.
 */
typealias Stmts = List<Stmt>

/**
 * Interface to represent all valid program statements.
 */
sealed interface Stmt {
    /**
     * The location data of important portion of this statement.
     */
    val context: Context

    /**
     * Delegates calls to the visitor's visit function to the method designated for this particular statement.
     *
     * @param X The type of values to be produced by this statement
     * @param visitor The visitor to visit
     */
    fun <X> accept(visitor: Visitor<X>): X

    /**
     * Represents an empty statement that does nothing.
     */
    data class Empty(override val context: Context) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitEmptyStmt(this)
    }

    /**
     * Represents a block statement with multiple statements.
     *
     * @property stmts The statements inside this block
     */
    data class Block(override val context: Context, val stmts: Stmts): Stmt{
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitBlockStmt(this)
    }

    /**
     * Represents a declaration statement that creates a variable with a given value.
     *
     * @property constant Whether the variable is constant or not
     * @property name The name of the variable
     * @property expr The value to assign to the variable
     */
    data class Declaration(override val context: Context, val constant: Boolean, val name: Expr.Name, val expr: Expr) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitDeclarationStmt(this)
    }

    /**
     * Represents a single expression statement.
     *
     * @property expr The expression contained by this statement
     */
    data class Expression(override val context: Context, val expr: Expr) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitExpressionStmt(this)
    }

    /**
     * An interface that facilitates walking the statement tree.
     *
     * @param X The type of values to be produced by this visitor
     */
    interface Visitor<X> {
        /**
         * The default visit method that delegates to all statements.
         *
         * @param stmt The statement to visit
         */
        fun visit(stmt: Stmt) =
            stmt.accept(this)

        /**
         * Visits an empty statement.
         *
         * @param stmt The statement to visit
         */
        fun visitEmptyStmt(stmt: Empty): X

        /**
         * Visits a block statement.
         *
         * @param stmt The statement to visit
         */
        fun visitBlockStmt(stmt: Block): X

        /**
         * Visits a declaration statement.
         *
         * @param stmt The statement to visit
         */
        fun visitDeclarationStmt(stmt: Declaration): X

        /**
         * Visits an expression statement.
         *
         * @param stmt The statement to visit
         */
        fun visitExpressionStmt(stmt: Expression): X
    }
}
