package pt.fabm

class PSGeneration (private val table:Table,private val appendable: Appendable){
    fun generateInsert(){
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

    fun generateSelect(checkFields:List<String> = emptyList(), postfix:()->String = {""}){
        val oderedFields = table.orderedFields
        val fieldsIterator = oderedFields.iterator()
        appendable.append("select ")

        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val space = if (fieldsIterator.hasNext()) ", " else ""
            appendable.append(field.name).append(space)
        }

        appendable.append(" from ").append(table.name)

        if(checkFields.isEmpty()) {
            appendable.append(';')
            return
        }

        appendable.append(" where ")

        val checkFieldsIterator = checkFields.iterator()

        while (checkFieldsIterator.hasNext()) {
            val field = checkFieldsIterator.next()
            val andClause = if (checkFieldsIterator.hasNext()) " and " else ""
            appendable.append(field)
            appendable.append("=?")
            appendable.append(andClause)
        }
        appendable.append(postfix())
        appendable.append(";")
    }
}