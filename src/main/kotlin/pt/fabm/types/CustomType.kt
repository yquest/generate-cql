package pt.fabm.types

import pt.fabm.Field
import pt.fabm.WithFields

class CustomType(val name: String) : Type, WithFields {
    override val literalName: String get() = name
    override val map: Map<String, *>
        get() {
            val map = hashMapOf<String, Any>()
            map["name"] = this.name
            if (fields.isNotEmpty()) map["fields"] = fields.map {
                val fieldMap = mutableMapOf<String, Any>()
                if (it.type is SimpleType) fieldMap["type"] = it.type.literalName
                else if (it.type is CustomType) fieldMap["custom"] = it.type.literalName
                else if (it.type is CollectionType) fieldMap[it.type.collectionName] =
                    it.type.collectionValue.literalName
                else throw error("impossible to map type")
                it.name to fieldMap
            }.toMap()
            return map
        }
    override val fields: MutableList<Field> = mutableListOf()

    companion object {
        fun fromYaml(types: List<CustomType>, map: Map<*, *>): CustomType? {
            val type = map["custom"]
            if (type == null) return null
            return types.find { it.name == type.toString() }
        }

        fun name(name: String, init: CustomType.() -> Unit): CustomType {
            val customType = CustomType(name)
            customType.init()
            return customType
        }
    }
}