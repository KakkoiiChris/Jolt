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

/**
 * Centralized storage for all declared variables.
 */
class Memory {
    /**
     * A map of records of all declared variables.
     */
    private val values = mutableMapOf<String, Double>()

    /**
     * @param name The variable name to retrieve
     *
     * @return The variable record stored at the given name, or null if it does not exist
     */
    operator fun get(name: String) =
        values[name]!!

    /**
     * Sets the given name to a new record.
     *
     * @param name The variable name to set
     * @param record The variable to set
     */
    operator fun set(name: String, value: Double) {
        values[name] = value
    }
}
