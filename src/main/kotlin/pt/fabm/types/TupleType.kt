package pt.fabm.types

class TupleType(val type: Type) : Type {
    override val literalName: String get() = "tuple<${type.literalName.toLowerCase()}>"
}