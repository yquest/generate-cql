package pt.fabm

import pt.fabm.types.*

class ExampleInsertGeneratorJson(
    private val appendable: Appendable,
    private val collectionRepetition: () -> Boolean = { false },
    private val generator: (SimpleType) -> String
) {

    fun generateByCustomType(ident: String, customType: CustomType) {
        appendable.append("{\n")
        val fieldsIterator = customType.fields.iterator()
        val reident = "  $ident"
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            appendable.append("$reident\"${field.name}\"").append(": ")
            generateByType(reident, field.type)
            if (fieldsIterator.hasNext()) appendable.append(",")
            appendable.append("\n")
        }
        appendable.append(ident).append("}")
    }

    fun generateByType(ident: String, type: Type) {
        when (type) {
            is SimpleType -> appendable.append(generator(type))
            is CollectionType -> generateCollection(ident, type.collectionValue)
            is MapType -> generateMap(ident, type.key, type.value)
            is CustomType -> generateByCustomType(ident, type)
            else -> error("type not defined yet")
        }
    }

    fun generateCollection(ident: String, element: Type) {
        var repetition: Boolean
        appendable.append("[")
        do {
            generateByType(ident, element)
            repetition = collectionRepetition()
            if (repetition) appendable.append(", ")
        } while (repetition)
        appendable.append("]")
    }

    fun generateMap(ident: String, key: Type, value: Type) {
        val reident = "  $ident"
        var repetition: Boolean
        appendable.append("{")
        do {
            appendable.append('\n')
            appendable.append(reident)
            if (key is SimpleType)
                appendable.append(generator(key))
            else
                appendable.append(generator(SimpleType.Type.TEXT.asType()))
            appendable.append(": ")

            generateByType(reident, value)
            repetition = collectionRepetition()
            if (repetition) appendable.append(",")
        } while (repetition)
        appendable.append('\n').append(ident).append("}")
    }

    fun generateFrozen(ident: String, type: Type) {
        generateByType(ident, type)
    }

    fun generateInsert(table: Table) {
        val orderedFields = table.orderedFields
        val fieldsIterator = orderedFields.iterator()
        appendable.append("insert into ${table.name} json '{\n")
        val ident = "  "
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val comma = if (fieldsIterator.hasNext()) ",\n" else ""
            appendable.append("$ident\"${field.name}\": ")
            generateByType(ident, field.type)
            appendable.append(comma)
        }
        appendable.append('\n').append("}';")
    }
}