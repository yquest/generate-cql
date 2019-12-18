package pt.fabm.types

interface CollectionType : Type {
    val collectionName: String
    val collectionValue: Type
    override val map :Map<String, *> get() = mapOf(collectionName to collectionValue)

    companion object {
        fun toCollectionType(map: Map<*, *>): CollectionType? {
            val type = listOf("set", "list").find { map.containsKey(it) } ?: return null
            fun createType(): Type {
                val value = map[type]
                return if (value is String) SimpleType.fromString(value)
                else if (value is Map<*, *>) Type.fromYaml(value)
                else throw error("no way!")

            }
            when (type) {
                "set" -> return SetType(createType())
                "list" -> return ListType(createType())
                else -> throw error("what??!!")
            }
        }
    }
}