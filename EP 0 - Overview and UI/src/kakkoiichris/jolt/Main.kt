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

import java.nio.file.Paths
import kotlin.time.measureTimedValue

/**
 * Program entry point.
 *
 * If **zero** arguments are supplied, the program enters REPL mode.
 *
 * If **one** argument is supplied, it is used as the name of the file to run.
 *
 * @param args Arguments from the command line
 */
fun main(args: Array<String>) = when (args.size) {
    0    -> repl()

    1    -> file(args.first())

    else -> System.err.println("Usage $JOLT jolt [filePath]".wrapBox())
}

/**
 * Executes a Read-Eval-Print Loop version of the language with persistent memory.
 */
private fun repl() {
    println("""        
        Jolt by KakkoiiChris
        v0.0
        
        Notes:
        - Memory is kept between inputs.
        - Tilde clears memory.
        - Empty line exits program.
    """.trimIndent().wrapBox() + '\n')

    while (true) {
        print("$JOLT ")

        val text = readlnOrNull()?.takeIf { it.isNotEmpty() } ?: break

        println()

        if (text == "~") {
            println("$JOLT Memory Cleared!".wrapBox() + "\n")

            continue
        }

        val source = Source("<REPL>", text)

        exec(source)
    }
}

/**
 * Executes a standalone version of the language for the specified file.
 *
 * @param filePath The path of the file to execute.
 */
private fun file(filePath: String) {
    val path = Paths.get(filePath)

    val source = Source.of(path)

    exec(source)
}

/**
 * Times the execution of the specified source, and prints the result and time to the screen.
 *
 * @param source The source code to execute
 */
private fun exec(source: Source) {
    val (value, duration) = measureTimedValue {
        println("Executing '${source.text}' from '${source.name}'!\n")
    }

    println("$JOLT $value\n\n${duration.inWholeNanoseconds / 1E6}ms".wrapBox() + '\n')
}
