package pt.fabm

import org.apache.commons.lang3.RandomStringUtils
import pt.fabm.types.CustomType
import pt.fabm.types.MapType
import pt.fabm.types.SimpleType
import pt.fabm.types.Type
import java.util.*
import kotlin.random.Random

class ExampleInsertGeneratorJson(private val appendable: Appendable) {

    fun generateSimpleType(type: SimpleType) {
        val integerType = type.type == SimpleType.Type.DATE ||
                type.type == SimpleType.Type.INT

        if (integerType) appendable.append(Random.nextInt(0, 10000).toString())
        else if (type.type == SimpleType.Type.TEXT)
            appendable.append("\"${RandomStringUtils.randomAlphanumeric(30)}\"")
        else if (type.type == SimpleType.Type.UUID) appendable.append("\"${UUID.randomUUID()}\"")
        else throw error("not defined yet generation")
    }

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
        if (type is SimpleType) generateSimpleType(type)
        else if (type is MapType) generateMap(ident, type.key, type.value)
        else if (type is CustomType) generateByCustomType(ident, type)
        else error("type not defined yet")
    }

    fun generateCollection(ident: String, element: Type) {
        appendable.append("[${generateByType(ident, element)}]")
    }

    fun generateList(ident: String, element: Type) {
        appendable.append("[${generateByType(ident, element)},${generateByType(ident, element)}]")
    }

    fun generateMap(ident: String, key: Type, value: Type) {
        appendable.append("{\"${generateByType("", key)}\": ${generateByType(ident, value)}}")
    }

    fun generateFrozen(ident: String, type: Type) {
        generateByType(ident, type)
    }

    fun generateInsert(table: Table) {
        val oderedFields = table.orderedFields
        var fieldsIterator = oderedFields.iterator()
        appendable.append("insert into ${table.name} json '{\n")
        val ident="  "
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