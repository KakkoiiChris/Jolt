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

import kakkoiichris.jolt.JoltValue
import kakkoiichris.jolt.parser.Expr

/**
 * Centralized storage for all declared variables.
 */
class Memory {
    /**
     * A map of references to all declared variables.
     */
    private val references = mutableMapOf<String, Reference>()

    /**
     * @param name The name to look up
     *
     * @return `true` if the name exists, or `false` otherwise
     */
    fun isDeclared(name: Expr.Name) =
        name.value in references

    /**
     * Inserts a new variable into memory with the given information.
     *
     * @param isConstant Whether the variable was declared as a constant
     * @param name The name of the variable
     * @param value The value stored by the variable
     */
    fun declare(isConstant: Boolean, name: Expr.Name, value: JoltValue<*>) {
        references[name.value] = Reference(isConstant, value)
    }

    /**
     * @param name The variable name to retrieve
     *
     * @return The reference stored at the given name, or null if it does not exist
     */
    operator fun get(name: String) =
        references[name]

    /**
     * Sets the given name to a new reference.
     *
     * @param name The variable name to set
     * @param value The reference to set
     */
    operator fun set(name: String, value: Reference) {
        references[name] = value
    }

    /**
     * A class that holds all the information for a given variable in memory.
     *
     * @param isConstant Whether the variable was declared as constant
     * @param value The value stored by the variable
     */
    data class Reference(val isConstant: Boolean, var value: JoltValue<*>)
}