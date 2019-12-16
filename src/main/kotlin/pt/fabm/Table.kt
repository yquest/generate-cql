package pt.fabm

import pt.fabm.types.SimpleType
import pt.fabm.types.Type
import java.lang.Appendable

class Table(val name: String):WithFields {
    override val fields = mutableListOf<Field>()
    var subTables = mutableListOf<Table>()
    fun toMap(): Map<String, Any> {
        val map = hashMapOf<String, Any>()
        map["name"] = this.name
        if (subTables.isNotEmpty()) map["sub"] = subTables.map { it.toMap() }
        if (fields.isNotEmpty()) map["fields"] = fields.map {
            val fieldMap = mutableMapOf<String, Any>()
            fieldMap["type"] = it.type.literalName
            if (it.pkType != Field.KeyType.NONE) fieldMap["key"] = it.pkType.name.toLowerCase()
            it.name to fieldMap
        }.toMap()
        return map
    }

    fun subTable(name: String, init: (Table.() -> Unit)? = null) {
        val table = Table(name)
        if (init != null) table.init()
        subTables.add(table)
    }

    fun concreteTables(): List<Table> {
        fun renderSubName(current: String): String {
            val sb = StringBuilder()
            var mark = 0
            val superString = "#super"
            var index = current.indexOf(superString)
            if (index == -1) return current

            do {
                if (index > 0 && current[index - 1] == '#') {
                    sb.append(current.substring(mark, index - 1))
                    sb.append(superString)
                } else {
                    sb.append(current.substring(mark, index))
                    sb.append(this.name)
                }
                index += mark
                mark = index + 6
                index = current.indexOf(superString, mark)
            } while (index != -1)
            sb.append(current.substring(mark))
            return sb.toString()
        }

        if (subTables.isEmpty()) return listOf(this)
        return subTables.flatMap { superTable ->
            superTable.concreteTables().map { subTable ->
                val subFieldsHerited = mutableListOf<Field>()
                val subFieldsConcretes = mutableListOf<Field>()
                val concreteTable = Table(renderSubName(subTable.name))
                for (superField in this.fields) {
                    val subField = subTable.fields.find { it.name == superField.name }
                    subFieldsConcretes.add(subField ?: superField)
                }
                val all = mutableListOf<Field>()
                all.addAll(subFieldsHerited)
                all.addAll(subFieldsConcretes)
                concreteTable.fields.addAll(all)
                concreteTable
            }
        }
    }

    companion object {
        fun name(name: String, init: Table.() -> Unit): Table {
            val table = Table(name)
            table.init()
            return table
        }

        fun fromMap(map: Map<*, *>): List<Table> {
            fun Map<*, *>.getType(): Type = (SimpleType.Type.values().find {
                it.name == this["type"].toString().toUpperCase()
            } ?: error("no simple type found")).asType()

            fun Map<*, *>.getKey(): Field.KeyType {
                if (this["key"] == null) return Field.KeyType.NONE
                val keyName = this["key"].toString()
                return Field.KeyType.values().find {
                    keyName.equals(it.name, true)
                } ?: error("invalid key $keyName")
            }

            fun Table.addFields(rawFields: Map<*, *>?) {
                (rawFields ?: emptyMap<Any, Any>()).forEach { rawField ->
                    if (rawField.value !is Map<*, *>) error("expect a map")
                    val fieldEntry = rawField.value as Map<*, *>
                    val field = Field(rawField.key.toString(), fieldEntry.getType(), fieldEntry.getKey())
                    this.fields += field
                }
            }

            fun fromRawToTable(rawTable: Any?): Table {
                if (rawTable !is Map<*, *>) error("expected a table as a map")
                val table = Table(rawTable["name"] as String)
                table.addFields(rawTable["fields"] as Map<*, *>)

                (rawTable["sub"] as List<*>).map { rawSubTable ->
                    val subTable: Table
                    if (rawSubTable is Map<*, *>) {
                        subTable = Table(rawSubTable["name"] as String)
                        subTable.addFields(rawSubTable["fields"] as Map<*, *>?)
                        table.subTables.add(subTable)
                    } else error("sub unexpected format")
                }
                return table
            }

            val tablesList: List<*> = map["tables"].let {
                if (it !is List<*>) error("wrong format")
                else it
            }
            return tablesList.map(::fromRawToTable)
        }

        fun printTables(tables:List<Table>, appendable: Appendable){
        }
    }


}