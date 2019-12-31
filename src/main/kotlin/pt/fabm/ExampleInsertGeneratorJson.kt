package pt.fabm

import org.apache.commons.lang3.RandomStringUtils
import pt.fabm.types.*
import java.util.*
import kotlin.random.Random

class ExampleInsertGeneratorJson(
    private val appendable: Appendable,
    private val simpleGenerator: (Boolean, SimpleType) -> String = ::generateSimple,
    private val repeatInCollection: () -> Boolean = { false }
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
        if (type is SimpleType) appendable.append(simpleGenerator(false, type))
        else if (type is MapType) generateMap(ident, type.key, type.value)
        else if (type is CustomType) generateByCustomType(ident, type)
        else if (type is FrozenType) generateFrozen(ident, type)
        else if (type is CollectionType) generateCollection(ident, type)
        else error("type not defined yet")
    }

    fun generateCollection(ident: String, type: CollectionType) {
        appendable.append("[")
        do {
            val repeat = repeatInCollection()
            generateByType(ident, type.collectionValue)
            if (repeat) appendable.append(',')
        } while (repeat)
        appendable.append("]")
    }

    fun generateMap(ident: String, key: Type, value: Type) {
        appendable.append('{').append('"')
        do {
            val repeat = repeatInCollection()
            if (key is SimpleType) appendable.append('"')
                .append(simpleGenerator(true, key))
                .append('"')
            else throw error("expected simple type")
            generateByType(ident, value)
            if (repeat) appendable.append(',')
        } while (repeat)
        appendable.append('"').append('}')
        appendable.append("{\"${generateByType("", key)}\": ${generateByType(ident, value)}}")
    }

    fun generateFrozen(ident: String, type: FrozenType) {
        generateByType(ident, type.type)
    }

    fun generateInsert(table: Table) {
        val oderedFields = table.orderedFields
        val fieldsIterator = oderedFields.iterator()
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

    companion object {
        fun applySimple(text: String, type: SimpleType): String {
            return when (type.type) {
                SimpleType.Type.DATE -> '"' + text + '"'
                SimpleType.Type.TIMESTAMP -> '"' + text + '"'
                SimpleType.Type.INT -> text
                SimpleType.Type.TEXT -> '"' + text + '"'
                SimpleType.Type.UUID -> '"' + text + '"'
            }
        }

        fun generateSimple(rawMode: Boolean, type: SimpleType): String {
            fun randomInt() = Random.nextInt(0, 10000).toString()
            fun randomText() = RandomStringUtils.randomAlphanumeric(30)
            fun randomUUID() = UUID.randomUUID().toString()

            val fn = when (type.type) {
                SimpleType.Type.DATE -> ::randomInt
                SimpleType.Type.TIMESTAMP -> ::randomInt
                SimpleType.Type.INT -> ::randomInt
                SimpleType.Type.TEXT -> ::randomText
                SimpleType.Type.UUID -> ::randomUUID
            }

            return if (rawMode) fn()
            else applySimple(fn(), type)
        }
    }
}