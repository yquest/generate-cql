package pt.fabm

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType


fun main() {

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
        simpleField("myField2", SimpleType.Type.TEXT)
        simpleField("myField3", SimpleType.Type.INT)
        simpleField("myField4", SimpleType.Type.TEXT)
        field("myField5", ct2)
        field("myField6", ct3)
        subTable("=super_a") {
            simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION, 0)
            simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.NONE, 2)
        }
        subTable("x_=super_b_a") {
            simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
            simpleField("myField5", SimpleType.Type.TEXT, Field.KeyType.CLUSTER)
        }
        subTable("==super_c")
        subTable("aa==super_c")
    }

    val options = DumperOptions()
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    val yaml = Yaml(options)


    val inStream = object {}.javaClass.getResourceAsStream("/model.yaml")

    val map = yaml.loadAs(inStream, Map::class.java)
    val fromMapModel: List<Table> = Table.fromSupplier(listOf(ct1, ct2, ct3)) { map[it] }

    println("dependencies")
    WithFields.printDependencies(fromMapModel[0].dependecies, System.out)
    Table.printTables(fromMapModel[0].concreteTables(), System.out)
    val concret = model.concreteTables()
    concret.forEach {
        println(it.name)
    }

    println(yaml.dumpAsMap(mapOf("tables" to listOf(model.toMap()))))

    ExampleInsertGeneratorSingleLine(System.out, { false }, SimpleTypeGenerator.JSON).generateInsert(concret[0])
    println()
    ExampleInsertGeneratorJson(System.out, { false }, SimpleTypeGenerator.JSON).generateInsert(concret[0])

}