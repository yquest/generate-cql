package pt.fabm.types

class SimpleType(val type: Type) : Type {
    enum class Type {
        TEXT, INT, UUID, DATE, TIMESTAMP;

        fun asType(): pt.fabm.types.Type = SimpleType(this)
    }

    override val literalName: String get() = type.name.toLowerCase()
}