package pt.fabm.types

class SimpleType(val type: Type) : Type {
    companion object {
        fun fromYaml(map: Map<*, *>): SimpleType? {
            val type = map["key"]
            if (type == null) return null
            else return fromString(type.toString())
        }

        fun fromString(string: String): SimpleType {
            return (Type.values().find { it.name == string.toUpperCase() }
                ?: throw error("no simpleType '$string' ")).asType()
        }
    }

    enum class Type {
        TEXT, INT, UUID, DATE, TIMESTAMP;

        fun asType(): SimpleType = SimpleType(this)
    }

    override val literalName: String get() = type.name.toLowerCase()
    override val map: Map<String, *> get() = mapOf("type" to literalName)
}