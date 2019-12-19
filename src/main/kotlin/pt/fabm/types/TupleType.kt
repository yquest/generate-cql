package pt.fabm.types

class TupleType(val types: List<SimpleType>) : Type {
    private val literalTypes: String get() = types.map { it.literalName.toLowerCase() }.joinToString(",")
    override val literalName: String get() = "tuple<${literalTypes}>"
    override val map: Map<String, Any>
        get() = mapOf("tuple" to listOf(literalTypes))
}