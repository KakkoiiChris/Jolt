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
package kakkoiichris.jolt

/**
 *
 */
sealed interface JoltValue<X> {
    /**
     *
     */
    val value: X

    val iterable: kotlin.collections.List<JoltValue<*>>? get() = null

    /**
     *
     */
    data class Boolean(override val value: kotlin.Boolean) : JoltValue<kotlin.Boolean> {
        override fun toString() =
            value.toString()
    }

    /**
     *
     */
    data class Number(override val value: Double) : JoltValue<Double> {
        override fun toString() =
            value.truncate()
    }

    /**
     *
     */
    data class String(override val value: kotlin.String) : JoltValue<kotlin.String> {
        override val iterable get() = value.map { String(it.toString()) }

        override fun toString() =
            value
    }

    /**
     *
     */
    data class List(override val value: MutableList<JoltValue<*>>) : JoltValue<MutableList<JoltValue<*>>> {
        override val iterable get() = value

        override fun toString() =
            if (value.isEmpty())
                "[]"
            else
                value.joinToString(prefix = "[ ", postfix = " ]")
    }
}