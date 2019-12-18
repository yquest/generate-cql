package pt.fabm.types

class ListType(val type: Type) : CollectionType {
    override val collectionName: String = "list"
    override val collectionValue: Type = type
    override val literalName: String get() = "list<${type.literalName.toLowerCase()}>"
}