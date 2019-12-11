package pt.fabm

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml


fun main() {

    val model = Table.name("myTable") {
        simpleField("myField1", SimpleType.Type.TEXT)
        simpleField("myField2", SimpleType.Type.TEXT)
        simpleField("myField3", SimpleType.Type.INT)
        simpleField("myField4", SimpleType.Type.TEXT)
        subTable("#super_a"){
            simpleField("myField4", SimpleType.Type.TEXT, Field.KeyType.PARTITION)
        }
        subTable("#super_b")
        subTable("#super_c")
    }

    val options = DumperOptions()
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    val yaml = Yaml(options)

    val concret = model.concreteTables()

    println(yaml.dumpAsMap(mapOf("tables" to listOf(model.toMap()))))
}