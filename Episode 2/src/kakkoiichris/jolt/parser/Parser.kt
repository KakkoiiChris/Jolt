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

import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.lexer.Lexer
import kakkoiichris.jolt.lexer.Token
import kakkoiichris.jolt.lexer.TokenType

class Parser(private val lexer: Lexer) {
    private var token = lexer.next()

    fun parse(): Program {
        val stmts = mutableListOf<Stmt>()

        while (!atEndOfFile()) {
            stmts += stmt()
        }

        return Program(stmts)
    }

    private fun here() =
        token.context

    private fun match(type: TokenType) =
        token.type == type

    private inline fun <reified T : TokenType> match() =
        T::class.isInstance(token.type)

    private fun step() {
        if (lexer.hasNext()) {
            token = lexer.next()
        }
    }

    private fun skip(type: TokenType) =
        if (match(type)) {
            step()

            true
        }
        else
            false

    private fun mustSkip(type: TokenType, errorMessage: String) {
        if (!skip(type)) {
            val context = here()

            joltError(errorMessage, lexer.source.getLine(context.row), context)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : TokenType> get(): Token<T> {
        val token = token

        step()

        return token as Token<T>
    }

    private fun atEndOfFile() =
        match(TokenType.EndOfFile)

    private fun stmt() = when {
        match(TokenType.Symbol.SEMICOLON) -> emptyStmt()

        else                              -> expressionStmt()
    }

    private fun emptyStmt(): Stmt.Empty {
        val context = here()

        mustSkip(TokenType.Symbol.SEMICOLON, "Expected a semicolon")

        return Stmt.Empty(context)
    }

    private fun expressionStmt(): Stmt.Expression {
        val expr = expr()

        val context = expr.context..here()

        mustSkip(TokenType.Symbol.SEMICOLON, "Expected a semicolon")

        return Stmt.Expression(context, expr)
    }

    private fun expr() =
        valueExpr()

    private fun valueExpr(): Expr.Value {
        val token = get<TokenType.Value>()

        return Expr.Value(token.context, token.type.value)
    }
}