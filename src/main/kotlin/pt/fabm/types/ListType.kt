package pt.fabm.types

class ListType(val type: Type) : CollectionType {
    override val collectionName: String = "list"
    override val collectionValue: Type = type
    override val literalName: String get() = "list<${type.literalName.toLowerCase()}>"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListType

        if (type != other.type) return false
        if (collectionName != other.collectionName) return false
        if (collectionValue != other.collectionValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + collectionName.hashCode()
        result = 31 * result + collectionValue.hashCode()
        return result
    }

}