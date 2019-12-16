package pt.fabm.types

class SetType(val type: Type) : Type {
    enum class Type {
        TEXT, INT, UUID, DATE, TIMESTAMP;

        fun asType(): pt.fabm.types.Type = SetType(this)
    }

    override val literalName: String get() = type.name.toLowerCase()
}