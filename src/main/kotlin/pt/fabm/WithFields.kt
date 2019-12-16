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
                .toSet()

            val all = mutableSetOf<CustomType>()

            if (this is CustomType) {
                if (!all.add(this)) {
                    all.remove(this)
                    all.add(this)
                }
            }
            return orderDependencies(siblingDependencies)
        }

    fun simpleField(name: String, type: SimpleType.Type, key: Field.KeyType = Field.KeyType.NONE) {
        fields.add(Field(name, type.asType(), key))
    }

    fun field(name: String, type: Type, key: Field.KeyType = Field.KeyType.NONE) {
        fields.add(Field(name, type, key))
    }

    companion object {
        fun printDependencies(dependencies: Set<CustomType>, appendable: Appendable) {
            for (customType in dependencies.reversed()) {
                appendable.append("create type ${customType.name}(\n")
                val fieldsIterator = customType.fields.iterator()
                while (fieldsIterator.hasNext()) {
                    val field = fieldsIterator.next()
                    appendable.append("  ${field.name} ${field.type.literalName}")
                    if (fieldsIterator.hasNext()) appendable.append(',')
                    appendable.append("\n")
                }
                appendable.append(");\n")
            }

        }

        fun orderDependencies(dependencies: Set<CustomType>): Set<CustomType> {

            val all = mutableSetOf<CustomType>()

            for (current in dependencies) {
                if (!all.add(current)) {
                    all.remove(current);
                    all.add(current);
                }
                for (child in current.dependecies) {
                    if (!all.add(child)) {
                        all.remove(child);
                        all.add(child);
                    }
                }
            }

            return all
        }
    }
}