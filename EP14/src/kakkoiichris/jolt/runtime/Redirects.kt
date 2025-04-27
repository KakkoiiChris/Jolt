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

sealed class Redirect(val origin: Context, val label: String) : Throwable() {
    class Break(origin: Context, label: String) : Redirect(origin, label)

    class Continue(origin: Context, label: String) : Redirect(origin, label)
}