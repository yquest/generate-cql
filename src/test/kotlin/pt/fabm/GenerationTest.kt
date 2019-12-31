package pt.fabm

import org.junit.Assert
import org.junit.Test
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType

class GenerationTest {

    private val ct1
        get() = CustomType.name("myCustomType1") {
            simpleField("ctf11", SimpleType.Type.TEXT)
            simpleField("ctf12", SimpleType.Type.TEXT)
        }
    private val ct2
        get() = CustomType.name("myCustomType2") {
            simpleField("ctf21", SimpleType.Type.TEXT)
            simpleField("ctf22", SimpleType.Type.TEXT)
            field("ctf23", ct1)
        }

    private val ct3
        get() = CustomType.name("myCustomType3") {
            simpleField("ctf31", SimpleType.Type.TEXT)
            simpleField("ctf32", SimpleType.Type.TEXT)
            field("ctf33", ct2)
        }
    private val defaultTable
        get() = Table.name("myTable") {
            simpleField("myField1", SimpleType.Type.TEXT)
            simpleField("myField2", SimpleType.Type.TIMESTAMP)
            simpleField("myField3", SimpleType.Type.INT)
            simpleField("myField4", SimpleType.Type.DATE)
            frozen("frozen5", ct1)
            setField("mySetField1", SimpleType.Type.TEXT)
            frozen("myField5", ct2)
            frozen("myField6", ct3, Field.KeyType.NONE, 2)
            subTable("=super_a") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION, 0)
                simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER, 1)
            }
            subTable("=super_b") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
            }

        }


    @Test
    fun testModelMap() {

        val tables = listOf(defaultTable)

        val modelExpected = Model(tables.flatMap { it.dependecies }, tables)
        val modelMap = modelExpected.toMap()
        val model = Model.fromSupplier { modelMap[it] }

        Assert.assertEquals(modelExpected, model)
    }

    @Test
    fun testInsertGen() {
        val model = Table.name("myTable") {
            simpleField("myField1", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
            simpleField("myField2", SimpleType.Type.TIMESTAMP)
            simpleField("myField3", SimpleType.Type.INT)
            simpleField("myField4", SimpleType.Type.DATE)
            setField("mySetField1", SimpleType.Type.TEXT)
        }

        val sb = StringBuilder()
        InsertPSGeneration(sb).generatePS(model)
        Assert.assertEquals(
            "insert into myTable (myField1, myField2, myField3, myField4, mySetField1) into (?,?,?,?,?);",
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

        Assert.assertEquals(expectedJsonExample, sb.toString())
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
            it += Field("myPk1", SimpleType.Type.TEXT.asType(), Field.KeyType.PARTITION, 0)
            it += Field("myPk2", SimpleType.Type.TEXT.asType(), Field.KeyType.PARTITION, 1)
        }
        val sb = StringBuilder()
        table.printDDL(sb)

        Assert.assertEquals(expectedDDL2PartitionKeys, sb.toString())
    }

    @Test
    fun testDDLTable1PartitionKey() {
        val table = defaultTable
        table.fields.let {
            it += Field("myPk1", SimpleType.Type.TEXT.asType(), Field.KeyType.PARTITION, 0)
        }
        val sb = StringBuilder()
        table.printDDL(sb)

        Assert.assertEquals(expectedDDL1PartitionKey, sb.toString())
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