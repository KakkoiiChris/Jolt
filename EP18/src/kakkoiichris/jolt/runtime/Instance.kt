package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.JoltInstance
import kakkoiichris.jolt.parser.Stmt

class Instance(val `class`: Stmt.Class, val scope: Memory.Scope) {
    private val memory = Memory()

    init {
        scope.declare(true, "this", JoltInstance(this))

        memory.push(scope)
    }

    operator fun get(name: String) =
        memory[name]
}