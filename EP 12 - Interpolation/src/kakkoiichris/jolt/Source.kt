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

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

/**
 * A wrapper class used to represent an origin-agnostic code source.
 *
 * @property name The simple name of the source
 * @property text The textual content of the source
 */
data class Source(val name: String, val text: String) {
    companion object {
        /**
         * @param path The path of the file to load
         *
         * @return A [Source] instance from the given NIO file path
         */
        fun of(path: Path): Source {
            val name = path.name
            val text = Files.readString(path)

            return Source(name, text)
        }
    }

    /**
     * @param row The row to retrieve
     *
     * @return The line of text, of the source code, at the given row
     */
    fun getLine(row: Int) =
        text.lines()[row - 1]
}
