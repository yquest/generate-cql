package pt.fabm.types

class FrozenType(val type: Type) : Type {
    override val literalName: String
        get() = "frozen<${type.literalName.toLowerCase()}>"
    override val map: Map<String, *>
        get() = mapOf("frozen" to type.map)
}