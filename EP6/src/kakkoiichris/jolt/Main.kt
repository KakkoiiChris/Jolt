/*      ___  _______  ___    _______
 *     |   ||       ||   |  |       |
 *     |   ||   _   ||   |  |_     _|
 *     |   ||  | |  ||   |    |   |
 *  ___|   ||  |_|  ||   |___ |   |
 * |       ||       ||       ||   |
 * |_______||_______||_______||___|
 *         SCRIPTING LANGUAGE
 */
package kakkoiichris.jolt

import kakkoiichris.jolt.lexer.Lexer
import kakkoiichris.jolt.parser.Parser
import kakkoiichris.jolt.parser.TypeChecker
import kakkoiichris.jolt.runtime.Runtime
import java.nio.file.Paths
import kotlin.system.exitProcess
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
fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage $JOLT jolt <filePath>".wrapBox())

        exitProcess(-1)
    }

    file(args.first())
}

/**
 * Executes a standalone version of the language for the specified file.
 *
 * @param filePath The path of the file to execute.
 */
private fun file(filePath: String) {
    val path = Paths.get(filePath)

    val source = Source.of(path)

    val (value, duration) = measureTimedValue {
        try {
            val lexer = Lexer(source)

            val parser = Parser(source, lexer)

            val program = parser.parse()

            TypeChecker.check(source, program)

            val runtime = Runtime(source)

            runtime.run(program)
        }
        catch (e: JoltError) {
            System.err.println(e.message)

            e.printStackTrace()

            Thread.sleep(20)
        }
    }

    println("$JOLT $value\n\n${duration.inWholeNanoseconds / 1E6}ms".wrapBox() + '\n')
}
