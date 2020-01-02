package pt.fabm

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.datastax.driver.core.Row
import org.cassandraunit.AbstractCassandraUnit4CQLTestCase
import org.cassandraunit.dataset.CQLDataSet
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class CassandraTests : AbstractCassandraUnit4CQLTestCase() {

    override fun getDataSet(): CQLDataSet {
        return ClassPathCQLDataSet("ddl.cql")
    }

    private val sb = StringBuilder()
    private fun insertRow() {
        session.execute(expectedInsertJsonExample)
    }

    @Before
    fun beforeTest() {
        sb.clear()
    }

    @Test
    fun generatedSelect() {
        insertRow()
        val psGeneration = PSGeneration(defaultTable, sb)
        psGeneration.generateSelect()
        val rs = session.execute(sb.toString())
        val rows = rs.all()
        Assert.assertEquals(1, rows.size)
    }

    @Test
    fun generatedUpdate() {
        fun toJsonObject(row: Row): JsonObject = Parser
            .default()
            .parse(StringBuilder(row.getString(0))) as JsonObject

        insertRow()
        val psGeneration = PSGeneration(defaultTable, sb)
        psGeneration.generateSelect(fromJson = true)
        val expected = session.execute(sb.toString()).one().let(::toJsonObject)
        expected[FieldConst.mySetField1] = JsonArray("-1", "-2")
        sb.clear()
        psGeneration.generateUpdate(
            toSet = listOf(FieldConst.mySetField1),
            clause = listOf(FieldConst.myField1)
        )
        session.execute(sb.toString(), setOf("-1", "-2"), "1")
        sb.clear()
        psGeneration.generateSelect(fromJson = true)
        val afterUpdate = session.execute(sb.toString()).one().let(::toJsonObject)
        Assert.assertEquals(expected, afterUpdate)
    }
}