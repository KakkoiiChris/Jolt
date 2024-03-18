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

import kakkoiichris.jolt.JoltError
import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.lexer.Lexer
import kakkoiichris.jolt.lexer.Token
import kakkoiichris.jolt.lexer.TokenType

/**
 * A class that converts tokens into [statements][Stmt], all at once via the [Program] class.
 *
 * @property lexer The lexer to generate tokens from
 */
class Parser(private val source: Source, private val lexer: Lexer) {
    /**
     * The current lexed token.
     */
    private var token = lexer.next()

    /**
     * Parses all statements and puts them into a program instance.
     *
     * @return A new [Program] instance
     */
    fun parse(): Program {
        val stmts = mutableListOf<Stmt>()

        while (!atEndOfFile()) {
            stmts += stmt()
        }

        return Program(stmts)
    }

    /**
     * Parses a single expression.
     *
     * @return A single expression
     */
    fun parseExpr() =
        expr()

    /**
     * Resets this parser's lexer, and starts the parser over from the beginning.
     */
    fun reset() {
        lexer.reset()

        token = lexer.next()
    }

    /**
     * @return The context of the current [token]
     */
    private fun here() =
        token.context

    /**
     * @param type The token type to match
     *
     * @return `true` if the token type was matched, or `false` otherwise
     */
    private fun match(type: TokenType) =
        token.type == type

    /**
     * @param types The token types to match
     *
     * @return `true` if any of the token types were matched, or `false` otherwise
     */
    private fun matchAny(vararg types: TokenType) =
        types.any { match(it) }

    /**
     * @param T The token type to match
     *
     * @return `true` if the token type was matched, or `false` otherwise
     */
    private inline fun <reified T : TokenType> match() =
        T::class.isInstance(token.type)

    /**
     * Gets the next token if it is available.
     */
    private fun step() {
        if (lexer.hasNext()) {
            token = lexer.next()
        }
    }

    /**
     * @param type The token type to skip
     *
     * @return `true` if the token type was matched, or `false` otherwise
     */
    private fun skip(type: TokenType) =
        if (match(type)) {
            step()

            true
        }
        else
            false

    /**
     * Produces an [error][JoltError] with the given error message if the given token is not skipped.
     *
     * @param type The token typ to skip
     * @param errorMessage The message for the error
     *
     * @throws JoltError If the token is not skipped
     */
    private fun mustSkip(type: TokenType, errorMessage: String = "PARSER_CORRUPTED") {
        if (!skip(type)) {
            val context = here()

            joltError(errorMessage, source.getLine(context.row), context)
        }
    }

    /**
     * @param T The token type to convert
     *
     * @return A token with the given token type
     */
    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : TokenType> get(): Token<T> {
        val token = token

        step()

        return token as Token<T>
    }

    /**
     * @return `true` if the current token's type is [EndOfFile][TokenType.EndOfFile], or false otherwise
     */
    private fun atEndOfFile() =
        match(TokenType.EndOfFile)

    /**
     * @return A single statement
     */
    private fun stmt() = when {
        match(TokenType.Symbol.SEMICOLON)                      -> emptyStmt()

        matchAny(TokenType.Keyword.LET, TokenType.Keyword.VAR) -> declarationStmt()

        else                                                   -> expressionStmt()
    }

    /**
     * @return A single empty statement
     */
    private fun emptyStmt(): Stmt.Empty {
        val context = here()

        mustSkip(TokenType.Symbol.SEMICOLON)

        return Stmt.Empty(context)
    }

    /**
     * @return A single empty statement
     */
    private fun declarationStmt(): Stmt.Declaration {
        val context = here()

        val constant = skip(TokenType.Keyword.LET)

        if (!constant) {
            mustSkip(TokenType.Keyword.VAR)
        }

        val name = nameExpr()

        var expr: Expr = Double.NaN.toValue(here())

        if (skip(TokenType.Symbol.EQUAL)) {
            expr = expr()
        }

        mustSkip(TokenType.Symbol.SEMICOLON, "Expected a semicolon")

        return Stmt.Declaration(context, constant, name, expr)
    }

    /**
     * @return A single expression statement
     */
    private fun expressionStmt(): Stmt.Expression {
        val expr = expr()

        val context = expr.context..here()

        mustSkip(TokenType.Symbol.SEMICOLON, "Expected a semicolon")

        return Stmt.Expression(context, expr)
    }

    /**
     * @return A single expression
     */
    private fun expr() =
        assignExpr()

    /**
     * @return A single assignment expression if an '=' is present
     */
    private fun assignExpr(): Expr {
        val expr = additiveExpr()

        if (match(TokenType.Symbol.EQUAL)) {
            if (expr !is Expr.Name) joltError("Value '$expr' is not assignable", source.getLine(expr.context.row), expr.context)

            mustSkip(TokenType.Symbol.EQUAL)

            val value = additiveExpr()

            val context = expr.context..here()

            return Expr.Assign(context, expr, value)
        }

        return expr
    }

    /**
     * @return A single additive binary expression if a '+' or '-' is present
     */
    private fun additiveExpr(): Expr {
        var expr = multiplicativeExpr()

        while (matchAny(TokenType.Symbol.PLUS, TokenType.Symbol.DASH)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = multiplicativeExpr()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single multiplicative binary expression if a '*', '/',  or '%' is present
     */
    private fun multiplicativeExpr(): Expr {
        var expr = prefixExpr()

        while (matchAny(TokenType.Symbol.STAR, TokenType.Symbol.SLASH, TokenType.Symbol.PERCENT)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = prefixExpr()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single prefix unary expression if a '-' is present
     */
    private fun prefixExpr(): Expr {
        if (match(TokenType.Symbol.DASH)) {
            val (start, type) = get<TokenType.Symbol>()

            val operator = Expr.Unary.Operator[type]

            val expr = prefixExpr()

            val context = start..here()

            return Expr.Unary(context, operator, expr)
        }

        return terminalExpr()
    }

    /**
     * @return A single terminal expression
     */
    private fun terminalExpr() = when {
        match<TokenType.Value<*>>()           -> valueExpr()

        match<TokenType.Name>()            -> nameExpr()

        match(TokenType.Symbol.LEFT_PAREN) -> nestedExpr()

        else                               -> joltError("Invalid terminal starting with '${token.type}'", source.getLine(here().row), here())
    }

    /**
     * @return A single value expression
     */
    private fun valueExpr(): Expr.Value {
        val (context, type) = get<TokenType.Value<*>>()

        return Expr.Value(context, type.value)
    }

    /**
     * @return A single name expression
     */
    private fun nameExpr(): Expr.Name {
        val (context, type) = get<TokenType.Name>()

        return Expr.Name(context, type.value)
    }

    /**
     * @return A single nested expression
     */
    private fun nestedExpr(): Expr.Nested {
        val start = here()

        mustSkip(TokenType.Symbol.LEFT_PAREN, "BROKEN LEFT PAREN")

        val expr = expr()

        mustSkip(TokenType.Symbol.RIGHT_PAREN, "Expected a right parenthesis")

        val context = start..here()

        return Expr.Nested(context, expr)
    }
}