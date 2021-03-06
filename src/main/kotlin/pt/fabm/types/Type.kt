package pt.fabm.types

interface Type {

    val literalName: String
    val entry: Pair<String, Any>
    fun asFrozen(): FrozenType = FrozenType(this)

    companion object {

        fun fromSupplier(
            customTypes: Iterable<CustomType> = emptyList(),
            typeSupplier: (entry: String) -> Any?
        ): Type =
            SimpleType.fromYaml(typeSupplier) ?:
            CollectionType.toCollectionType(typeSupplier) ?:
            CustomType.fromYaml(customTypes,typeSupplier) ?:
            FrozenType.fromYaml(customTypes,typeSupplier) ?:
            throw error("type not expected")
    }
}
