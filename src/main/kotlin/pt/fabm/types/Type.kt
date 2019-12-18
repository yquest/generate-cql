package pt.fabm.types

interface Type {
    val literalName: String
    val map: Map<String, *>
    fun asFrozen(): FrozenType = FrozenType(this)

    companion object {

        fun fromYaml(map: Map<*, *>, customTypes:List<CustomType> = emptyList()): Type {
            return SimpleType.fromYaml(map) ?:
            CollectionType.toCollectionType(map) ?:
            CustomType.fromYaml(customTypes, map) ?:
                    throw error("type not expected")
        }
    }
}