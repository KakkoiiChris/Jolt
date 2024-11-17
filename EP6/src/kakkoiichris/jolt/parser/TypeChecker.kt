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

import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError

object TypeChecker : Stmt.Visitor<Unit>, Expr.Visitor<DataType> {
    private val variableTypes = mutableMapOf<String, DataType>()

    private lateinit var source: Source

    fun check(source: Source, program: Program) {
        this.source = source

        for (stmt in program) {
            visit(stmt)
        }
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty) = Unit

    override fun visitDeclarationStmt(stmt: Stmt.Declaration) {
        val expectedType = stmt.type?.value
        val assignedType = stmt.expr?.let { visit(it) }

        if (expectedType != assignedType) {
            joltError(
                "Value of type '$assignedType' cannot be assigned to variable of type '$expectedType'",
                source, stmt.expr.context
            )
        }

        variableTypes[stmt.name.value] = assignedType!!
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        visit(stmt.expr)
    }

    override fun visitValueExpr(expr: Expr.Value) =
        Primitive.NUM

    override fun visitNameExpr(expr: Expr.Name) =
        variableTypes[expr.value] ?: joltError(
            "Variable '$${expr.value}' is undeclared",
            source, expr.context
        )

    override fun visitNestedExpr(expr: Expr.Nested) =
        visit(expr.expr)

    override fun visitUnaryExpr(expr: Expr.Unary) = when (expr.operator) {
        Expr.Unary.Operator.NEGATE -> Primitive.NUM
    }

    override fun visitBinaryExpr(expr: Expr.Binary) = when (expr.operator) {
        Expr.Binary.Operator.ADD       -> Primitive.NUM
        Expr.Binary.Operator.SUBTRACT  -> Primitive.NUM
        Expr.Binary.Operator.MULTIPLY  -> Primitive.NUM
        Expr.Binary.Operator.DIVIDE    -> Primitive.NUM
        Expr.Binary.Operator.REMAINDER -> Primitive.NUM
    }

    override fun visitAssignExpr(expr: Expr.Assign): DataType {
        val expectedType = visit(expr.name)
        val assignedType = visit(expr.value)

        if (expectedType != assignedType) {
            joltError(
                "Value of type '$assignedType' cannot be assigned to variable of type '$expectedType'",
                source, expr.value.context
            )
        }

        return assignedType
    }
}
