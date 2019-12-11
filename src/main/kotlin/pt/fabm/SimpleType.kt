package pt.fabm

class SimpleType(val type: Type) : Type {
    enum class Type {
        TEXT, INT, UUID, DATE, TIMESTAMP;

        fun asType(): pt.fabm.Type = SimpleType(this)
    }

    override val literalName: String get() = type.name.toLowerCase()
}