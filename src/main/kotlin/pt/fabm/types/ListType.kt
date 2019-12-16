package pt.fabm.types

class ListType(val type: Type) : Type {
    override val literalName: String get() = "list<${type.literalName.toLowerCase()}>"
}