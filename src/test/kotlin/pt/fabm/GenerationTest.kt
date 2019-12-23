package pt.fabm

import org.junit.Test
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType

class GenerationTest {

    @Test
    fun mapGeneration() {
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
            field("myField5", ct2)
            field("myField6", ct3, Field.KeyType.NONE, 2)
            subTable("#super_a") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION, 0)
                simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER, 1)
            }
            subTable("=super_b") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
                simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER)
            }
        }

        val modelMap = Table.createModel(listOf(model))

        Assert.assertEquals(Table.fromSupplier(listOf(ct3, ct2, ct1)) {
            modelMap[it]
        }, listOf(model))
    }
}