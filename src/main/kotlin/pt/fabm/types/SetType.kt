package pt.fabm.types

class SetType(private val type: Type) : CollectionType {
    override val collectionName: String
        get() = "set"
    override val collectionValue: Type
        get() = type

    override val literalName: String get() = "set<${type.literalName}>"
    override val entry: Pair<String, Any> get() = "set" to type.entry
}