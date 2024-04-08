/********************************************
 * ::::::::::: ::::::::  :::    ::::::::::: *
 *     :+:    :+:    :+: :+:        :+:     *
 *     +:+    +:+    +:+ +:+        +:+     *
 *     +#+    +#+    +:+ +#+        +#+     *
 *     +#+    +#+    +#+ +#+        +#+     *
 * #+# #+#    #+#    #+# #+#        #+#     *
 *            Scripting Language            *
 ********************************************/
package kakkoiichris.jolt.parser

/**
 * A container for all parsed structures from a source file.
 *
 * @property exprs The expressions of this program
 */
class Program(private val exprs: Exprs) : Iterator<Expr> by exprs.iterator()