package pt.fabm.types

class SetType(private val type: Type) : CollectionType {
    override val collectionName: String
        get() = "set"
    override val collectionValue: Type
        get() = type

    override val literalName: String get() = "set<${type.literalName}>"
    override val entry: Pair<String, Any>
        get() = "set" to listOf(type.entry).toMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetType

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

}