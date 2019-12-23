package pt.fabm

class InsertPSGeneration (private val appendable: Appendable){
    fun generatePS(table:Table, postfix:()->String = {""}){
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
        appendable.append(postfix())
        appendable.append(");")
    }
}