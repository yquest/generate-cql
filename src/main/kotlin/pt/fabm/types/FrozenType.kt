package pt.fabm.types

class FrozenType(val type: Type) : Type {
    override val literalName: String
        get() = "frozen<${type.literalName.toLowerCase()}>"
    override val entry: Pair<String, Any>
        get() = "frozen" to type.entry
}