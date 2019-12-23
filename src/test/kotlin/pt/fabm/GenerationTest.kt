package pt.fabm

import org.junit.Assert
import org.junit.Test
import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType
import java.lang.StringBuilder

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
            setField("mySetField1" ,SimpleType.Type.TEXT)
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
    fun testInsertGen(){
        val model = Table.name("myTable") {
            simpleField("myField1", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
            simpleField("myField2", SimpleType.Type.TIMESTAMP)
            simpleField("myField3", SimpleType.Type.INT)
            simpleField("myField4", SimpleType.Type.DATE)
            setField("mySetField1" ,SimpleType.Type.TEXT)

        }

        val sb = StringBuilder()
        InsertPSGeneration(sb).generatePS(model)
        Assert.assertEquals(
            "insert into myTable (myField1, myField2, myField3, myField4, mySetField1) into (?,?,?,?,?);",
            sb.toString()
        )
        println(sb.toString())
    }
}