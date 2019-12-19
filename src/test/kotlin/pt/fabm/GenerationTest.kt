package pt.fabm

import org.junit.Test
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType

class GenerationTest {

    companion object {
        fun createModel(): Table {
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


            return Table.name("myTable") {
                simpleField("myField1", SimpleType.Type.TEXT)
                simpleField("myField2", SimpleType.Type.TEXT)
                simpleField("myField3", SimpleType.Type.INT)
                simpleField("myField4", SimpleType.Type.TEXT)
                field("myField5", ct2)
                field("myField6", ct3)
                subTable("#super_a") {
                    simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION, 0)
                    simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.NONE, 2)
                }
                subTable("x_#super_b_a") {
                    simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
                    simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER)
                }
                subTable("##super_c")
                subTable("aa##super_c")
            }
        }
    }

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
            simpleField("myField4", SimpleType.Type.TEXT)
            field("myField5", ct2)
            field("myField6", ct3,Field.KeyType.NONE,2)
            subTable("#super_a") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION, 0)
                simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.NONE, 2)
            }
            subTable("x_#super_b_a") {
                simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
                simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER)
            }
        }

        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(options)

        println(
            yaml.dumpAsMap(
                mapOf(
                    "dependencies" to model.dependecies.map { it.map },
                    "tables" to listOf(model.toMap())
                )
            )
        )
    }
}