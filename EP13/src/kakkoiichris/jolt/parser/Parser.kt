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

        match(TokenType.Symbol.LEFT_BRACE)                     -> blockStmt()

        matchAny(TokenType.Keyword.LET, TokenType.Keyword.VAR) -> declarationStmt()

        match(TokenType.Keyword.IF)                            -> ifElseStmt()

        match(TokenType.Keyword.WHILE)                         -> whileStmt()

        match(TokenType.Keyword.DO)                            -> doWhileStmt()

        match(TokenType.Keyword.FOR)                           -> forStmt()

        match(TokenType.Keyword.BREAK)                         -> breakStmt()

        match(TokenType.Keyword.CONTINUE)                      -> continueStmt()

        else                                                   -> expressionStmt()
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
     * @return A single block statement
     */
    private fun blockStmt(): Stmt.Block {
        val start = here()

        mustSkip(TokenType.Symbol.LEFT_BRACE)

        val stmts = mutableListOf<Stmt>()

        while (!skip(TokenType.Symbol.RIGHT_BRACE)) {
            stmts += stmt()
        }

        val context = start..here()

        return Stmt.Block(context, stmts)
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

        val assigned = skip(TokenType.Symbol.EQUAL)

        val expr = if (assigned) expr() else Expr.Empty

        mustSkip(TokenType.Symbol.SEMICOLON, "Expected a semicolon")

        val context = start..here()

        return Stmt.Declaration(context, constant, name, expr)
    }

    /**
     * @return A single empty statement
     */
    private fun ifElseStmt(): Stmt.IfElse {
        val start = here()

        mustSkip(TokenType.Keyword.IF)
        mustSkip(TokenType.Symbol.LEFT_PAREN, "Condition must start with parentheses")

        val condition = expr()

        mustSkip(TokenType.Symbol.RIGHT_PAREN, "Condition must end with parentheses")

        val branchTrue = stmt()

        val branchFalse = if (skip(TokenType.Keyword.ELSE)) stmt() else Stmt.Empty(here())

        val context = start..here()

        return Stmt.IfElse(context, condition, branchTrue, branchFalse)
    }

    /**
     * @return A single while statement
     */
    private fun whileStmt(): Stmt.While {
        val start = here()

        mustSkip(TokenType.Keyword.WHILE)

        val label = if (skip(TokenType.Symbol.AT)) name().value else ""

        mustSkip(TokenType.Symbol.LEFT_PAREN, "Condition must start with parentheses")

        val condition = expr()

        mustSkip(TokenType.Symbol.RIGHT_PAREN, "Condition must end with parentheses")

        val body = stmt()

        val context = start..here()

        return Stmt.While(context, label, condition, body)
    }

    /**
     * @return A single so-while statement
     */
    private fun doWhileStmt(): Stmt.DoWhile {
        val start = here()

        mustSkip(TokenType.Keyword.DO)

        val label = if (skip(TokenType.Symbol.AT)) name().value else ""

        val body = stmt()

        mustSkip(TokenType.Symbol.LEFT_PAREN, "Condition must start with parentheses")

        val condition = expr()

        mustSkip(TokenType.Symbol.RIGHT_PAREN, "Condition must end with parentheses")
        mustSkip(TokenType.Symbol.SEMICOLON, "Do-while loop must end with a semicolon")

        val context = start..here()

        return Stmt.DoWhile(context, label, condition, body)
    }

    /**
     * @return A single so-while statement
     */
    private fun forStmt(): Stmt.For {
        val start = here()

        mustSkip(TokenType.Keyword.FOR)

        val label = if (skip(TokenType.Symbol.AT)) name().value else ""

        mustSkip(TokenType.Symbol.LEFT_PAREN, "Condition must start with parentheses")

        val pointer = name()

        mustSkip(TokenType.Symbol.COLON)

        val iterable = expr()

        mustSkip(TokenType.Symbol.RIGHT_PAREN, "Condition must end with parentheses")

        val body = stmt()

        val context = start..here()

        return Stmt.For(context, label, pointer, iterable, body)
    }

    /**
     * @return A single break statement
     */
    private fun breakStmt(): Stmt.Break {
        val start = here()

        mustSkip(TokenType.Keyword.BREAK)

        val label = if (!match(TokenType.Symbol.SEMICOLON)) name().value else ""

        mustSkip(TokenType.Symbol.SEMICOLON)

        val context = start..here()

        return Stmt.Break(context, label)
    }

    /**
     * @return A single continue statement
     */
    private fun continueStmt(): Stmt.Continue {
        val start = here()

        mustSkip(TokenType.Keyword.CONTINUE)

        val label = if (!match(TokenType.Symbol.SEMICOLON)) name().value else ""

        mustSkip(TokenType.Symbol.SEMICOLON)

        val context = start..here()

        return Stmt.Continue(context, label)
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

    /**
     * @return A single assignment expression if an `=` is present
     */
    private fun assign(): Expr {
        val expr = or()

        if (match(TokenType.Symbol.EQUAL)) {
            if (expr is Expr.GetIndex) {
                val start = expr.context

                mustSkip(TokenType.Symbol.EQUAL)

                val value = or()

                val context = start..here()

                return Expr.SetIndex(context, expr.target, expr.index, value)
            }

            if (expr !is Expr.Name) {
                joltError("Cannot assign to '${expr.javaClass.simpleName}'!", source, expr.context)
            }

            val start = expr.context

            mustSkip(TokenType.Symbol.EQUAL)

            val value = or()

            val context = start..here()

            return Expr.Assign(context, expr, value)
        }

        return expr
    }

    /**
     * @return A single equality binary expression if a `==` or `!=` is present
     */
    private fun or(): Expr {
        var expr = xor()

        while (match(TokenType.Symbol.PIPE)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = xor()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single equality binary expression if a `==` or `!=` is present
     */
    private fun xor(): Expr {
        var expr = and()

        while (match(TokenType.Symbol.CARET)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = and()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single equality binary expression if a `==` or `!=` is present
     */
    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.Symbol.AMPERSAND)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = equality()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single equality binary expression if a `==` or `!=` is present
     */
    private fun equality(): Expr {
        var expr = relational()

        while (matchAny(TokenType.Symbol.DOUBLE_EQUAL, TokenType.Symbol.EXCLAMATION_EQUAL)) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = relational()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single relational binary expression if a `<`, `<=`, `>`, or `>=` is present
     */
    private fun relational(): Expr {
        var expr = additive()

        while (matchAny(
                TokenType.Symbol.LESS,
                TokenType.Symbol.LESS_EQUAL,
                TokenType.Symbol.GREATER,
                TokenType.Symbol.GREATER_EQUAL
            )
        ) {
            val (_, type) = get<TokenType.Symbol>()

            val operator = Expr.Binary.Operator[type]

            val right = additive()

            val context = expr.context..here()

            expr = Expr.Binary(context, operator, expr, right)
        }

        return expr
    }

    /**
     * @return A single additive binary expression if a `+` or `-` is present
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
     * @return A single multiplicative binary expression if a `*`, `/`,  or `%` is present
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
     * @return A single prefix unary expression if a `-` is present
     */
    private fun prefix(): Expr {
        if (matchAny(TokenType.Symbol.DASH, TokenType.Symbol.EXCLAMATION, TokenType.Symbol.POUND)) {
            val (start, type) = get<TokenType.Symbol>()

            val operator = Expr.Unary.Operator[type]

            val expr = prefix()

            val context = start..here()

            return Expr.Unary(context, operator, expr)
        }

        return postfix()
    }

    /**
     * @return A single postfix expr if a '[' is present
     */
    private fun postfix(): Expr {
        var expr = terminal()

        while (match(TokenType.Symbol.LEFT_SQUARE)) {
            val (_, _) = get<TokenType.Symbol>()

            val index = expr()

            mustSkip(TokenType.Symbol.RIGHT_SQUARE)

            val context = expr.context..here()

            expr = Expr.GetIndex(context, expr, index)
        }

        return expr
    }

    /**
     * @return A single terminal expression
     */
    private fun terminal() = when {
        match<TokenType.Value>()            -> value()

        match<TokenType.Name>()             -> name()

        match(TokenType.Symbol.LEFT_PAREN)  -> nested()

        match(TokenType.Symbol.LEFT_SQUARE) -> list()

        else                                -> joltError(
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

    private fun list(): Expr {
        val start = here()

        mustSkip(TokenType.Symbol.LEFT_SQUARE)

        val expr = expr()

        if (skip(TokenType.Keyword.FOR)) {
            val pointer = name()

            mustSkip(TokenType.Symbol.COLON)

            val iterable = expr()

            mustSkip(TokenType.Symbol.RIGHT_SQUARE)

            val context = start..here()

            return Expr.ListGenerator(context, expr, pointer, iterable)
        }

        val elements = mutableListOf(expr)

        if (!skip(TokenType.Symbol.RIGHT_SQUARE)) {
            while (skip(TokenType.Symbol.COMMA)) {
                elements += expr()
            }

            mustSkip(TokenType.Symbol.RIGHT_SQUARE)
        }

        val context = start..here()

        return Expr.ListLiteral(context, elements)
    }
}