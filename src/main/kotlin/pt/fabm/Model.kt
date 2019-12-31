package pt.fabm

import pt.fabm.types.CustomType
import java.lang.Appendable

data class Model(
    val types: Iterable<CustomType>,
    val tables: Iterable<Table>
) {
    fun toMap(): Map<String, List<Map<String, Any>>> {
        val typesEntry = "types" to types.map {
            it.map
        }.reversed()
        val tablesEntry = "tables" to tables.map { it.toMap() }
        return mapOf(typesEntry, tablesEntry)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model

        if (types != other.types) return false
        if (tables != other.tables) return false

        return true
    }

    override fun hashCode(): Int {
        var result = types.hashCode()
        result = 31 * result + tables.hashCode()
        return result
    }

    fun printDDLs(appendable: Appendable){
        for(type in types){
            type.printDDL(appendable)
        }
    }

    companion object {
        fun fromSupplier(supplier: (String) -> Any?): Model {

            val rawTypes = supplier("types") ?: throw error("types not present")
            if (rawTypes !is List<*>) throw error("types is not a list")
            val types = CustomType.fromRawTypes(rawTypes)

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

            return Model(types, tablesRaw.map(::fromRawToTable))
        }

        fun createModel(list: List<Table>): Model =
            Model(list.flatMap {
                it.dependecies
            }, list)

    }
}