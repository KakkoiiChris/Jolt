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

import kakkoiichris.jolt.JOLT
import kakkoiichris.jolt.parser.Expr

/**
 * Centralized storage for all declared variables.
 */
class Memory {
    /**
     * A map of references to all declared variables.
     */
    private var stack = Scope()

    /**
     * Puts an empty scope onto the stack.
     */
    fun push() {
        stack = Scope(stack)
    }

    /**
     * Removes the current scope from the stack.
     */
    fun pop() {
        stack = stack.parent ?: error("JOLT ERROR $JOLT MEMORY STACK UNDERFLOW")
    }

    /**
     * @param name The name to look up
     *
     * @return `true` if the name exists in the current scope, or `false` otherwise
     */
    fun isDeclared(name: Expr.Name) =
        stack.isDeclared(name)

    /**
     * Inserts a new variable into the current scope with the given information.
     *
     * @param isConstant Whether the variable was declared as a constant
     * @param name The name of the variable
     * @param value The value stored by the variable
     */
    fun declare(isConstant: Boolean, name: Expr.Name, value: Double) {
        stack.declare(isConstant, name, value)
    }

    /**
     * Walks from the current scope down until it finds the reference or reaches the bottom.
     *
     * @param name The variable name to retrieve
     *
     * @return The reference stored at the given name, or null if it does not exist
     */
    operator fun get(name: String): Reference? {
        var here: Scope? = stack

        while (here != null) {
            if (name in here) {
                return here[name]!!
            }

            here = here.parent
        }

        return null
    }

    /**
     * A class that holds all references in a given memory scope.
     *
     * @param parent The scope outside of this one
     */
    data class Scope(val parent: Scope? = null) : MutableMap<String, Reference> by mutableMapOf() {
        /**
         * @param name The name to look up
         *
         * @return `true` if the name exists, or `false` otherwise
         */
        fun isDeclared(name: Expr.Name) =
            name.value in this

        /**
         * Inserts a new variable with the given information.
         *
         * @param isConstant Whether the variable was declared as a constant
         * @param name The name of the variable
         * @param value The value stored by the variable
         */
        fun declare(isConstant: Boolean, name: Expr.Name, value: Double) {
            set(name.value, Reference(isConstant, value))
        }
    }

    /**
     * A class that holds all the information for a given variable in memory.
     *
     * @param isConstant Whether the variable was declared as constant
     * @param value The value stored by the variable
     */
    data class Reference(val isConstant: Boolean, var value: Double)
}