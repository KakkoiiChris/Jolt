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
package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.JoltValue

/**
 * Centralized storage for all declared variables.
 */
class Memory {
    /**
     * A map of records of all declared variables.
     */
    private var scope = Scope()

    fun new(name: String, constant: Boolean): Record? {
        if (name in scope) {
            return null
        }

        val record = Record(constant, JoltValue.Number(Double.NaN))

        scope[name] = record

        return record
    }

    /**
     * @param name The variable name to retrieve
     *
     * @return The variable record stored at the given name, or null if it does not exist
     */
    operator fun get(name: String) =
        scope[name] ?: scope.parent?.get(name)

    /**
     * Sets the given name to a new record.
     *
     * @param name The variable name to set
     * @param record The variable to set
     */
    operator fun set(name: String, record: Record) {
        scope[name] = record
    }

    /**
     * Clears all records from memory.
     */
    fun clear() {
        while (true) {
            scope = scope.parent ?: break
        }

        scope.clear()
    }

    fun push(scope: Scope = Scope(this.scope)) {
        this.scope = scope
    }

    fun pop(): Scope {
        val scope = scope

        this.scope = scope.parent ?: TODO("CAN'T POP")

        return scope
    }

    /**
     * A class to store information about a variable alongside the value it contains.
     *
     * @property constant If the variable was declared as constant
     * @property value The value of ths variable
     */
    data class Record(val constant: Boolean, var value: JoltValue<*>)

    data class Scope(val parent: Scope? = null) : MutableMap<String, Record> by mutableMapOf()
}