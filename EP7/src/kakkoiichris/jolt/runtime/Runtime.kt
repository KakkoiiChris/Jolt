/*      ___  _______  ___    _______
 *     |   ||       ||   |  |       |
 *     |   ||   _   ||   |  |_     _|
 *     |   ||  | |  ||   |    |   |
 *  ___|   ||  |_|  ||   |___ |   |
 * |       ||       ||       ||   |
 * |_______||_______||_______||___|
 *         SCRIPTING LANGUAGE
 */
package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.JoltError
import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.parser.Expr
import kakkoiichris.jolt.parser.Program
import kakkoiichris.jolt.parser.Stmt

/**
 * A class that executes programs via implementations of the visitors of both the Expr and Stmt class.
 *
 * @param source The program source used for retrieving error contexts
 */
class Runtime(private val source: Source) : Stmt.Visitor<Unit>, Expr.Visitor<Double> {
    private val memory = Memory()

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
     * Visits all the inner statements with a new memory scope.
     *
     * @param stmt The statement to visit
     */
    override fun visitBlockStmt(stmt: Stmt.Block) {
        try {
            memory.push()

            for (subStmt in stmt.stmts) {
                visit(subStmt)
            }
        }
        finally {
            memory.pop()
        }
    }

    /**
     * Registers a new variable in memory with the given constancy and value.
     *
     * @param stmt The statement to visit
     */
    override fun visitDeclarationStmt(stmt: Stmt.Declaration) {
        val (context, isConstant, name, expr) = stmt

        if (memory.isDeclared(name)) {
            joltError("Variable name '${name.value}' is already declared", source, context)
        }

        val value = visit(expr)

        memory.declare(isConstant, name, value)
    }

    /**
     * Stores the value of the contained expression, and prints it to the screen.
     *
     * @param stmt The statement to visit
     */
    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        last = visit(stmt.expr)

        println(last)
    }

    /**
     * @param expr The expression to visit
     *
     * @return [Double.NaN]
     */
    override fun visitEmptyExpr(expr: Expr.Empty) = Double.NaN

    /**
     * @param expr The expression to visit
     *
     * @return The value contained by this expression
     */
    override fun visitValueExpr(expr: Expr.Value) = expr.value

    /**
     * @return The value of the variable stored in memory
     *
     * @throws JoltError If the variable name is not present in memory
     */
    override fun visitNameExpr(expr: Expr.Name): Double {
        val reference = memory[expr.value] ?: joltError("Name '${expr.value}' is not declared", source, expr.context)

        return reference.value
    }

    /**
     * @param expr The expression to visit
     *
     * @return The result of the inner expression
     */
    override fun visitNestedExpr(expr: Expr.Nested) = visit(expr.expr)

    /**
     * @param expr The expression to visit
     *
     * @return The result of the operator on its operand
     */
    override fun visitUnaryExpr(expr: Expr.Unary) = when (expr.operator) {
        Expr.Unary.Operator.NEGATE -> -visit(expr.operand)
    }

    /**
     * @param expr The expression to visit
     *
     * @return The result of the operator on its operands
     */
    override fun visitBinaryExpr(expr: Expr.Binary) = when (expr.operator) {
        Expr.Binary.Operator.ADD       -> visit(expr.operandLeft) + visit(expr.operandRight)

        Expr.Binary.Operator.SUBTRACT  -> visit(expr.operandLeft) - visit(expr.operandRight)

        Expr.Binary.Operator.MULTIPLY  -> visit(expr.operandLeft) * visit(expr.operandRight)

        Expr.Binary.Operator.DIVIDE    -> visit(expr.operandLeft) / visit(expr.operandRight)

        Expr.Binary.Operator.REMAINDER -> visit(expr.operandLeft) % visit(expr.operandRight)
    }

    /**
     * Changes the value of the variable stored in memory.
     *
     * @return The value assigned
     *
     * @throws JoltError If the variable name is not present in memory, or if the variable is constant
     */
    override fun visitAssignExpr(expr: Expr.Assign): Double {
        val reference = memory[expr.name.value]
            ?: joltError("Name '${expr.name.value}' is not declared", source, expr.name.context)

        if (reference.isConstant) {
            joltError("Name '${expr.name.value}' is constant and cannot be reassigned", source, expr.name.context)
        }

        val value = visit(expr.value)

        reference.value = value

        return value
    }
}