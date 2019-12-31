package pt.fabm

import pt.fabm.types.*

class ExampleInsertGeneratorSingleLine(
    private val appendable: Appendable,
    private val collectionRepetition: () -> Boolean = { false },
    private val generator: (SimpleType) -> String
) {

    fun generateByCustomType(customType: CustomType) {
        appendable.append("{")
        val fieldsIterator = customType.fields.iterator()
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            appendable.append(field.name).append(" : ")
            generateByType(field.type)
            if (fieldsIterator.hasNext()) appendable.append(", ")
        }
        appendable.append("}")
    }

    fun generateByType(type: Type) {
        when (type) {
            is SimpleType -> appendable.append(generator(type))
            is MapType -> generateMap(type.key, type.value)
            is SetType -> generateSet(type.collectionValue)
            is ListType -> generateList(type.collectionValue)
            is CustomType -> generateByCustomType(type)
            else -> error("type not defined yet")
        }
    }

    fun generateSet(element: Type) {
        var repetition: Boolean
        appendable.append("{")
        do {
            generateByType(element)
            repetition = collectionRepetition()
            if(repetition) appendable.append(", ")
        } while (repetition)
        appendable.append("}")
    }

    fun generateList(element: Type) {
        var repetition: Boolean
        appendable.append("[")
        do {
            generateByType(element)
            repetition = collectionRepetition()
            if(repetition) appendable.append(", ")
        } while (repetition)
        appendable.append("]")
    }

    fun generateMap(key: Type, value: Type) {
        var repetition: Boolean
        appendable.append("{")
        do {
            generateByType(key)
            appendable.append(": ")
            generateByType(value)
            repetition = collectionRepetition()
            if(repetition) appendable.append(", ")
        } while (repetition)
        appendable.append("}")
    }

    fun generateFrozen(type: Type) {
        generateByType(type)
    }

    fun generateInsert(table: Table) {
        val oderedFields = table.orderedFields
        var fieldsIterator = oderedFields.iterator()
        appendable.append("insert into ${table.name} (")
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val comma = if (fieldsIterator.hasNext()) ", " else ""
            appendable.append("${field.name}$comma")
        }
        appendable.append(") into (")

        fieldsIterator = oderedFields.iterator()
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val comma = if (fieldsIterator.hasNext()) ", " else ""
            generateByType(field.type)
            appendable.append(comma)
        }
        appendable.append(");")
    }
}