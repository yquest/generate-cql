package pt.fabm

import org.apache.commons.lang3.RandomStringUtils
import pt.fabm.types.SimpleType
import java.util.*
import kotlin.random.Random

interface SimpleTypeGenerator {

    companion object {

        val JSON = { type: SimpleType ->

            when {
                isIntType(type) -> intType()
                type.type == SimpleType.Type.TEXT -> "\"${RandomStringUtils.randomAlphanumeric(30)}\""
                type.type == SimpleType.Type.UUID -> "\"${UUID.randomUUID()}\""
                else -> throw error("not defined yet generation")
            }
        }

        val SINGLE_LINE = { type: SimpleType ->

            when {
                isIntType(type) -> intType()
                type.type == SimpleType.Type.TEXT -> "'${RandomStringUtils.randomAlphanumeric(30)}'"
                type.type == SimpleType.Type.UUID -> UUID.randomUUID().toString()
                else -> throw error("not defined yet generation")
            }
        }

        private fun intType(): String = Random.nextInt(0, 10000).toString()
        private fun isIntType(type: SimpleType): Boolean = when (type.type) {
            SimpleType.Type.DATE -> true
            SimpleType.Type.TIMESTAMP -> true
            SimpleType.Type.INT -> true
            else -> false
        }

        fun jsonApply(entry: Any, type: SimpleType): String =
            if (isIntType(type))
                if (entry is Int) entry.toString()
                else error("invalid type")
            else
                if (entry !is String) error("invalid type")
                else when(type.type){
                    SimpleType.Type.TEXT -> "\"${entry}\""
                    SimpleType.Type.UUID -> entry
                    else -> throw error("not defined yet generation")
                }

        fun singleLineApply(entry: Any, type: SimpleType): String =
            if (isIntType(type))
                if (entry is Int) entry.toString()
                else error("invalid type")
            else
                if (entry !is String)
                    error("invalid type")
                else when(type.type){
                    SimpleType.Type.TEXT -> "'${entry}'"
                    SimpleType.Type.UUID -> entry
                    else -> throw error("not defined yet generation")
                }

    }
}
