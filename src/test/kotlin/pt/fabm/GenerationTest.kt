package pt.fabm

import org.junit.Assert
import org.junit.Test
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import pt.fabm.types.SimpleType

class GenerationTest {

    @Test
    fun testModelMap() {

        val tables = listOf(defaultTable)

        val modelExpected = Model(tables.flatMap { it.dependecies }, tables)
        val modelMap = modelExpected.toMap()
        val model = Model.fromSupplier { modelMap[it] }

        Assert.assertEquals(modelExpected, model)
    }

    @Test
    fun testPrepareStatementsGen() {
        val table = Table.name(DDLConst.name) {
            simpleField(FieldConst.myField1, SimpleType.Type.TEXT, Field.KeyType.PARTITION)
            simpleField(FieldConst.myField2, SimpleType.Type.TIMESTAMP)
            simpleField(FieldConst.myField3, SimpleType.Type.INT)
            simpleField(FieldConst.myField4, SimpleType.Type.DATE)
            setField(FieldConst.mySetField1, SimpleType.Type.TEXT)
        }
        val sb = StringBuilder()
        val psGeneration = PSGeneration(table, sb)
        psGeneration.generateInsert()
        Assert.assertEquals(
            "insert into my_table (my_field1, my_field2, my_field3, my_field4, my_set_field1) into (?,?,?,?,?);",
            sb.toString()
        )
        sb.clear()
        psGeneration.generateSelect(false, listOf(FieldConst.myField1))
        Assert.assertEquals(
            "select my_field1, my_field2, my_field3, my_field4, my_set_field1 from my_table where my_field1=?;",
            sb.toString()
        )
        sb.clear()
        psGeneration.generateSelect()
        Assert.assertEquals(
            "select my_field1, my_field2, my_field3, my_field4, my_set_field1 from my_table;",
            sb.toString()
        )
        sb.clear()
        psGeneration.generateUpdate(listOf(FieldConst.mySetField1), listOf(FieldConst.myField1))
        Assert.assertEquals(
            "update my_table set my_set_field1=? where my_field1=?;",
            sb.toString()
        )
    }

    @Test
    fun testInsertExampleGen() {
        val sb = StringBuilder()
        var counter = 4
        fun doCount(): Boolean {
            return counter-- > 1
        }

        var strCounter = 0
        fun generate(rawMode: Boolean, simpleType: SimpleType): String {
            strCounter++
            return if (rawMode) strCounter.toString()
            else ExampleInsertGeneratorJson.applySimple(strCounter.toString(), simpleType)
        }
        ExampleInsertGeneratorJson(
            appendable = sb,
            repeatInCollection = ::doCount,
            simpleGenerator = ::generate
        ).generateInsert(defaultTable)

        Assert.assertEquals(expectedInsertJsonExample, sb.toString())
    }

    @Test
    fun testSerialization() {
        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(options)

        val modelExpected = defaultTable.let { table ->
            Model(table.dependecies.toList(), listOf(table))
        }

        val serialized = yaml.dump(defaultTable.let { table ->
            Model(table.dependecies, listOf(table)).toMap()
        })

        Assert.assertEquals(expectedSerialization, serialized)
        val map = yaml.loadAs(serialized, Map::class.java)
        val model = Model.fromSupplier { map[it] }

        Assert.assertEquals(modelExpected, model)
    }

    @Test
    fun testDDLType() {
        val sb = StringBuilder()
        ct3.printDDL(sb)

        Assert.assertEquals(expectedDDLType, sb.toString())
    }

    @Test
    fun testDDLTable2PartitionKeys() {
        val table = defaultTable
        table.fields.let {
            it += Field("my_pk2", SimpleType.Type.TEXT.asType(), Field.KeyType.PARTITION, 0)
        }
        val sb = StringBuilder()
        table.printDDL(sb)

        Assert.assertEquals(expectedDDL2PartitionKeys, sb.toString())
    }

    @Test
    fun testDDLSubTable1Key() {
        val table = defaultTable
        val sb = StringBuilder()
        val concreteTables = table.concreteTables()
        Assert.assertEquals(concreteTables.size, 2)
        concreteTables[0].printDDL(sb)

        Assert.assertEquals(expectedDDLConcreteTable1, sb.toString())
    }
}