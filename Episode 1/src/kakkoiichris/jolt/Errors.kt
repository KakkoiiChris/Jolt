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

class JoltError(message: String) : RuntimeException(message)

fun joltError(message: String, line: String, context: Context): Nothing {
    val fullErrorMessage = buildString {
        appendLine("Jolt Error $JOLT $message!")
        appendLine()
        appendLine("${context.row}| $line")

        append(" ".repeat(context.column + (context.row.toString().length) + 1))
        append("$UNDERLINE".repeat(context.end - context.start + 1))
    }

    throw JoltError(fullErrorMessage.wrapRoundBox())
}