package pt.fabm.types

interface CollectionType : Type {
    val collectionName: String
    val collectionValue: Type
    override val entry: Pair<String, Any> get() = collectionName to collectionValue

    companion object {
        fun toCollectionType(typeSupplier: (String) -> Any?): CollectionType? {
            for (current in listOf("set", "list")) {
                val rawField = (typeSupplier(current) ?: continue)
                    .let { if(it !is Map<*,*>)throw error("expected map") else it }

                val type = Type.fromSupplier {
                    rawField[it]
                }

                when (current) {
                    "set" -> return SetType(type)
                    "list" -> return ListType(type)
                }
            }
            return null
        }
    }
}