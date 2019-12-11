package pt.fabm

class Table(val name: String) {
    val fields: MutableList<Field> = mutableListOf()
    var subTables = mutableListOf<Table>()
    fun toMap(): Map<String, Any> {
        val map = hashMapOf<String, Any>()
        map["name"] = this.name
        map["fields"] = fields.map {
            val fieldMap = mutableMapOf<String, Any>()
            fieldMap.put("type", it.type.literalName)
            if (it.pkType != Field.KeyType.NONE) fieldMap.put("key", it.pkType.name.toLowerCase())
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
        fun renderSubName(current: String) {
            val sb = StringBuilder()
            var mark = 0
            var index = current.indexOf("#super")

            while (index != -1) {
                sb.append(current.substring(mark, index))
                if (index > 0 && current.get(index - 1) == '#') {
                    sb.append("#super")
                    mark = index + 7
                } else {
                    sb.append(this.name)
                    mark = index + 6
                }
                index = current.indexOf("#super", mark)
            }
            sb.append(current.substring(mark))
        }

        if (subTables.isEmpty()) return listOf(this)
        return subTables.flatMap { superTable ->
            superTable.concreteTables().map { subTable ->
                val subFieldsHerited = mutableListOf<Field>()
                val subFieldsConcretes = mutableListOf<Field>()
                val concreteTable = Table(subTable.name)
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
    }
}