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
package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.*
import kakkoiichris.jolt.lexer.Context
import kakkoiichris.jolt.parser.Expr
import kakkoiichris.jolt.parser.Program
import kakkoiichris.jolt.parser.Stmt

/**
 * A class that executes programs by implementing the visitors of both the Expr and Stmt class.
 */
class Runtime(private val source: Source, private val memory: Memory = Memory()) : Expr.Visitor<JoltValue<*>>, Stmt.Visitor<Unit> {
    init {
        memory["\$last"] = Memory.Record(false, JoltValue.Number(0.0))
    }

    /**
     * Visits each of the program's statements in order.
     *
     * @param program The program to execute
     *
     * @return The value of the last expression
     */
    fun run(program: Program): JoltValue<*> {
        try {
            for (stmt in program) {
                visit(stmt)
            }
        }
        catch (r: Break) {
            joltError("Break statement was unhandled", source.getLine(r.origin.row), r.origin)
        }
        catch (r: Continue) {
            joltError("Continue statement was unhandled", source.getLine(r.origin.row), r.origin)
        }

        return JoltValue.Number(0.0)
    }

    /**
     * Visits each of the program's statements in order.
     *
     * @param expr The expression to evaluate
     *
     * @return The value of the last expression
     */
    fun runExpr(expr: Expr): JoltValue<*> {
        val value = visit(expr)

        memory["_"] = Memory.Record(true, value)

        return value
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
        memory[expr.value]?.value
            ?: joltError("Variable '${expr.value}' is undeclared", source.getLine(expr.context.row), expr.context)

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
    override fun visitUnaryExpr(expr: Expr.Unary): JoltValue<*> {
        val (_, operator, operand) = expr

        return when (operator) {
            Expr.Unary.Operator.NEGATE -> negateOperator(operand)

            Expr.Unary.Operator.NOT    -> notOperator(operand)

            Expr.Unary.Operator.SIZE   -> sizeOperator(operand)
        }
    }

    private fun invalidOperand(operand: JoltValue<*>, operator: Expr.Unary.Operator, context: Context): Nothing =
        joltError("Operand '$operand' is invalid for unary '$operator' operator", source.getLine(context.row), context)

    /**
     *
     */
    private fun negateOperator(operand: Expr) = when (val o = visit(operand)) {
        is JoltValue.Number -> JoltValue.Number(-o.value)

        is JoltValue.String -> JoltValue.String(o.value.reversed())

        else                -> invalidOperand(o, Expr.Unary.Operator.NEGATE, operand.context)
    }

    /**
     *
     */
    private fun notOperator(operand: Expr) = when (val o = visit(operand)) {
        is JoltValue.Boolean -> JoltValue.Boolean(!o.value)

        else                 -> invalidOperand(o, Expr.Unary.Operator.NOT, operand.context)
    }

    /**
     *
     */
    private fun sizeOperator(operand: Expr) = when (val o = visit(operand)) {
        is JoltValue.String -> JoltValue.Number(o.value.length.toDouble())

        else                -> JoltValue.Number(1.0)
    }

    /**
     * @param expr The expression to visit
     *
     * @return The result of the operator on its operands
     */
    override fun visitBinaryExpr(expr: Expr.Binary): JoltValue<*> {
        val (_, operator, left, right) = expr

        return when (operator) {
            Expr.Binary.Operator.OR            -> orOperator(left, right)

            Expr.Binary.Operator.AND           -> andOperator(left, right)

            Expr.Binary.Operator.EQUAL         -> equalOperator(left, right)

            Expr.Binary.Operator.NOT_EQUAL     -> notEqualOperator(left, right)

            Expr.Binary.Operator.LESS          -> lessOperator(left, right)

            Expr.Binary.Operator.LESS_EQUAL    -> lessEqualOperator(left, right)

            Expr.Binary.Operator.GREATER       -> greaterOperator(left, right)

            Expr.Binary.Operator.GREATER_EQUAL -> greaterEqualOperator(left, right)

            Expr.Binary.Operator.ADD           -> addOperator(left, right)

            Expr.Binary.Operator.SUBTRACT      -> subtractOperator(left, right)

            Expr.Binary.Operator.MULTIPLY      -> multiplyOperator(left, right)

            Expr.Binary.Operator.DIVIDE        -> divideOperator(left, right)

            Expr.Binary.Operator.REMAINDER     -> remainderOperator(left, right)
        }
    }

    private fun invalidLeftOperand(operand: JoltValue<*>, operator: Expr.Binary.Operator, context: Context): Nothing =
        joltError("Left operand '$operand' is invalid for binary '$operator' operator", source.getLine(context.row), context)

    private fun invalidRightOperand(operand: JoltValue<*>, operator: Expr.Binary.Operator, context: Context): Nothing =
        joltError("Right operand '$operand' is invalid for binary '$operator' operator", source.getLine(context.row), context)

    /**
     *
     */
    private fun orOperator(left: Expr, right: Expr): JoltValue<Boolean> {
        val l = visit(left)

        l as? JoltValue.Boolean ?: invalidLeftOperand(l, Expr.Binary.Operator.OR, left.context)

        if (l.value) {
            return JoltValue.Boolean(true)
        }

        val r = visit(right)

        r as? JoltValue.Boolean ?: invalidRightOperand(r, Expr.Binary.Operator.OR, right.context)

        return r
    }

    /**
     *
     */
    private fun andOperator(left: Expr, right: Expr): JoltValue<Boolean> {
        val l = visit(left)

        l as? JoltValue.Boolean ?: invalidLeftOperand(l, Expr.Binary.Operator.AND, left.context)

        if (!l.value) {
            return JoltValue.Boolean(true)
        }

        val r = visit(right)

        r as? JoltValue.Boolean ?: invalidRightOperand(r, Expr.Binary.Operator.AND, right.context)

        return r
    }

    /**
     *
     */
    private fun equalOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Boolean -> when (val r = visit(right)) {
            is JoltValue.Boolean -> JoltValue.Boolean(l.value == r.value)

            else                 -> JoltValue.Boolean(false)
        }

        is JoltValue.Number  -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Boolean(l.value == r.value)

            else                -> JoltValue.Boolean(false)
        }

        is JoltValue.String  -> when (val r = visit(right)) {
            is JoltValue.String -> JoltValue.Boolean(l.value == r.value)

            else                -> JoltValue.Boolean(false)
        }

        is JoltValue.List    -> when (val r = visit(right)) {
            is JoltValue.List -> JoltValue.Boolean(l.value == r.value)

            else              -> JoltValue.Boolean(false)
        }
    }

    /**
     *
     */
    private fun notEqualOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Boolean -> when (val r = visit(right)) {
            is JoltValue.Boolean -> JoltValue.Boolean(l.value != r.value)

            else                 -> JoltValue.Boolean(true)
        }

        is JoltValue.Number  -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Boolean(l.value != r.value)

            else                -> JoltValue.Boolean(true)
        }

        is JoltValue.String  -> when (val r = visit(right)) {
            is JoltValue.String -> JoltValue.Boolean(l.value != r.value)

            else                -> JoltValue.Boolean(true)
        }

        is JoltValue.List    -> when (val r = visit(right)) {
            is JoltValue.List -> JoltValue.Boolean(l.value != r.value)

            else              -> JoltValue.Boolean(false)
        }
    }

    /**
     *
     */
    private fun lessOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Boolean(l.value < r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.LESS, right.context)
        }

        is JoltValue.String -> when (val r = visit(right)) {
            is JoltValue.String -> JoltValue.Boolean(l.value < r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.LESS, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.LESS, left.context)
    }

    /**
     *
     */
    private fun lessEqualOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Boolean(l.value <= r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.LESS_EQUAL, right.context)
        }

        is JoltValue.String -> when (val r = visit(right)) {
            is JoltValue.String -> JoltValue.Boolean(l.value <= r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.LESS_EQUAL, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.LESS_EQUAL, left.context)
    }

    /**
     *
     */
    private fun greaterOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Boolean(l.value > r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.GREATER, right.context)
        }

        is JoltValue.String -> when (val r = visit(right)) {
            is JoltValue.String -> JoltValue.Boolean(l.value > r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.GREATER, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.GREATER, left.context)
    }

    /**
     *
     */
    private fun greaterEqualOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Boolean(l.value >= r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.GREATER_EQUAL, right.context)
        }

        is JoltValue.String -> when (val r = visit(right)) {
            is JoltValue.String -> JoltValue.Boolean(l.value >= r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.GREATER_EQUAL, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.GREATER_EQUAL, left.context)
    }

    /**
     *
     */
    private fun addOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Boolean -> when (val r = visit(right)) {
            is JoltValue.String -> JoltValue.String(l.value.toString() + r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.ADD, right.context)
        }

        is JoltValue.Number  -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Number(l.value + r.value)

            is JoltValue.String -> JoltValue.String(l.value.toString() + r)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.ADD, right.context)
        }

        is JoltValue.String  -> {
            val r = visit(right)

            JoltValue.String(l.value + r.toString())
        }

        is JoltValue.List    -> {
            val r = visit(right)

            JoltValue.List((l.value + r).toMutableList())
        }
    }

    /**
     *
     */
    private fun subtractOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Number(l.value - r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.SUBTRACT, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.SUBTRACT, left.context)
    }

    /**
     *
     */
    private fun multiplyOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Number(l.value * r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.MULTIPLY, right.context)
        }

        is JoltValue.String -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.String(l.value.repeat(r.value.toInt()))

            else                -> invalidRightOperand(r, Expr.Binary.Operator.MULTIPLY, right.context)
        }

        is JoltValue.List   -> when (val r = visit(right)) {
            is JoltValue.Number -> {
                val list = buildList {
                    repeat(r.value.toInt()) {
                        addAll(l.value)
                    }
                }.toMutableList()

                JoltValue.List(list)
            }

            else                -> invalidRightOperand(r, Expr.Binary.Operator.MULTIPLY, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.MULTIPLY, left.context)
    }

    /**
     *
     */
    private fun divideOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Number(l.value / r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.DIVIDE, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.DIVIDE, left.context)
    }

    /**
     *
     */
    private fun remainderOperator(left: Expr, right: Expr) = when (val l = visit(left)) {
        is JoltValue.Number -> when (val r = visit(right)) {
            is JoltValue.Number -> JoltValue.Number(l.value % r.value)

            else                -> invalidRightOperand(r, Expr.Binary.Operator.REMAINDER, right.context)
        }

        else                -> invalidLeftOperand(l, Expr.Binary.Operator.REMAINDER, left.context)
    }

    /**
     * Changes the value of the variable stored in memory.
     *
     * @return The value assigned
     *
     * @throws JoltError If the variable name is not present in memory, or if the variable is constant
     */
    override fun visitAssignExpr(expr: Expr.Assign): JoltValue<*> {
        val (_, name, value) = expr

        val record = memory[name.value]
            ?: joltError("Variable '${name.value}' is undeclared", source.getLine(name.context.row), name.context)

        if (record.constant) joltError("Cannot assign to constant '${name.value}'", source.getLine(name.context.row), name.context)

        val v = visit(value)

        record.value = v

        return v
    }

    override fun visitInterpolationExpr(expr: Expr.Interpolation): JoltValue.String {
        val result = buildString {
            for (e in expr.exprs) {
                append(visit(e))
            }
        }

        return JoltValue.String(result)
    }

    private fun checkIndexOutOfBounds(index: Int, type: String, size: Int, context: Context) {
        if (index !in 0..<size) {
            joltError("Index '$index' out of bounds for '$type' of length '$size'", source.getLine(context.row), context)
        }
    }

    override fun visitGetIndexExpr(expr: Expr.GetIndex): JoltValue<*> {
        val (_, target, index) = expr

        return when (val t = visit(target)) {
            is JoltValue.String -> when (val i = visit(index)) {
                is JoltValue.Number -> {
                    checkIndexOutOfBounds(i.value.toInt(), "string", t.value.length, index.context)

                    JoltValue.String(t.value[i.value.toInt()].toString())
                }

                else                -> joltError("String index must be a number", source.getLine(index.context.row), index.context)
            }

            is JoltValue.List   -> when (val i = visit(index)) {
                is JoltValue.Number -> {
                    checkIndexOutOfBounds(i.value.toInt(), "list", t.value.size, index.context)

                    t.value[i.value.toInt()]
                }

                else                -> joltError("String index must be a number", source.getLine(index.context.row), index.context)
            }

            else                -> joltError("Value '$t' cannot be indexed", source.getLine(index.context.row), index.context)
        }
    }

    override fun visitSetIndexExpr(expr: Expr.SetIndex): JoltValue<*> {
        val (_, target, index, value) = expr

        return when (val t = visit(target)) {
            is JoltValue.List -> when (val i = visit(index)) {
                is JoltValue.Number -> {
                    checkIndexOutOfBounds(i.value.toInt(), "list", t.value.size, index.context)

                    val v = visit(value)

                    t.value[i.value.toInt()] = v

                    v
                }

                else                -> joltError("String index must be a number", source.getLine(index.context.row), index.context)
            }

            else              -> joltError("Value '$t' cannot be indexed", source.getLine(index.context.row), index.context)
        }
    }

    override fun visitListLiteralExpr(expr: Expr.ListLiteral): JoltValue<*> {
        val (_, elements) = expr

        val e = elements.map { visit(it) }.toMutableList()

        return JoltValue.List(e)
    }

    /**
     * Does nothing.
     *
     * @param stmt The statement to visit
     */
    override fun visitEmptyStmt(stmt: Stmt.Empty) = Unit

    /**
     * Registers a new variable in memory with the given value
     */
    override fun visitDeclarationStmt(stmt: Stmt.Declaration) {
        val (_, constant, name, expr) = stmt

        val record = memory.new(name.value, constant)
            ?: joltError("Variable '${name.value}' is already declared", source.getLine(name.context.row), name.context)

        val value = visit(expr)

        record.value = value
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        val (_, stmts) = stmt

        try {
            memory.push()

            for (subStmt in stmts) {
                visit(subStmt)
            }
        }
        finally {
            memory.pop()
        }
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        val (_, condition, body, `else`) = stmt

        val c = visit(condition)

        if (c !is JoltValue.Boolean) {
            joltError("If statement condition must result in a boolean", source.getLine(condition.context.row), condition.context)
        }

        if (c.value) {
            visit(body)
        }
        else if (`else` != null) {
            visit(`else`)
        }
    }

    override fun visitLoopStmt(stmt: Stmt.Loop) {
        val (_, label, body) = stmt

        while (true) {
            try {
                visit(body)
            }
            catch (r: Break) {
                if (label?.value == r.label) {
                    break
                }

                throw r
            }
            catch (r: Continue) {
                if (label?.value == r.label) {
                    continue
                }

                throw r
            }
        }
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        val (_, label, condition, body) = stmt

        while (true) {
            try {
                val c = visit(condition)

                if (c !is JoltValue.Boolean) {
                    joltError("While statement condition must result in a boolean", source.getLine(condition.context.row), condition.context)
                }

                if (!c.value) break

                visit(body)
            }
            catch (r: Break) {
                if (label?.value == r.label) {
                    break
                }

                throw r
            }
            catch (r: Continue) {
                if (label?.value == r.label) {
                    continue
                }

                throw r
            }
        }
    }

    override fun visitDoStmt(stmt: Stmt.Do) {
        val (_, label, body, condition) = stmt

        while (true) {
            try {
                visit(body)

                val c = visit(condition)

                if (c !is JoltValue.Boolean) {
                    joltError("Do statement condition must result in a boolean", source.getLine(condition.context.row), condition.context)
                }

                if (!c.value) break
            }
            catch (r: Break) {
                if (label?.value == r.label) {
                    break
                }

                throw r
            }
            catch (r: Continue) {
                if (label?.value == r.label) {
                    continue
                }

                throw r
            }
        }
    }

    override fun visitForStmt(stmt: Stmt.For) {
        val (_, label, pointer, iterable, body) = stmt

        try {
            memory.push()

            memory.new(pointer.value, true)

            val i = visit(iterable)

            val elements = i.iterable
                ?: joltError("Value '$i' cannot be iterated over", source.getLine(iterable.context.row), iterable.context)

            for (element in elements) {
                try {
                    memory[pointer.value]!!.value = element

                    visit(body)
                }
                catch (r: Break) {
                    if (label?.value == r.label) {
                        break
                    }

                    throw r
                }
                catch (r: Continue) {
                    if (label?.value == r.label) {
                        continue
                    }

                    throw r
                }
            }
        }
        finally {
            memory.pop()
        }
    }

    override fun visitBreakStmt(stmt: Stmt.Break) {
        val (context, label) = stmt

        throw Break(context, label?.value ?: "")
    }

    override fun visitContinueStmt(stmt: Stmt.Continue) {
        val (context, label) = stmt

        throw Continue(context, label?.value ?: "")
    }

    /**
     * Stores the value of the contained expression, and prints it to the screen.
     *
     * @param stmt The statement to visit
     */
    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        val value = visit(stmt.expr)

        println("$JOLT <$value>")
    }
}