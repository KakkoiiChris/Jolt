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

import kakkoiichris.jolt.Source
import kakkoiichris.jolt.joltError

class Lexer(private val source: Source) : Iterator<Token> {
    companion object {
        private const val NUL = '\u0000'
    }

    private var pos = 0
    private var row = 1
    private var column = 1

    override fun hasNext() =
        pos <= source.text.length

    override fun next(): Token {
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

                else                 -> {
                    val context = here()

                    joltError("Illegal character '${peek()}'", source.getLine(context.row), context)
                }
            }
        }

        return endOfFile()
    }

    private fun here(length: Int = 1) =
        Context(source.name, row, column, pos, pos + length - 1)

    private fun peek(offset: Int = 0) =
        if (pos + offset < source.text.length)
            source.text[pos + offset]
        else
            NUL

    private fun look(length: Int) =
        buildString {
            repeat(length) { i ->
                append(peek(i))
            }
        }

    private fun match(char: Char) =
        peek() == char

    private fun match(predicate: (Char) -> Boolean) =
        predicate(peek())

    private fun match(string: String) =
        look(string.length) == string

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

    private fun skip(char: Char) =
        if (match(char)) {
            step()

            true
        }
        else
            false

    private fun skip(predicate: (Char) -> Boolean) =
        if (match(predicate)) {
            step()

            true
        }
        else
            false

    private fun skip(string: String) =
        if (match(string)) {
            step(string.length)

            true
        }
        else
            false

    private fun mustSkip(char: Char, errorMessage: String) {
        if (!skip(char)) {
            val context = here()

            joltError(errorMessage, source.getLine(context.row), context)
        }
    }

    private fun mustSkip(string: String, errorMessage: String) {
        if (!skip(string)) {
            val context = here(string.length)

            joltError(errorMessage, source.getLine(context.row), context)
        }
    }

    private fun atEndOfFile() =
        match(NUL)

    private fun skipWhitespace() {
        while (skip(Char::isWhitespace)) Unit
    }

    private fun skipLineComment() {
        mustSkip("//", "Expected the beginning of a line comment")

        while (!skip('\n')) {
            if (atEndOfFile()) break

            step()
        }
    }

    private fun skipBlockComment() {
        mustSkip("/*", "Expected the beginning of a block comment")

        while (!skip("*/")) {
            step()
        }
    }

    private fun StringBuilder.take() {
        append(peek())

        step()
    }

    private fun number(): Token {
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

        val type = TokenType.Value(result.toDouble())

        return Token(context, type)
    }

    private fun endOfFile(): Token {
        val context = here()

        mustSkip(NUL, "Expected the end of the file")

        return Token(context, TokenType.EndOfFile)
    }
}