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
package kakkoiichris.jolt.lexer

import kakkoiichris.jolt.JoltError
import kakkoiichris.jolt.JoltValue
import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError

/**
 * A class that converts source code into tokens, one at a time via the [Iterator] mechanism.
 *
 * @property source The code [Source] to convert
 */
class Lexer(private val source: Source) : Iterator<Token<*>> {
    companion object {
        /**
         * The null-terminator character, used to mark the end of the source.
         */
        private const val NUL = '\u0000'

        /**
         *
         */
        private val literals = listOf(true, false)
            .map { JoltValue.Boolean(it) }
            .associateBy { it.toString() }

        /**
         * @return `true` if the given character is alphabetic or is an underscore, or `false` otherwise
         */
        private fun isWordStartChar(char: Char) = char.isLetter() || char == '_'

        /**
         * @return `true` if the given character is alphanumeric or is an underscore, or `false` otherwise
         */
        private fun isWordChar(char: Char) = char.isLetterOrDigit() || char == '_'
    }

    /**
     * The position within the source code string.
     */
    private var pos = 0

    /**
     * The vertical position within the source code.
     */
    private var row = 1

    /**
     * The horizontal position within the source code.
     */
    private var column = 1

    /**
     * @return `true` if [pos] is less than or equal to the length if the source code, or `false` otherwise
     *
     * @see Iterator.hasNext
     */
    override fun hasNext() =
        pos <= source.text.length

    /**
     * Skips any skippable characters before yielding a [Token].
     *
     * @return A single [Token]
     *
     * @see Iterator.next
     */
    override fun next(): Token<*> {
        while (!atEndOfFile()) {
            // Text to skip...
            if (match(Char::isWhitespace)) {
                skipWhitespace()

                continue
            }

            if (match("//")) {
                skipLineComment()

                continue
            }

            if (match("/*")) {
                skipBlockComment()

                continue
            }

            // Text to turn into tokens...
            return when {
                match(Char::isDigit)     -> number()

                match(::isWordStartChar) -> word()

                else                     -> symbol()
            }
        }

        return endOfFile()
    }

    /**
     * Sets the counters to their initial positions so the lexer can be used again.
     */
    fun reset() {
        pos = 0
        row = 1
        column = 1
    }

    /**
     * @param length The length of the token
     *
     * @return A new [Context] instance with the location data of the current position within the [Lexer]
     */
    private fun here(length: Int = 1) =
        Context(source.name, row, column, length)

    /**
     * @param offset The amount of characters to look forward
     *
     * @return The character at the current [position][pos] plus the given offset if it is within the length of the source code plus 1, or [NUL] otherwise
     */
    private fun peek(offset: Int = 0) =
        if (pos + offset < source.text.length)
            source.text[pos + offset]
        else
            NUL

    /**
     * @param length The length of the substring to retrieve
     *
     * @return A substring of the source code from the current position
     */
    private fun look(length: Int) =
        buildString {
            repeat(length) { i ->
                append(peek(i))
            }
        }

    /**
     * @param char The character to match
     *
     * @return `true` if the character was matched, or `false` otherwise
     */
    private fun match(char: Char) =
        peek() == char

    /**
     * @param predicate The predicate to match against
     *
     * @return `true` if the predicate was matched, or `false` otherwise
     */
    private fun match(predicate: (Char) -> Boolean) =
        predicate(peek())

    /**
     * @param string The string to match
     *
     * @return `true` if the string was matched, or `false` otherwise
     */
    private fun match(string: String) =
        look(string.length) == string

    /**
     * Updates the row and column in response to newline characters, and advances the lexer.
     *
     * @param amount The amount of characters to move the lexer by
     */
    private fun step(amount: Int = 1) =
        repeat(amount) {
            if (match('\n')) {
                row++
                column = 1
            }
            else {
                column++
            }

            pos++
        }

    /**
     * Advances the lexer if the currently [peeked][peek] character is equivalent to the given character.
     *
     * @param char The character to skip
     *
     * @return `true` if the character was matched, or `false` otherwise
     */
    private fun skip(char: Char) =
        if (match(char)) {
            step()

            true
        }
        else
            false

    /**
     * Advances the lexer if the currently [peeked][peek] character matches the given character predicate.
     *
     * @param predicate The predicate to test against
     *
     * @return `true` if the predicate was matched, or `false` otherwise
     */
    private fun skip(predicate: (Char) -> Boolean) =
        if (match(predicate)) {
            step()

            true
        }
        else
            false

    /**
     * Advances the lexer by the length of the given string if an equivalent substring can be found at the current position.
     *
     * @param string The string to skip
     *
     * @return `true` if the string was matched, or `false` otherwise
     */
    private fun skip(string: String) =
        if (match(string)) {
            step(string.length)

            true
        }
        else
            false

    /**
     * Produces an [error][JoltError] with the given error message if the given character is not skipped.
     *
     * @param char The character to skip
     * @param errorMessage The message for the error
     *
     * @throws JoltError If the character is not skipped
     */
    private fun mustSkip(char: Char, errorMessage: String) {
        if (!skip(char)) {
            val context = here()

            joltError(errorMessage, source.getLine(context.row), context)
        }
    }

    /**
     * Produces an [error][JoltError] with the given error message if the given string is not skipped.
     *
     * @param string The string to skip
     * @param errorMessage The error message to show
     *
     * @throws JoltError If the string is not skipped
     */
    private fun mustSkip(string: String, errorMessage: String) {
        if (!skip(string)) {
            val context = here(string.length)

            joltError(errorMessage, source.getLine(context.row), context)
        }
    }

    /**
     * @return `true` if the currently [peeked][peek] character is [NUL], or false otherwise
     */
    private fun atEndOfFile() =
        match(NUL)

    /**
     * Steps past any contiguous whitespace characters.
     */
    private fun skipWhitespace() {
        while (skip(Char::isWhitespace)) Unit
    }

    /**
     * Steps past characters until it reaches the end of a line or the end of the file.
     *
     * @throws JoltError If not at beginning of a line comment (should not happen)
     */
    private fun skipLineComment() {
        mustSkip("//", "Expected the beginning of a line comment")

        while (!skip('\n')) {
            if (atEndOfFile()) break

            step()
        }
    }

    /**
     * Steps past characters until it reaches the end of the block comment.
     *
     * @throws JoltError If not at beginning of a block comment (should not happen), or if the end of the file is reached before the comment is closed
     */
    private fun skipBlockComment() {
        val start = here(2)

        mustSkip("/*", "Expected the beginning of a block comment")

        while (!skip("*/")) {
            if (atEndOfFile()) joltError("Unclosed block comment", source.getLine(start.row), start)

            step()
        }
    }

    /**
     * [StringBuilder] extension to append the current character and step past it.
     */
    private fun StringBuilder.take() {
        append(peek())

        step()
    }

    /**
     * @return A [token][Token] with a [Value][TokenType.Value] token type containing the lexed number
     */
    private fun number(): Token<TokenType.Value<JoltValue.Number>> {
        val start = here()

        val result = buildString {
            do {
                take()
            }
            while (match(Char::isDigit))

            if (match('.')) {
                do {
                    take()
                }
                while (match(Char::isDigit))
            }
        }

        val context = start..here()

        val type = TokenType.Value(JoltValue.Number(result.toDouble()))

        return Token(context, type)
    }

    /**
     * @return A [token][Token] with a [Keyword][TokenType.Keyword] token type if the lexed word is a valid keyword, or a [Name][TokenType.Name] token type otherwise
     */
    private fun word(): Token<*> {
        val start = here()

        val result = buildString {
            do {
                take()
            }
            while (match(::isWordChar))
        }

        val context = start..here()

        val keyword = TokenType.Keyword.entries.firstOrNull { it.name.equals(result, ignoreCase = true) }

        val literal = literals[result]

        val type = keyword
            ?: literal?.let { TokenType.Value(it) }
            ?: TokenType.Name(result)

        return Token(context, type)
    }

    /**
     * @return A [token][Token] with a [Symbol][TokenType.Symbol] token [type][TokenType]
     *
     * @throws JoltError If an unknown character is encountered
     */
    private fun symbol(): Token<TokenType.Symbol> {
        val start = here()

        val symbol = when {
            skip('=')  -> when {
                skip('=') -> TokenType.Symbol.DOUBLE_EQUAL

                else      -> TokenType.Symbol.EQUAL
            }

            skip("||") -> TokenType.Symbol.DOUBLE_PIPE

            skip("&&") -> TokenType.Symbol.DOUBLE_AMPERSAND

            skip('!')  -> when {
                skip('=') -> TokenType.Symbol.EXCLAMATION_EQUAL

                else      -> TokenType.Symbol.EXCLAMATION
            }

            skip('<')  -> when {
                skip('=') -> TokenType.Symbol.LESS_EQUAL

                else      -> TokenType.Symbol.LESS
            }

            skip('>')  -> when {
                skip('=') -> TokenType.Symbol.GREATER_EQUAL

                else      -> TokenType.Symbol.GREATER
            }

            skip('+')  -> TokenType.Symbol.PLUS

            skip('-')  -> TokenType.Symbol.DASH

            skip('*')  -> TokenType.Symbol.STAR

            skip('/')  -> TokenType.Symbol.SLASH

            skip('%')  -> TokenType.Symbol.PERCENT

            skip('(')  -> TokenType.Symbol.LEFT_PAREN

            skip(')')  -> TokenType.Symbol.RIGHT_PAREN

            skip('{')  -> TokenType.Symbol.LEFT_BRACE

            skip('}')  -> TokenType.Symbol.RIGHT_BRACE

            skip(';')  -> TokenType.Symbol.SEMICOLON

            else       -> joltError("Illegal character '${peek()}'", source.getLine(start.row), start)
        }

        val context = start..here()

        return Token(context, symbol)
    }

    /**
     * @return A [token][Token] containing the [EndOfFile][TokenType.EndOfFile] token [type][TokenType]
     *
     * @throws JoltError If not at the end of the file (should not happen)
     */
    private fun endOfFile(): Token<TokenType.EndOfFile> {
        val context = here()

        mustSkip(NUL, "Expected the end of the file")

        return Token(context, TokenType.EndOfFile)
    }
}