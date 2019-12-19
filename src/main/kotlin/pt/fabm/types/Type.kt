package pt.fabm.types

interface Type {

    val literalName: String
    val map: Map<String, Any>
    fun asFrozen(): FrozenType = FrozenType(this)

    companion object {

        fun fromYaml(typeSupplier: (entry:String)->Any?, customTypes: List<CustomType> = emptyList()): Type =
            SimpleType.fromYaml(typeSupplier) ?:
            CollectionType.toCollectionType(typeSupplier) ?:
            CustomType.fromYaml(customTypes, typeSupplier) ?:
                throw error("type not expected")
    }
}
