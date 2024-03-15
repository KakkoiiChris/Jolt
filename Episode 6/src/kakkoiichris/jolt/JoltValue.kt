package kakkoiichris.jolt

sealed interface JoltValue<X> {
    val value: X

    data class Boolean(override val value: kotlin.Boolean) : JoltValue<kotlin.Boolean>

    data class Number(override val value: Double) : JoltValue<Double>
}