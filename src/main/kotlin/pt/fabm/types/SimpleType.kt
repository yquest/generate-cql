package pt.fabm.types

class SimpleType(val type: Type) : Type {
    companion object {

        fun fromYaml(keyProvider: (String) -> Any?): SimpleType? {
            val key = keyProvider("type") ?: return null
            return fromString(key.toString())
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
    override val entry: Pair<String, Any> get() = "type" to literalName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleType

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return "SimpleType(type=$type)"
    }


}