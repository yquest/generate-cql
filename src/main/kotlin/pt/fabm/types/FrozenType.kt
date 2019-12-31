package pt.fabm.types

class FrozenType(val type: Type) : Type {
    override val literalName: String
        get() = "frozen<${type.literalName.toLowerCase()}>"
    override val entry: Pair<String, Any>
        get() = "frozen" to listOf(type.entry).toMap()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrozenType

        if (type != other.type) return false

        return true
    }

    companion object{
        fun fromYaml(customTypes: Iterable<CustomType>,typeSupplier:(entry: String) -> Any?):Type?{
            val rawType = typeSupplier("frozen")?:return null
            if(rawType !is Map<*,*>) throw error("expected map")
            return FrozenType(
                Type.fromSupplier(customTypes){
                    rawType[it]
                }
            )
        }
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return "FrozenType(type=$type)"
    }

}