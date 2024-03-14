package kakkoiichris.jolt.runtime

class Memory {
    private val variables = mutableMapOf<String, VariableRecord>()

    operator fun get(name: String) =
        variables[name]

    operator fun set(name: String, record: VariableRecord) {
        variables[name] = record
    }

    fun clear()=
        variables.clear()

    data class VariableRecord(val constant: Boolean, var value: Double)
}