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
package kakkoiichris.jolt

import kakkoiichris.jolt.lexer.Context

/**
 * A [RuntimeException] subclass to handle errors specific to the Jolt language.
 *
 * @param message The message of the error
 */
class JoltError(message: String) : RuntimeException(message)

/**
 * Throws a [JoltError] that displays an error message, as well as underlining the location within the source code that the error occurred, wrapped in a box.
 *
 * @param message The message of the error
 * @param line The source code line on which the error occurred
 * @param context The location data of the error
 *
 * @throws JoltError
 */
fun joltError(message: String, line: String, context: Context): Nothing {
    val fullErrorMessage = buildString {
        appendLine("Jolt Error $JOLT $message!")
        appendLine()
        appendLine("${context.row}| $line")

        append(" ".repeat(context.column + (context.row.toString().length) + 1))
        append("$UNDERLINE".repeat(context.length))
    }

    throw JoltError(fullErrorMessage.wrapRoundBox())
}