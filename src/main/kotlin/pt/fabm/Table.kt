package pt.fabm

class Table(val name: String) {
    val fields: MutableList<Field> = mutableListOf()
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

    fun simpleField(name: String, type: SimpleType.Type, key: Field.KeyType = Field.KeyType.NONE) {
        fields.add(Field(name, type.asType(), key))
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
            fun fromRawToTable(rawTable: Map<*, *>): Table {
                @Suppress("UNCHECKED_CAST")
                (rawTable["fields"] as List<Map<*, *>>).map { rawField ->
                    TODO("complete fields map")
                }
                TODO("complete table map")
            }

            @Suppress("UNCHECKED_CAST")
            val tablesList: List<Map<*, *>> = map["tables"] as List<Map<*, *>>
            return tablesList.map { rawTable ->
                fromRawToTable(rawTable)
            }
        }
    }
}