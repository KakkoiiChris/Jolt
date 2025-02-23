package kakkoiichris.jolt

import kotlin.math.floor

interface JoltValue<X> {
    val value: X

    companion object {
        fun of(x: Any) = when (x) {
            is Boolean -> JoltBool(x)

            is Double  -> JoltNum(x)

            else       -> TODO("NOT A VALUE")
        }
    }
}

data class JoltBool(override val value: Boolean) : JoltValue<Boolean> {
    override fun toString() = value.toString()
}

data class JoltNum(override val value: Double) : JoltValue<Double> {
    private fun truncate() = (
        if (value == floor(value))
            value.toInt()
        else
            value
        ).toString()

    override fun toString() = truncate()
}