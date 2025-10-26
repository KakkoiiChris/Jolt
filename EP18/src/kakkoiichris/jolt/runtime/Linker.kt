package kakkoiichris.jolt.runtime

import kakkoiichris.jolt.JoltUnit
import kakkoiichris.jolt.JoltValue
import kakkoiichris.jolt.joltError
import kakkoiichris.jolt.parser.Expr

typealias Method = (Runtime, Linker.LinkData) -> JoltValue<*>

object Linker {
    private val links = mutableMapOf<String, Link>()

    init {
        addLink("print", 1) { _, data ->
            val (x) = data.args

            print(x)

            JoltUnit
        }

        addLink("println", 1) { _, data ->
            val (x) = data.args

            println(x)

            JoltUnit
        }
    }

    fun addLink(name: String, arity: Int = 0, method: Method) {
        if (name in links) {
            joltError("Duplicate link for function '$name'")
        }

        links[name] = Link(arity, method)
    }

    operator fun get(name: Expr.Name) =
        links[name.value]

    data class Link(val arity: Int, val method: Method) {
        operator fun invoke(runtime: Runtime, args: List<JoltValue<*>>): JoltValue<*> {
            val linkData = LinkData(args)

            return method(runtime, linkData)
        }
    }

    data class LinkData(val args: List<JoltValue<*>>)
}