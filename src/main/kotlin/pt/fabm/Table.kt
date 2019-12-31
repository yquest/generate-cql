package pt.fabm

import pt.fabm.types.CustomType

class Table(val name: String) : WithFields,DDLAble {
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

        val map = hashMapOf<String, Any>()
        map["name"] = this.name
        if (subTables.isNotEmpty()) map["sub"] = subTables.map { it.toMap() }
        if (fields.isNotEmpty()) map["fields"] = fields.map(Field::toPairMap).toMap()
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
            val superString = "=super"
            var index = current.indexOf(superString)
            if (index == -1) return current

            do {
                if (index > 0 && current[index - 1] == '=') {
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

    override fun printDDL(appendable: java.lang.Appendable) {
        appendable.append("create table ")
        val isSimpleKey = fields.filter {
            it.pkType == Field.KeyType.PARTITION || it.pkType == Field.KeyType.CLUSTER
        }.size < 2
        appendable.append("${name}(\n")
        val fieldIterator = orderedFields.iterator()
        while (fieldIterator.hasNext()) {
            val field = fieldIterator.next()
            val comma = if (fieldIterator.hasNext() || !isSimpleKey) "," else ""
            val simpleKey = if (isSimpleKey && field.pkType == Field.KeyType.PARTITION) " primary key" else ""
            appendable.append("  ${field.name}   ${field.type.literalName}$simpleKey$comma\n")
        }
        if (!isSimpleKey) {
            val pk = fields.filter { it.pkType == Field.KeyType.PARTITION }
                .joinToString(", ", "(", ")") { it.name }
            val keys = listOf(pk) + fields.filter { it.pkType == Field.KeyType.CLUSTER }.map { it.name }
            appendable.append("  ").append("primary key")
            appendable.append(keys.joinToString(", ", "(", ")")).append('\n')
        }
        appendable.append(");\n")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Table

        if (name != other.name) return false
        if (fields != other.fields) return false
        if (subTables != other.subTables) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fields.hashCode()
        result = 31 * result + subTables.hashCode()
        return result
    }

    override fun toString(): String {
        return "Table(name='$name', fields=$fields, subTables=$subTables)"
    }

    companion object {
        fun name(name: String, init: Table.() -> Unit): Table {
            val table = Table(name)
            table.init()
            return table
        }

        fun fromSupplier(supplier: (String) -> Any?): List<Table> {

            val typesRaw = supplier("types") ?: throw error("types not present")
            if (typesRaw !is List<*>) throw error("types is not a list")
            val types = CustomType.fromYaml(typesRaw as Map<*, *>)

            fun Table.addFields(rawFields: Any?) {
                if (rawFields !is Map<*, *>?)
                    throw error("expected a map")
                (rawFields ?: emptyMap<Any, Any?>()).entries
                    .map { it.key.toString() to it.value }
                    .map { if (it.second == null) error("expect a map") else it.first to it.second!! }
                    .map {
                        Field.fromSupplier(it.first, it.second, types)
                    }
                    .let { fields.addAll(it) }
            }

            fun fromRawToTable(rawTable: Any?): Table {
                if (rawTable !is Map<*, *>) error("expected a table as a map")
                val table = Table(rawTable["name"] as String)
                table.addFields(rawTable["fields"] as Map<*, *>)
                val subList = rawTable["sub"]
                if (subList !is List<*>) throw error("expected a list")

                subList.map { rawSubTable ->
                    val subTable: Table
                    if (rawSubTable is Map<*, *>) {
                        subTable = Table(rawSubTable["name"] as String)
                        subTable.addFields(rawSubTable["fields"] as Map<*, *>?)
                        table.subTables.add(subTable)
                    } else error("sub unexpected format")
                }
                return table
            }

            val tablesRaw = supplier("tables") ?: throw error("types not present")
            if (tablesRaw !is List<*>) throw error("tables is not a list")

            return tablesRaw.map(::fromRawToTable)
        }
    }
}