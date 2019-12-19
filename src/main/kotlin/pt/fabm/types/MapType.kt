package pt.fabm.types

class MapType(val key: Type, val value: Type) : Type {
    override val literalName: String
        get() = "map<${key.literalName.toLowerCase()}, ${value.literalName.toLowerCase()}>"
    override val map: Map<String, Any>
        get() = mapOf("map" to listOf(key.literalName, value.literalName))

    companion object {
        fun fromYaml(map: Map<String, Any>): MapType? {
            val type = map["map"] ?: return null
            if (type !is Map<*,*>) throw error("expected a list")
            val rawKey = (type[0] ?: return null).toString()
            val rawValue = (type[1] ?: return null).toString()
            return MapType(SimpleType.fromString(rawKey),SimpleType.fromString(rawValue))
        }
    }
}