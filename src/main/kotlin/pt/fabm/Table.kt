package pt.fabm

import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType
import pt.fabm.types.Type

class Table(val name: String) : WithFields {
    override val fields = mutableListOf<Field>()
    var subTables = mutableListOf<Table>()
    val orderedFields: List<Field>
        get() {
            val toReorder = fields.filter { it.order != -1 }
            val orderedFields = fields.filter { it.order == -1 }.toMutableList()
            for (fieldToReorder in toReorder) {
                orderedFields.add(fieldToReorder.order, fieldToReorder)
            }
            return orderedFields
        }

    fun toMap(): Map<String, Any> {
        fun createCustomMap(field: Field): Pair<String, Any> {
            val type = field.type
            val resultMap = if (type !is CustomType) {
                type.map.toMutableMap()
            } else {
                mutableMapOf("custom" to field.type.literalName as Any)
            }
            if (field.order != -1)
                resultMap["order"] = field.order
            return field.name to resultMap
        }

        val map = hashMapOf<String, Any>()
        map["name"] = this.name
        if (subTables.isNotEmpty()) map["sub"] = subTables.map { it.toMap() }
        if (fields.isNotEmpty()) map["fields"] = fields.map(::createCustomMap).toMap()
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

            fun Map<*, *>.getOrder(): Int {
                if (this["order"] == null) return -1
                return this["order"].toString().toInt()
            }

            fun Table.addFields(rawFields: Map<*, *>?) {
                (rawFields ?: emptyMap<Any, Any>()).forEach { rawField ->
                    if (rawField.value !is Map<*, *>) error("expect a map")
                    val fieldEntry = rawField.value as Map<*, *>
                    val field =
                        Field(rawField.key.toString(), fieldEntry.getType(), fieldEntry.getKey(), fieldEntry.getOrder())
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

        fun printTables(tables: List<Table>, appendable: Appendable) {
            for (table in tables) {
                val isSimpleKey = !table.fields.any { it.pkType == Field.KeyType.CLUSTER }
                appendable.append("${table.name}(\n")
                val fieldIterator = table.orderedFields.iterator()
                while (fieldIterator.hasNext()) {
                    val field = fieldIterator.next()
                    val comma = if (fieldIterator.hasNext() || !isSimpleKey) "," else ""
                    val simpleKey = if (isSimpleKey && field.pkType == Field.KeyType.PARTITION) " primary_key" else ""
                    appendable.append("  ${field.name}   ${field.type.literalName}$simpleKey$comma\n")
                }
                if (!isSimpleKey) {
                    val pk = table.fields.filter { it.pkType == Field.KeyType.PARTITION }
                        .joinToString(", ", "(", ")") { it.name }
                    val keys = listOf(pk) + table.fields.filter { it.pkType == Field.KeyType.CLUSTER }.map { it.name }
                    appendable.append("  ").append("primary key")
                    appendable.append(keys.joinToString(", ", "(", ")")).append('\n')
                }
                appendable.append(");\n")
            }
        }
    }


}