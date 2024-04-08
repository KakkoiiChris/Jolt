/********************************************
 * ::::::::::: ::::::::  :::    ::::::::::: *
 *     :+:    :+:    :+: :+:        :+:     *
 *     +:+    +:+    +:+ +:+        +:+     *
 *     +#+    +#+    +:+ +#+        +#+     *
 *     +#+    +#+    +#+ +#+        +#+     *
 * #+# #+#    #+#    #+# #+#        #+#     *
 *            Scripting Language            *
 ********************************************/
package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.parser.Expr
import kakkoiichris.jolt.parser.Program
import kakkoiichris.jolt.parser.Stmt

/**
 * A class that executes programs by implementing the visitors of both the Expr and Stmt class.
 */
class Runtime(private val source: Source) : Stmt.Visitor<Unit>, Expr.Visitor<Double> {
    /**
     * The value of the last expression.
     */
    private var last = 0.0

    /**
     * Visits each of the program's statements in order.
     *
     * @param program The program to execute
     *
     * @return The value of the last expression
     */
    fun run(program: Program): Double {
        for (stmt in program) {
            visit(stmt)
        }

        return last
    }

    /**
     * Does nothing.
     *
     * @param stmt The statement to visit
     */
    override fun visitEmptyStmt(stmt: Stmt.Empty) = Unit

    /**
     * Stores the value of the contained expression, and prints it to the screen.
     *
     * @param stmt The statement to visit
     */
    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        last = visit(stmt.expr)

        if (last == 42.0) {
            joltError("The meaning of life", source, stmt.expr.context)
        }

        println(last)
    }

    /**
     * @param expr The expression to visit
     *
     * @return The value contained by this expression
     */
    override fun visitValueExpr(expr: Expr.Value) = expr.value
}