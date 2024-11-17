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
 * A class that executes programs by implementing the visitors of both the Expr and Stmt class.
 */
class Runtime(private val source: Source) : Stmt.Visitor<Unit>, Expr.Visitor<Double> {
    private val memory = Memory()

    private val last = Memory.Record(false, 0.0)

    init {
        memory["\$last"] = last
    }

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

        return last.value
    }

    /**
     * Does nothing.
     *
     * @param stmt The statement to visit
     */
    override fun visitEmptyStmt(stmt: Stmt.Empty) = Unit

    override fun visitDefaultDeclarationStmt(stmt: Stmt.DefaultDeclaration) {

    }

    /**
     * Registers a new variable in memory with the given value
     */
    override fun visitDeclarationStmt(stmt: Stmt.Declaration) {
        val (_, constant, name, type, expr) = stmt

        val value = visit(expr)

        memory[name.value] = value
    }

    /**
     * Stores the value of the contained expression, and prints it to the screen.
     *
     * @param stmt The statement to visit
     */
    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        val last = visit(stmt.expr)

        this.last.value = last

        println(last)
    }

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
    override fun visitNameExpr(expr: Expr.Name) =
        memory[expr.value].value

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
        val record = memory[expr.name.value]

        if (record.constant) {
            joltError("Cannot reassign constant '${expr.name.value}'!", source, expr.context)
        }

        val value = visit(expr.value)

        record.value = value

        return value
    }
}