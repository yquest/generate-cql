package pt.fabm

import org.apache.commons.lang3.RandomStringUtils
import pt.fabm.types.CustomType
import pt.fabm.types.MapType
import pt.fabm.types.SimpleType
import pt.fabm.types.Type
import java.util.*
import kotlin.random.Random

class ExampleInsertGeneratorSingleLine(private val appendable: Appendable) {

    fun generateSimpleType(type: SimpleType) {
        val integerType = type.type == SimpleType.Type.DATE ||
                type.type == SimpleType.Type.INT

        if (integerType) appendable.append(Random.nextInt(0, 10000).toString())
        else if (type.type == SimpleType.Type.TEXT)
            appendable.append("'${RandomStringUtils.randomAlphanumeric(30)}'")
        else if (type.type == SimpleType.Type.UUID) appendable.append(UUID.randomUUID().toString())
        else throw error("not defined yet generation")
    }

    fun generateByCustomType(customType: CustomType) {
        appendable.append("{")
        val fieldsIterator = customType.fields.iterator()
        while (fieldsIterator.hasNext()){
            val field = fieldsIterator.next()
            appendable.append(field.name).append(" : ")
            generateByType(field.type)
            if(fieldsIterator.hasNext()) appendable.append(", ")
        }
        appendable.append("}")
    }

    fun generateByType(type: Type) {
        if (type is SimpleType) generateSimpleType(type)
        else if (type is MapType) generateMap(type.key, type.value)
        else if (type is CustomType) generateByCustomType(type)
        else error("type not defined yet")
    }

    fun generateSet(element: Type) {
        appendable.append("{${generateByType(element)},${generateByType(element)}}")
    }

    fun generateList(element: Type) {
        appendable.append("[${generateByType(element)},${generateByType(element)}]")
    }

    fun generateMap(key: Type, value: Type) {
        appendable.append("{${generateByType(key)} : ${generateByType(value)}}")
    }

    fun generateFrozen(type: Type){
        generateByType(type)
    }

    fun generateInsert(table:Table){
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