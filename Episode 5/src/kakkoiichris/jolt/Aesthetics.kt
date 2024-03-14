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

import kotlin.math.floor

/**
 * The 'Lightning Bolt' emoji, used in many places in the UI.
 */
const val JOLT = '⚡'

/**
 * The double horizontal box drawing character, used to underline locations of errors.
 */
const val UNDERLINE = '═'

/**
 * @receiver The value to truncate
 *
 * @return A string version of this number, with the decimal point and zero removed if it is a whole number
 */
fun Double.truncate(): String {
    val floor = floor(this)

    return if (equals(floor))
        floor.toInt().toString()
    else
        toString()
}

/**
 * An intermediate class to give names for the different
 *
 * @property chars The characters to be used for creating boxes
 */
@JvmInline
value class CharSet(private val chars: String) {
    /**
     * The vertical line character.
     */
    val vertical get() = chars[0]

    /**
     * The horizontal line character.
     */
    val horizontal get() = chars[1]

    /**
     * The upper left corner character.
     */
    val upLeft get() = chars[2]

    /**
     * The upper right corner character.
     */
    val upRight get() = chars[3]

    /**
     * The lower left corner character.
     */
    val downLeft get() = chars[4]

    /**
     * The lower right corner character
     */
    val downRight get() = chars[5]

    /**
     * The left facing T intersection character.
     */
    val crossLeft get() = chars[6]

    /**
     * The right facing T intersection character.
     */
    val crossRight get() = chars[7]
}

/**
 * Creates a box of best fit around the receiving string.
 *
 * When the string has multiple lines in it, the box will fit the width of the longest line. All lines are left aligned.
 *
 * If any of the lines are blank, a horizontal dividing line will be put in its place.
 *
 * @receiver A [String], preferably of length 1 or more, possibly containing multiple newline characters
 *
 * @param charSet The set of characters to use when creating the box.
 *
 * @return A copy of the string with a box around it
 */
private fun String.wrapBox(charSet: CharSet) = buildString {
    val lines = this@wrapBox.lines()

    val maxWidth = lines.maxOf { it.length }

    appendLine("${charSet.upLeft}${charSet.horizontal.toString().repeat(maxWidth + 2)}${charSet.upRight}")

    for (line in lines) {
        if (line.isEmpty()) {
            appendLine("${charSet.crossLeft}${charSet.horizontal.toString().repeat(maxWidth + 2)}${charSet.crossRight}")
        }
        else {
            appendLine("${charSet.vertical} ${line.padEnd(maxWidth)} ${charSet.vertical}")
        }
    }

    append("${charSet.downLeft}${charSet.horizontal.toString().repeat(maxWidth + 2)}${charSet.downRight}")
}

/**
 * Wraps a single-outline box around the receiving string.
 *
 * @receiver A [String], preferably of length 1 or more, possibly containing multiple newline characters
 *
 * @return A copy of the string with a box around it
 */
fun String.wrapSingleBox() =
    wrapBox(CharSet("│─┌┐└┘├┤"))

/**
 * Wraps a single-outline rounded-corner box around the receiving string.
 *
 * @receiver A [String], preferably of length 1 or more, possibly containing multiple newline characters
 *
 * @return A copy of the string with a rounded box around it
 */
fun String.wrapRoundBox() =
    wrapBox(CharSet("│─╭╮╰╯├┤"))

/**
 * Wraps a double-outline box around the receiving string.
 *
 * @receiver A [String], preferably of length 1 or more, possibly containing multiple newline characters
 *
 * @return A copy of the string with a double box around it
 */
fun String.wrapDoubleBox() =
    wrapBox(CharSet("║═╔╗╚╝╠╣"))
