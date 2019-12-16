package pt.fabm

import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType
import pt.fabm.types.Type

interface WithFields {
    val fields: MutableList<Field>
    val dependecies: Set<CustomType>
        get() {
            val siblingDependencies = fields
                .map { it.type }
                .filterIsInstance<CustomType>()

            val all = mutableSetOf<CustomType>()

            if (this is CustomType) all.add(this)
            for (current in siblingDependencies) {
                all.add(current)
                val children = current.fields.flatMap {
                    if (it.type !is CustomType && !all.contains(it.type)) return@flatMap emptyList<CustomType>()
                    else return@flatMap listOf(it.type as CustomType)
                }

                for (child in children) {
                    all.add(child)
                }
            }

            return all
        }

    fun simpleField(name: String, type: SimpleType.Type, key: Field.KeyType = Field.KeyType.NONE) {
        fields.add(Field(name, type.asType(), key))
    }

    fun field(name: String, type: Type, key: Field.KeyType = Field.KeyType.NONE) {
        fields.add(Field(name, type, key))
    }
}