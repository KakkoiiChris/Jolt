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
        override fun toString() =
            value
    }
}