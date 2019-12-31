package pt.fabm.types

import pt.fabm.DDLAble
import pt.fabm.Field
import pt.fabm.WithFields

class CustomType(val name: String) : Type, WithFields, DDLAble {
    override val literalName: String get() = name
    override val entry: Pair<String, String> get() = "custom" to name
    val map: Map<String, Any>
        get() {
            val map = mutableMapOf<String, Any>()
            map["name"] = this.name
            if (fields.isNotEmpty()) map["fields"] = fields.map {
                val fieldMap = mutableMapOf<String, Any>()
                when (it.type) {
                    is SimpleType -> fieldMap["type"] = it.type.literalName
                    is CustomType -> fieldMap["custom"] = it.type.literalName
                    is CollectionType -> fieldMap[it.type.collectionName] =
                        it.type.collectionValue.literalName
                    else -> throw error("impossible to map type")
                }
                it.name to fieldMap
            }.toMap()
            return map
        }
    override val fields: MutableList<Field> = mutableListOf()


    companion object {
        fun fromRawTypes(rawTypes: List<*>): List<CustomType> {
            val types = mutableListOf<CustomType>()
            return rawTypes.map { rawType ->
                if (rawType !is Map<*, *>) throw error("expected map")
                val customType = CustomType(rawType["name"] as String)
                for (rawField in rawType["fields"] as Map<*, *>) {
                    customType.fields += Field.fromSupplier(
                        rawField.key as String,
                        rawField.value ?: throw error("expected map"),
                        types
                    )
                }
                types += customType
                customType
            }.reversed()
        }

        fun fromYaml(types: Iterable<CustomType>, typeSupplier: (String) -> Any?): CustomType? {
            val customName = (typeSupplier("custom") ?: return null)
                .let { if (it is String) it else null } ?: return null
            return types.find { it.name == customName } ?: return null
        }

        fun fromYaml(map: Map<*, *>): List<CustomType> {
            val typesRaw = (map["types"] ?: throw error("types entry expected"))
                .let { if (it !is List<*>) throw error("a list is expected") else it }
            val customTypes = mutableListOf<CustomType>()
            return typesRaw.reversed().map { rawType ->
                if (rawType !is Map<*, *>) throw error("expected map")
                val customType = CustomType(rawType["name"] as String)
                for (rawField in rawType["fields"] as Map<*, *>) {
                    customType.fields += Field.fromSupplier(
                        rawField.key as String,
                        rawField.value ?: throw error("expected map"),
                        customTypes
                    )
                }
                customTypes += customType
                customType
            }
        }

        fun name(name: String, init: CustomType.() -> Unit): CustomType {
            val customType = CustomType(name)
            customType.init()
            return customType
        }
    }

    override fun printDDL(appendable: Appendable) {
        appendable.append("create type ").append(name).append("(\n")
        val iterator = fields.iterator()
        while (iterator.hasNext()) {
            val field = iterator.next()
            appendable
                .append("  ")
                .append(field.name)
                .append(" ")
                .append(field.type.literalName)
            if(iterator.hasNext()) appendable.append(',')
            appendable.append('\n')
        }
        appendable.append(");\n")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomType

        if (name != other.name) return false
        if (fields != other.fields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }

    override fun toString(): String {
        return "CustomType(name='$name', fields=$fields)"
    }

}