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

import kakkoiichris.jolt.lexer.Lexer
import kakkoiichris.jolt.parser.Parser
import kakkoiichris.jolt.runtime.Runtime
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
private fun exec(source: Source) = try {
    val (value, duration) = measureTimedValue {
        println("Executing '${source.text}' from '${source.name}'!")

        val lexer = Lexer(source)

        val parser = Parser(source, lexer)

        val program = parser.parse()

        val runtime = Runtime(source)

        runtime.run(program)
    }

    println("$JOLT $value\n\n${duration.inWholeNanoseconds / 1E6}ms".wrapBox() + '\n')
}
catch (e: JoltError) {
    System.err.println("${e.message}\n")

    Thread.sleep(20)
}
