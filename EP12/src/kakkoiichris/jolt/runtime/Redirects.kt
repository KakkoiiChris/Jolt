/*      ___  _______  ___    _______ 
 *     |   ||       ||   |  |       |
 *     |   ||   _   ||   |  |_     _|
 *     |   ||  | |  ||   |    |   |  
 *  ___|   ||  |_|  ||   |___ |   |  
 * |       ||       ||       ||   |  
 * |_______||_______||_______||___|  
 *         SCRIPTING LANGUAGE
 */
package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.lexer.Context

sealed class Redirect(val origin: Context) : Throwable()

class Break(origin: Context) : Redirect(origin)

class Continue(origin: Context) : Redirect(origin)