package kakkoiichris.jolt

import kotlin.math.floor

interface JoltValue<X> {
    val value: X

    val type: String

    val iterable: List<JoltValue<*>> get() = listOf(this)

    companion object {
        fun of(x: Any) = when (x) {
            is Boolean -> JoltBool(x)

            is Double  -> JoltNum(x)

            is String  -> JoltString(x)

            else       -> TODO("NOT A VALUE")
        }
    }
}

data class JoltBool(override val value: Boolean) : JoltValue<Boolean> {
    override val type = "bool"

    override fun toString() = value.toString()
}

data class JoltNum(override val value: Double) : JoltValue<Double> {
    override val type = "num"

    override val iterable get() = (0..<value.toInt()).map { JoltNum(it.toDouble()) }.toList()

    private fun truncate() = (
        if (value == floor(value))
            value.toInt()
        else
            value
        ).toString()

    override fun toString() = truncate()
}

data class JoltString(override val value: String) : JoltValue<String> {
    override val type = "string"

    override val iterable get() = value.toCharArray().map { JoltString(it.toString()) }.toList()

    override fun toString() = "\"$value\""
}

data class JoltList(override val value: MutableList<JoltValue<*>>) : JoltValue<MutableList<JoltValue<*>>> {
    override val type get() = "list"

    override val iterable get() = value.toList()

    override fun toString() = if (value.isNotEmpty())
        value.joinToString(prefix = "[", postfix = "]", separator = ", ") { it.toString() }
    else
        "[]"
}