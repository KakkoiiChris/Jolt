/********************************************
 * ::::::::::: ::::::::  :::    ::::::::::: *
 *     :+:    :+:    :+: :+:        :+:     *
 *     +:+    +:+    +:+ +:+        +:+     *
 *     +#+    +#+    +:+ +#+        +#+     *
 *     +#+    +#+    +#+ +#+        +#+     *
 * #+# #+#    #+#    #+# #+#        #+#     *
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
 * @property text The content of the source
 */
data class Source(val name: String, val text: String) {
    companion object {
        /**
         * Creates a [Source] instance from the given NIO file path.
         *
         * @param path The path of the file to load
         */
        fun of(path: Path): Source {
            val name = path.name
            val text = Files.readString(path)

            return Source(name, text)
        }
    }
}
