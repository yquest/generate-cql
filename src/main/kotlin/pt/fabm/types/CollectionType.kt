package pt.fabm.types

interface CollectionType : Type {
    val collectionName: String
    val collectionValue: Type
    override val entry: Pair<String, Any> get() = collectionName to collectionValue

    companion object {
        fun toCollectionType(typeSupplier: (String) -> Any?): CollectionType? {
            for (current in listOf("set", "list")) {
                val simple = (current.let { typeSupplier(it) } ?: continue)
                    .let { if (it is String) it else null } ?: return null

                val value = SimpleType.fromString(simple)

                when (current) {
                    "set" -> return SetType(value)
                    "list" -> return ListType(value)
                }
            }
            return null
        }
    }
}