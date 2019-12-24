package pt.fabm

import org.junit.Assert
import org.junit.Test
import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType

class GenerationTest {

    @Test
    fun testModelMap() {
        val ct1 = CustomType.name("myCustomType1") {
            simpleField("ctf11", SimpleType.Type.TEXT)
            simpleField("ctf12", SimpleType.Type.TEXT)
        }

        val ct2 = CustomType.name("myCustomType2") {
            simpleField("ctf21", SimpleType.Type.TEXT)
            simpleField("ctf22", SimpleType.Type.TEXT)
            field("ctf23", ct1)
        }

        val ct3 = CustomType.name("myCustomType3") {
            simpleField("ctf31", SimpleType.Type.TEXT)
            simpleField("ctf32", SimpleType.Type.TEXT)
            field("ctf33", ct2)
        }

        val model = Table.name("myTable") {
            simpleField("myField1", SimpleType.Type.TEXT)
            simpleField("myField2", SimpleType.Type.TIMESTAMP)
            simpleField("myField3", SimpleType.Type.INT)
            simpleField("myField4", SimpleType.Type.DATE)
            setField("mySetField1", SimpleType.Type.TEXT)
            field("myField5", ct2)
            field("myField6", ct3, Field.KeyType.NONE, 2)
            subTable("=super_a") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION, 0)
                simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER, 1)
            }
            subTable("=super_b") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
                simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER)
            }
        }

        val modelMap = Table.createModel(listOf(model))

        Assert.assertEquals(Table.fromSupplier(listOf(ct1, ct2, ct3)) {
            modelMap[it]
        }, listOf(model))
    }

    @Test
    fun testInsertPSGen() {
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
    fun testInsertExamplesGen() {
        val ct1 = CustomType.name("myCustomType1") {
            simpleField("ctf11", SimpleType.Type.TEXT)
            simpleField("ctf12", SimpleType.Type.TEXT)
        }

        val table = Table.name("myTable") {
            simpleField("myField1", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
            simpleField("myField2", SimpleType.Type.TIMESTAMP)
            simpleField("myField3", SimpleType.Type.INT)
            simpleField("myField4", SimpleType.Type.DATE)
            setField("mySetField1", SimpleType.Type.TEXT)
            field("customField", ct1, Field.KeyType.NONE)
            //TODO fix in case that we have a different key in map than the TEXT one
            mapField("mapField", SimpleType.Type.TEXT.asType() to SimpleType.Type.INT.asType())
        }

        val sb = StringBuilder()
        val gen = listOf(
            "a",//myField1
            1, //myField2
            2, //myField3
            3, //myField4
            "b", "c", //mySetField1
            "d", "e", //customField
            "aa", 11, "cc", 22//mapField
        )
        var i = 1
        val firststop = {
            if (i-- > 0) true
            else {
                i = 1
                false
            }
        }

        gen.iterator().let { iterator ->
            ExampleInsertGeneratorSingleLine(sb, firststop) {
                SimpleTypeGenerator.singleLineApply(iterator.next(), it)
            }.generateInsert(table)
        }
        Assert.assertEquals(
            "insert into myTable (myField1, myField2, myField3, myField4, mySetField1, customField, mapField)"
                    + " into ('a', 1, 2, 3, {'b', 'c'}, {ctf11 : 'd', ctf12 : 'e'}, {'aa': 11, 'cc': 22});",
            sb.toString()
        )
        sb.clear()

        gen.iterator().let { iterator ->
            ExampleInsertGeneratorJson(sb, firststop) {
                SimpleTypeGenerator.jsonApply(iterator.next(), it)
            }.generateInsert(table)
        }
        Assert.assertEquals(
            """ 
            insert into myTable json '{
              "myField1": "a",
              "myField2": 1,
              "myField3": 2,
              "myField4": 3,
              "mySetField1": ["b", "c"],
              "customField": {
                "ctf11": "d",
                "ctf12": "e"
              },
              "mapField": {
                "aa": 11,
                "cc": 22
              }
            }';""".trimIndent(), sb.toString()
        )

    }
}