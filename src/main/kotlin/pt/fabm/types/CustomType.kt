package pt.fabm.types

import pt.fabm.Field
import pt.fabm.WithFields

class CustomType(val name: String) : Type, WithFields {
    override val literalName: String get() = name
    override val fields: MutableList<Field> = mutableListOf()

    companion object {
        fun name(name: String, init: CustomType.() -> Unit): CustomType {
            val customType = CustomType(name)
            customType.init()
            return customType
        }
    }

    fun toMap(): Map<String, Any> {
        val map = hashMapOf<String, Any>()
        map["name"] = this.name
        if (fields.isNotEmpty()) map["fields"] = fields.map {
            val fieldMap = mutableMapOf<String, Any>()
            fieldMap["type"] = it.type.literalName
            it.name to fieldMap
        }.toMap()
        return map
    }
}