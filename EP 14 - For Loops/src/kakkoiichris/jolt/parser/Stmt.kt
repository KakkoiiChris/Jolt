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
     * Represents an empty statement that does nothing.
     */
    data class Declaration(override val context: Context, val constant: Boolean, val name: Expr.Name, val expr: Expr) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitDeclarationStmt(this)
    }

    /**
     * Represents an empty statement that does nothing.
     */
    data class Block(override val context: Context, val stmts: Stmts) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitBlockStmt(this)
    }

    /**
     * Represents an empty statement that does nothing.
     */
    data class If(override val context: Context, val condition: Expr, val body: Stmt, val `else`: Stmt?) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitIfStmt(this)
    }

    data class Loop(override val context: Context, val label: Expr.Name?, val body: Stmt) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitLoopStmt(this)
    }

    data class While(override val context: Context, val label: Expr.Name?, val condition: Expr, val body: Stmt) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitWhileStmt(this)
    }

    data class Do(override val context: Context, val label: Expr.Name?, val body: Stmt, val condition: Expr) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitDoStmt(this)
    }

    data class For(override val context: Context, val label: Expr.Name?, val pointer: Expr.Name, val iterable: Expr, val body: Stmt) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitForStmt(this)
    }

    data class Break(override val context: Context, val label: Expr.Name?) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitBreakStmt(this)
    }

    data class Continue(override val context: Context, val label: Expr.Name?) : Stmt {
        override fun <X> accept(visitor: Visitor<X>): X =
            visitor.visitContinueStmt(this)
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
         * Visits a declaration statement.
         *
         * @param stmt The statement to visit
         */
        fun visitDeclarationStmt(stmt: Declaration): X

        /**
         * Visits an if-else statement.
         *
         * @param stmt The statement to visit
         */
        fun visitBlockStmt(stmt: Block): X

        /**
         * Visits an if-else statement.
         *
         * @param stmt The statement to visit
         */
        fun visitIfStmt(stmt: If): X

        fun visitLoopStmt(stmt: Loop): X

        fun visitWhileStmt(stmt: While): X

        fun visitDoStmt(stmt: Do): X

        fun visitForStmt(stmt: For): X

        fun visitBreakStmt(stmt: Break): X

        fun visitContinueStmt(stmt: Continue): X

        /**
         * Visits an expression statement.
         *
         * @param stmt The statement to visit
         */
        fun visitExpressionStmt(stmt: Expression): X
    }
}