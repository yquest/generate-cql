package pt.fabm.types

import pt.fabm.Field
import pt.fabm.WithFields

class CustomType(val name: String) : Type, WithFields {
    override val literalName: String get() = name
    override val entry:Pair<String,String> get() = "custom" to name
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
        fun fromYaml(types: List<CustomType>, typeSupplier: (String) -> Any?): CustomType? {
            val customName = (typeSupplier("custom") ?: return null)
                .let { if (it is String) it else null } ?: return null
            return types.find { it.name == customName } ?: return null
        }

        fun name(name: String, init: CustomType.() -> Unit): CustomType {
            val customType = CustomType(name)
            customType.init()
            return customType
        }
    }
}