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

import kakkoiichris.jolt.parser.Expr

/**
 * Centralized storage for all declared variables.
 */
class Memory {
    /**
     * A map of records of all declared variables.
     */
    private val values = mutableMapOf<String, Reference>()

    fun isDeclared(name: Expr.Name) =
        isDeclared(name.value)

    fun isDeclared(name: String) =
        name in values

    /**
     * @param name The variable name to retrieve
     *
     * @return The variable record stored at the given name, or null if it does not exist
     */
    operator fun get(name: String) =
        values[name]

    /**
     * Sets the given name to a new value.
     *
     * @param name The variable name to set
     * @param value The variable to set
     */
    operator fun set(name: String, value: Reference) {
        values[name] = value
    }

    data class Reference(val isConstant: Boolean, var value: Double)
}