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
import kakkoiichris.jolt.runtime.Memory
import kakkoiichris.jolt.runtime.Runtime
import java.nio.file.Paths
import kotlin.time.Duration
import kotlin.time.TimedValue
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

    else -> System.err.println("Usage $JOLT jolt [filePath]".wrapDoubleBox())
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
    """.trimIndent().wrapDoubleBox() + '\n')

    val memory = Memory()

    while (true) {
        print("$JOLT ")

        val text = readlnOrNull()?.takeIf { it.isNotEmpty() } ?: break

        println()

        if (text == "~") {
            memory.clear()

            println("$JOLT Memory Cleared!".wrapRoundBox() + "\n")

            continue
        }

        val source = Source("<REPL>", text)

        val lexer = Lexer(source)

        val parser = Parser(source, lexer)

        val (value, duration) = try {
            measureTimedValue {
                val program = parser.parse()

                val runtime = Runtime(source, memory)

                runtime.run(program)
            }
        }
        catch (e: JoltError) {
            try {
                measureTimedValue {
                    parser.reset()

                    val expr = parser.parseExpr()

                    val runtime = Runtime(source, memory)

                    runtime.runExpr(expr)
                }
            }
            catch (e: JoltError) {
                System.err.println("${e.message}\n")

                Thread.sleep(20)

                TimedValue(0.0, Duration.ZERO)
            }
        }

        if (duration != Duration.ZERO) {
            println("$JOLT ${value.truncate()}\n\n${duration.inWholeNanoseconds / 1E6}ms".wrapRoundBox() + '\n')
        }
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

    try {
        val lexer = Lexer(source)

        val parser = Parser(source, lexer)

        val program = parser.parse()

        val runtime = Runtime(source)

        runtime.run(program)
    }
    catch (e: JoltError) {
        System.err.println("${e.message}\n")

        Thread.sleep(20)
    }
}
