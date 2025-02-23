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
        match(TokenType.Symbol.SEMICOLON) -> emptyStmt()

        else                              -> expressionStmt()
    }

    /**
     * @return A single empty statement
     */
    private fun emptyStmt(): Stmt.Empty {
        val context = here()

        mustSkip(TokenType.Symbol.SEMICOLON, "Expected a semicolon")

        return Stmt.Empty(context)
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
        valueExpr()

    /**
     * @return A single value expression
     */
    private fun valueExpr(): Expr.Value {
        val (context, type) = get<TokenType.Value>()

        return Expr.Value(context, type.value)
    }
}