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

/**
 * A class that executes programs by implementing the visitors of both the Expr and Stmt class.
 */
class Runtime(private val source: Source) : Expr.Visitor<Double> {
    /**
     * Visits each of the program's statements in order.
     *
     * @param program The program to execute
     *
     * @return The value of the last expression
     */
    fun run(program: Program): Double {
        var last = Double.NaN

        for (expr in program) {
            last = visit(expr)

            if (last == 42.0) {
                joltError("The meaning of life", source, expr.context)
            }

            println(last)
        }

        return last
    }

    /**
     * @param expr The expression to visit
     *
     * @return The value contained by this expression
     */
    override fun visitValueExpr(expr: Expr.Value) = expr.value
}