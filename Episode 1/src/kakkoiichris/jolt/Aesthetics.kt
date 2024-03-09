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
 * The 'Lightning Bolt' emoji, used in many places in the UI.
 */
const val JOLT = '⚡'

/**
 * The double horizontal box drawing character, used to underline locations of errors.
 */
const val UNDERLINE = '═'

/**
 * An intermediate class to give names for the different
 *
 * @property chars The characters to be used for creating boxes
 */
@JvmInline
value class CharSet(private val chars: String) {
    val vertical get() = chars[0]
    val horizontal get() = chars[1]
    val upLeft get() = chars[2]
    val upRight get() = chars[3]
    val downLeft get() = chars[4]
    val downRight get() = chars[5]
    val crossLeft get() = chars[6]
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

private const val SINGLE_CHARS = "│─┌┐└┘├┤"

/**
 * Wraps a single-outline box around the receiving string.
 *
 * @receiver A [String], preferably of length 1 or more, possibly containing multiple newline characters
 *
 * @return A copy of the string with a box around it
 */
fun String.wrapSingleBox() =
    wrapBox(CharSet(SINGLE_CHARS))

private const val ROUND_CHARS = "│─╭╮╰╯├┤"

/**
 * Wraps a single-outline rounded-corner box around the receiving string.
 *
 * @receiver A [String], preferably of length 1 or more, possibly containing multiple newline characters
 *
 * @return A copy of the string with a rounded box around it
 */
fun String.wrapRoundBox() =
    wrapBox(CharSet(ROUND_CHARS))

private const val DOUBLE_CHARS = "║═╔╗╚╝╠╣"

/**
 * Wraps a double-outline box around the receiving string.
 *
 * @receiver A [String], preferably of length 1 or more, possibly containing multiple newline characters
 *
 * @return A copy of the string with a double box around it
 */
fun String.wrapDoubleBox() =
    wrapBox(CharSet(DOUBLE_CHARS))
