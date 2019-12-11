package pt.fabm

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml


fun main() {

    val model = Table.name("myTable") {
        simpleField("myField1", SimpleType.Type.TEXT)
        simpleField("myField2", SimpleType.Type.TEXT)
        simpleField("myField3", SimpleType.Type.INT)
        simpleField("myField4", SimpleType.Type.TEXT)
        subTable("#super_a") {
            simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
            simpleField("myField5", SimpleType.Type.TEXT)
        }
        subTable("x_#super_b_a")
        subTable("##super_c")
        subTable("aa##super_c")
    }

    val options = DumperOptions()
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    val yaml = Yaml(options)

    val concret = model.concreteTables()
    concret.forEach {
        println(it.name)
    }

    val inStream = object {}.javaClass.getResourceAsStream("/model.yaml")

    val map = yaml.loadAs(inStream, Map::class.java)
    val fromMapModel:List<Table> = Table.fromMap(map)


    println(yaml.dumpAsMap(mapOf("tables" to listOf(model.toMap()))))
}