package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.parser.Expr
import kakkoiichris.jolt.parser.Program
import kakkoiichris.jolt.parser.Stmt

/**
 * A class that executes programs by implementing the visitors of both the Expr and Stmt class.
 */
class Runtime(private val source: Source) : Expr.Visitor<Double>, Stmt.Visitor<Unit> {
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
     * @param expr The expression to visit
     *
     * @return The value contained by this expression
     */
    override fun visitValueExpr(expr: Expr.Value) = expr.value

    /**
     *
     */
    override fun visitNestedExpr(expr: Expr.Nested) = visit(expr.expr)

    /**
     * @param expr The expression to visit
     *
     * @return The value contained by this expression
     */
    override fun visitUnaryExpr(expr: Expr.Unary) = when (expr.operator) {
        Expr.Unary.Operator.NEGATE -> -visit(expr.expr)
    }

    /**
     * @param expr The expression to visit
     *
     * @return The value contained by this expression
     */
    override fun visitBinaryExpr(expr: Expr.Binary) = when (expr.operator) {
        Expr.Binary.Operator.ADD       -> visit(expr.left) + visit(expr.right)

        Expr.Binary.Operator.SUBTRACT  -> visit(expr.left) - visit(expr.right)

        Expr.Binary.Operator.MULTIPLY  -> visit(expr.left) * visit(expr.right)

        Expr.Binary.Operator.DIVIDE    -> visit(expr.left) / visit(expr.right)

        Expr.Binary.Operator.REMAINDER -> visit(expr.left) % visit(expr.right)
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

        // TODO: Example Interpreter Error
        if (last == 42.0) {
            joltError("The meaning of life", source.getLine(stmt.expr.context.row), stmt.expr.context)
        }

        println(last)
    }
}