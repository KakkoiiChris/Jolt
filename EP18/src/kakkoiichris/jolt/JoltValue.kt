package kakkoiichris.jolt

import kakkoiichris.jolt.parser.Expr
import kakkoiichris.jolt.parser.Stmt
import kakkoiichris.jolt.runtime.Instance
import kakkoiichris.jolt.runtime.Invocable
import kakkoiichris.jolt.runtime.Memory
import kotlin.math.floor

interface JoltValue<X> {
    val value: X

    val type: String

    val iterable: List<JoltValue<*>> get() = listOf(this)

    companion object {
        fun of(x: Any) = when (x) {
            Unit       -> JoltUnit

            is Boolean -> JoltBool(x)

            is Double  -> JoltNum(x)

            is String  -> JoltString(x)

            else       -> TODO("NOT A VALUE")
        }
    }
}

data object JoltUnit : JoltValue<Unit> {
    override val value = Unit

    override val type = "unit"
}

data class JoltBool(override val value: Boolean) : JoltValue<Boolean> {
    override val type = "bool"

    override fun toString() = value.toString()
}

data class JoltNum(override val value: Double) : JoltValue<Double> {
    override val type = "num"

    override val iterable get() = (0..<value.toInt()).map { JoltNum(it.toDouble()) }.toList()

    private fun truncate() = (
        if (value == floor(value))
            value.toInt()
        else
            value
        ).toString()

    override fun toString() = truncate()
}

data class JoltString(override val value: String) : JoltValue<String> {
    override val type = "string"

    override val iterable get() = value.toCharArray().map { JoltString(it.toString()) }.toList()

    override fun toString() = "\"$value\""
}

data class JoltList(override val value: MutableList<JoltValue<*>>) : JoltValue<MutableList<JoltValue<*>>> {
    override val type get() = "list"

    override val iterable get() = value.toList()

    override fun toString() = if (value.isNotEmpty())
        value.joinToString(prefix = "[", postfix = "]", separator = ", ") { it.toString() }
    else
        "[]"
}

data class JoltFun(override val value: Stmt.Fun, override val scope: Memory.Scope) : JoltValue<Stmt.Fun>, Invocable {
    override val type get() = "fun"

    override val name get() = value.name

    override val isVariadic get() = value.isVariadic

    override val params get() = value.params

    val isLinked get() = value.isLinked

    val body get() = value.body

    override fun toString() =
        "fun ${value.name}${value.params.joinToString(prefix = "(", postfix = ")", separator = ", ") { it.name.value }}"
}

data class JoltClass(override val value: Stmt.Class, override val scope: Memory.Scope) : JoltValue<Stmt.Class>,
    Invocable {
    override val type get() = "class"

    override val name get() = value.name

    override val isVariadic get() = value.isVariadic

    override val params get() = value.params

    val init get() = value.init

    override fun toString() =
        "class ${value.name}${
            value.params.joinToString(
                prefix = "(",
                postfix = ")",
                separator = ", "
            ) { it.name.value }
        }"
}

data class JoltInstance(override val value: Instance) : JoltValue<Instance> {
    override val type get() = "instance"

    operator fun get(name: Expr.Name) =
        value[name.value]

    operator fun set(name: Expr.Name, value: JoltValue<*>) {
        this.value.scope[name.value]?.value = value
    }

    override fun toString() =
        "instance of ${value.`class`.name.value}${
            value.scope.entries.joinToString(
                prefix = "(",
                postfix = ")",
                separator = ", "
            ) { "${it.key} = ${it.value.value}" }
        }"
}