/*      ___  _______  ___    _______
 *     |   ||       ||   |  |       |
 *     |   ||   _   ||   |  |_     _|
 *     |   ||  | |  ||   |    |   |
 *  ___|   ||  |_|  ||   |___ |   |
 * |       ||       ||       ||   |
 * |_______||_______||_______||___|
 *         SCRIPTING LANGUAGE
 */
package kakkoiichris.jolt.lexer

import kakkoiichris.jolt.JoltError
import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.lexer.Lexer.Companion.NUL

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

            return when {
                match(Char::isDigit) -> number()

                else                 -> joltError("Illegal character '${peek()}'", source, here())
            }
        }

        return endOfFile()
    }

    /**
     * @param length The length of the token
     *
     * @return A new [Context] instance with the location data of the current position within the [Lexer]
     */
    private fun here(length: Int = 1) =
        Context(source.name, row, column, pos, pos + length)

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
        else false

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
    private fun mustSkip(char: Char, errorMessage: String = "BROKEN") {
        if (!skip(char)) {
            joltError(errorMessage, source, here())
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
    private fun mustSkip(string: String, errorMessage: String = "BROKEN") {
        if (!skip(string)) {
            joltError(errorMessage, source, here(string.length))
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
        while (match(Char::isWhitespace)) step()
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
            if (atEndOfFile()) joltError("Unclosed block comment", source, start)

            step()
        }
    }

    /**
     * [StringBuilder] extension to append the current character and step past it.
     */
    private fun StringBuilder.take() {
        val char = peek()

        append(char)

        mustSkip(char)
    }

    /**
     * @return A [token][Token] with a [Value][TokenType.Value] token [type][TokenType] containing the lexed number
     */
    private fun number(): Token<TokenType.Value> {
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

            if (match { it in "Ee" }) {
                take()

                if (match { it in "+-" }) {
                    take()
                }

                do {
                    take()
                }
                while (match(Char::isDigit))
            }
        }

        val context = start..here()

        val type = TokenType.Value(result.toDouble())

        return Token(context, type)
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