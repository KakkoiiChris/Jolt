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

/**
 * A container for all parsed structures from a source file.
 *
 * @property stmts The statements of this program
 */
class Program(private val stmts: Stmts) : Iterator<Stmt> by stmts.iterator()