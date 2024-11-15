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

import kakkoiichris.jolt.JoltError
import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.lexer.Context
import kakkoiichris.jolt.lexer.Lexer
import kakkoiichris.jolt.lexer.Token
import kakkoiichris.jolt.lexer.TokenType

/**
 * A class that converts tokens into [expressions][Expr], all at once via the [Program] class.
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
    private fun mustSkip(type: TokenType, errorMessage: String = "BROKEN") {
        if (!skip(type)) {
            val context = here()

            joltError(errorMessage, source, context)
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

        mustSkip(token.type)

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
        val start = here()

        val constant = skip(TokenType.Keyword.LET)

        if (!constant) {
            mustSkip(TokenType.Keyword.VAR)
        }

        val name = name()

        val typed = skip(TokenType.Symbol.COLON)

        var type = if (typed) type() else null

        val assigned = skip(TokenType.Symbol.EQUAL)

        val expr = if (assigned) expr() else null

        if (assigned && type == null) {
            type = Type(Context.none, expr!!.type)
        }

        val context = start..here()

        return Stmt.Declaration(context, constant, name, type!!, expr!!)
    }

    private fun type(): Type {
        val start = here()

        var dataType = when {
            skip(TokenType.Keyword.NUM) -> Primitive.NUM

            else                        -> TODO("BASE DATA TYPE")
        }

        val context = start..here()

        return Type(context, dataType)
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
        assign()

    private fun assign(): Expr {
        if (match<TokenType.Name>()) {
            val start = here()

            val name = name()

            if (!skip(TokenType.Symbol.EQUAL)) {
                lexer.undo()

                return additive()
            }

            val value = additive()

            val context = start..here()

            return Expr.Assign(context, name, value)
        }

        return additive()
    }

    /**
     * @return A single additive binary expression if a '+' or '-' is present
     */
    private fun additive(): Expr {
        var expr = multiplicative()

        while (matchAny(TokenType.Symbol.PLUS, TokenType.Symbol.DASH)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = multiplicative()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single multiplicative binary expression if a '*', '/',  or '%' is present
     */
    private fun multiplicative(): Expr {
        var expr = prefix()

        while (matchAny(TokenType.Symbol.STAR, TokenType.Symbol.SLASH, TokenType.Symbol.PERCENT)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = prefix()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single prefix unary expression if a '-' is present
     */
    private fun prefix(): Expr {
        if (match(TokenType.Symbol.DASH)) {
            val (start, type) = get<TokenType.Symbol>()

            val operator = Expr.Unary.Operator[type]

            val expr = prefix()

            val context = start..here()

            return Expr.Unary(context, operator, expr)
        }

        return terminal()
    }

    /**
     * @return A single terminal expression
     */
    private fun terminal() = when {
        match<TokenType.Value>()           -> value()

        match<TokenType.Name>()            -> name()

        match(TokenType.Symbol.LEFT_PAREN) -> nested()

        else                               -> joltError(
            "Invalid terminal starting with '${token.type}'",
            source,
            here()
        )
    }

    /**
     * @return A single value expression
     */
    private fun value(): Expr.Value {
        val (context, type) = get<TokenType.Value>()

        return Expr.Value(context, type.value)
    }

    /**
     * @return A single name expression
     */
    private fun name(): Expr.Name {
        val (context, type) = get<TokenType.Name>()

        return Expr.Name(context, type.value)
    }

    /**
     * @return A single nested expression
     */
    private fun nested(): Expr.Nested {
        val start = here()

        mustSkip(TokenType.Symbol.LEFT_PAREN)

        val expr = expr()

        mustSkip(TokenType.Symbol.RIGHT_PAREN, "Expected a right parenthesis")

        val context = start..here()

        return Expr.Nested(context, expr)
    }
}