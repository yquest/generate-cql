package pt.fabm

import org.apache.commons.lang3.RandomStringUtils
import pt.fabm.types.MapType
import pt.fabm.types.SimpleType
import pt.fabm.types.Type
import kotlin.random.Random

class ExampleGenerator {

    fun generateSimpleType(type: SimpleType): String {
        val integerType = type.type == SimpleType.Type.DATE ||
                type.type == SimpleType.Type.INT ||
                type.type == SimpleType.Type.TEXT

        return if (integerType)
            Random.nextInt(0, 10000).toString()
        else if (type.type == SimpleType.Type.TEXT)
            "'${RandomStringUtils.randomAlphanumeric(30)}'"
        else "'not defined yet generation'"
    }

    fun generateByType(type: Type): String {
        return if (type is SimpleType) generateSimpleType(type)
        else if(type is MapType) generateMap(type.key, type.value)
        else "'not defined generation'"
    }

    fun generateMap(key: Type, value: Type): String {
        return "{${generateByType(key)}:${generateByType(value)}}"
    }
}