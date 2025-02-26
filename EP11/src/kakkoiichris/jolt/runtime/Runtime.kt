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

import kakkoiichris.jolt.*
import kakkoiichris.jolt.lexer.Context
import kakkoiichris.jolt.parser.Expr
import kakkoiichris.jolt.parser.Program
import kakkoiichris.jolt.parser.Stmt

/**
 * A class that executes programs via implementations of the visitors of both the Expr and Stmt class.
 *
 * @param source The program source used for retrieving error contexts
 */
class Runtime(private val source: Source) : Stmt.Visitor<Unit>, Expr.Visitor<JoltValue<*>> {
    private val memory = Memory()

    private var last: JoltValue<*> = JoltNum(0.0)

    /**
     * Visits each of the program's statements in order.
     *
     * @param program The program to execute
     *
     * @return The value of the last expression
     */
    fun run(program: Program): JoltValue<*> {
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
     * Executes one statement if the condition is `true`, or another statement otherwise.
     *
     * @param stmt The statement to visit
     */
    override fun visitIfElseStmt(stmt: Stmt.IfElse) {
        val condition = visit(stmt.condition)

        if (condition !is JoltBool) {
            joltError("Condition must be of type 'bool'", source, stmt.condition.context)
        }

        if (condition.value) {
            visit(stmt.branchTrue)
        }
        else {
            visit(stmt.branchFalse)
        }
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
    override fun visitNoneExpr(expr: Expr.None) = JoltNum(Double.NaN)

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
    override fun visitNameExpr(expr: Expr.Name): JoltValue<*> {
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
        Expr.Unary.Operator.NEGATE -> when (val operand = visit(expr.operand)) {
            is JoltNum -> JoltNum(-operand.value)

            else       -> invalidUnaryOperand(operand, expr.operator, expr.operand.context)
        }

        Expr.Unary.Operator.NOT    -> when (val operand = visit(expr.operand)) {
            is JoltBool -> JoltBool(!operand.value)

            else        -> invalidUnaryOperand(operand, expr.operator, expr.operand.context)
        }
    }

    private fun invalidUnaryOperand(operand: JoltValue<*>, operator: Expr.Unary.Operator, context: Context): Nothing =
        joltError(
            "Operand of type '${operand.type}' invalid for unary '${operator.symbol}' operator",
            source,
            context
        )

    /**
     * @param expr The expression to visit
     *
     * @return The result of the operator on its operands
     */
    override fun visitBinaryExpr(expr: Expr.Binary) = when (expr.operator) {
        Expr.Binary.Operator.OR            -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltBool -> when (val operandRight = visit(expr.operandRight)) {
                is JoltBool -> JoltBool(operandLeft.value || operandRight.value)
                else        -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else        -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.XOR           -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltBool -> when (val operandRight = visit(expr.operandRight)) {
                is JoltBool -> JoltBool(operandLeft.value xor operandRight.value)
                else        -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else        -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.AND           -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltBool -> when (val operandRight = visit(expr.operandRight)) {
                is JoltBool -> JoltBool(operandLeft.value && operandRight.value)
                else        -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else        -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.EQUAL         -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltBool -> when (val operandRight = visit(expr.operandRight)) {
                is JoltBool -> JoltBool(operandLeft.value == operandRight.value)
                else        -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            is JoltNum  -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltBool(operandLeft.value == operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else        -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.NOT_EQUAL     -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltBool -> when (val operandRight = visit(expr.operandRight)) {
                is JoltBool -> JoltBool(operandLeft.value != operandRight.value)
                else        -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            is JoltNum  -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltBool(operandLeft.value != operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else        -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.LESS          -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltBool(operandLeft.value < operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.LESS_EQUAL    -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltBool(operandLeft.value <= operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.GREATER       -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltBool(operandLeft.value > operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.GREATER_EQUAL -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltBool(operandLeft.value >= operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.ADD           -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltNum(operandLeft.value + operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.SUBTRACT      -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltNum(operandLeft.value - operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.MULTIPLY      -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltNum(operandLeft.value * operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.DIVIDE        -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltNum(operandLeft.value / operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }

        Expr.Binary.Operator.REMAINDER     -> when (val operandLeft = visit(expr.operandLeft)) {
            is JoltNum -> when (val operandRight = visit(expr.operandRight)) {
                is JoltNum -> JoltNum(operandLeft.value % operandRight.value)
                else       -> invalidRightOperand(operandRight, expr.operator, expr.operandRight.context)
            }

            else       -> invalidLeftOperand(operandLeft, expr.operator, expr.operandLeft.context)
        }
    }

    private fun invalidLeftOperand(operand: JoltValue<*>, operator: Expr.Binary.Operator, context: Context): Nothing =
        joltError(
            "Left operand of type '${operand.type}' invalid for binary '${operator.symbol}' operator",
            source,
            context
        )

    private fun invalidRightOperand(operand: JoltValue<*>, operator: Expr.Binary.Operator, context: Context): Nothing =
        joltError(
            "Right operand of type '${operand.type}' invalid for binary '${operator.symbol}' operator",
            source,
            context
        )

    /**
     * Changes the value of the variable stored in memory.
     *
     * @return The value assigned
     *
     * @throws JoltError If the variable name is not present in memory, or if the variable is constant
     */
    override fun visitAssignExpr(expr: Expr.Assign): JoltValue<*> {
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