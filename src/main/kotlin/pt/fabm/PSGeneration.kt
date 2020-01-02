package pt.fabm

class PSGeneration(private val table: Table, private val appendable: Appendable) {
    fun generateInsert() {
        val oderedFields = table.orderedFields
        var fieldsIterator = oderedFields.iterator()
        appendable.append("insert into ").append(table.name).append(" (")

        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val comma = if (fieldsIterator.hasNext()) ", " else ""
            appendable.append(field.name).append(comma)
        }
        appendable.append(") into (")

        fieldsIterator = oderedFields.iterator()

        while (fieldsIterator.hasNext()) {
            fieldsIterator.next()
            val comma = if (fieldsIterator.hasNext()) "," else ""
            appendable.append("?")
            appendable.append(comma)
        }
        appendable.append(");")
    }

    fun generateSelect(fromJson: Boolean = false, checkFields: Iterable<String> = emptyList()) {
        val oderedFields = table.orderedFields
        val fieldsIterator = oderedFields.iterator()
        appendable.append("select ").append(if (fromJson) " json " else "")

        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val space = if (fieldsIterator.hasNext()) ", " else ""
            appendable.append(field.name).append(space)
        }

        appendable.append(" from ").append(table.name)

        val checkFieldsIterator = checkFields.iterator()
        if (checkFieldsIterator.hasNext()) appendable.append(" where ")

        while (checkFieldsIterator.hasNext()) {
            val field = checkFieldsIterator.next()
            val andClause = if (checkFieldsIterator.hasNext()) " and " else ""
            appendable.append(field)
            appendable.append("=?")
            appendable.append(andClause)
        }
        appendable.append(";")
    }

    fun generateUpdate(toSet: Iterable<String>, clause: Iterable<String>) {
        val toSetIterator = toSet.iterator()
        appendable.append("update ").append(table.name).append(" set ")

        while (toSetIterator.hasNext()) {
            val field = toSetIterator.next()
            val separator = if (toSetIterator.hasNext()) " and " else ""
            appendable.append(field).append("=?").append(separator)
        }

        val clauseIterator = clause.iterator()
        if (clauseIterator.hasNext()) appendable.append(" where ")
        else error("clause fields must be filled")

        while (clauseIterator.hasNext()) {
            val field = clauseIterator.next()
            val separator = if (clauseIterator.hasNext()) " and " else ""
            appendable.append(field).append("=?").append(separator)
        }

        appendable.append(";")
    }

    fun generateDelete(clause: Iterable<String>) {
        val clauseIterator = clause.iterator()
        if (!clauseIterator.hasNext()) throw error("no clause defined to delete")
        appendable.append("delete from ")
            .append(table.name)
            .append(" where ")
        while (clauseIterator.hasNext()) {
            val field = clauseIterator.next()
            val separator = if (clauseIterator.hasNext()) " and " else ""
            appendable.append(field).append("=?").append(separator)
        }

        appendable.append(";")
    }
}