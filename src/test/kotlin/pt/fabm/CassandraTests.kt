package pt.fabm

import org.cassandraunit.AbstractCassandraUnit4CQLTestCase
import org.cassandraunit.dataset.CQLDataSet
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.junit.Assert
import org.junit.Test


class CassandraTests : AbstractCassandraUnit4CQLTestCase() {

    @Test
    fun should_have_started_and_execute_cql_script() {
        session.execute(expectedJsonExample)
        val rs = session.execute("select * from myTable;")
        val rows = rs.all()
        Assert.assertEquals(1, rows.size)
    }

    override fun getDataSet(): CQLDataSet {
        return ClassPathCQLDataSet("ddl.cql")
    }


}