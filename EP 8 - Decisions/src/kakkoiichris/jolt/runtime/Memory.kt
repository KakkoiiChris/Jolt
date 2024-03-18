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
    private val records = mutableMapOf<String, Record>()

    /**
     * @param name The variable name to retrieve
     *
     * @return The variable record stored at the given name, or null if it does not exist
     */
    operator fun get(name: String) =
        records[name]

    /**
     * Sets the given name to a new record.
     *
     * @param name The variable name to set
     * @param record The variable to set
     */
    operator fun set(name: String, record: Record) {
        records[name] = record
    }

    /**
     * Clears all records from memory.
     */
    fun clear() =
        records.clear()

    /**
     * A class to store information about a variable alongside the value it contains.
     *
     * @property constant If the variable was declared as constant
     * @property value The value of ths variable
     */
    data class Record(val constant: Boolean, var value: JoltValue<*>)
}