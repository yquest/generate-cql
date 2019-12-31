package pt.fabm

import pt.fabm.types.CustomType
import pt.fabm.types.Type

class Field(val name: String, val type: Type, val pkType: KeyType = KeyType.NONE, val order: Int = -1) {
    enum class KeyType {
        CLUSTER, PARTITION, NONE
    }

    companion object {
        fun fromSupplier(name: String, fieldRaw: Any, types: Iterable<CustomType>): Field {
            if (fieldRaw !is Map<*, *>) error("expect a map")

            val type = Type.fromSupplier(types) {fieldRaw[it]}

            val order = (fieldRaw["order"] ?: -1).let {
                if (it is Int) it else error("Int expected")
            }
            val key = fieldRaw["key"].let {
                when (it) {
                    null -> KeyType.NONE
                    !is String -> error("Expected String")
                    else -> KeyType.values().find { keyType -> it.toUpperCase() == keyType.name }
                        ?: error("key $it invalid")
                }
            }
            return Field(name, type, key, order)
        }

    }

    fun toPairMap(): Pair<String, Map<String, Any>> {
        val entries = listOfNotNull(
            type.entry,
            if (order == -1) null else "order" to order,
            if (pkType == KeyType.NONE) null else "key" to pkType.name.toLowerCase()
        )
        return name to entries.toMap()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Field

        if (name != other.name) return false
        if (type != other.type) return false
        if (pkType != other.pkType) return false
        if (order != other.order) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + pkType.hashCode()
        result = 31 * result + order
        return result
    }

    override fun toString(): String {
        return "Field(name='$name', type=$type, pkType=$pkType, order=$order)"
    }


}