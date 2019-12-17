package pt.fabm

import org.apache.commons.lang3.RandomStringUtils
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType
import kotlin.random.Random

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

    val options = DumperOptions()
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    val yaml = Yaml(options)

    println("dependencies")
    WithFields.printDependencies(model.dependecies, System.out)
    Table.printTables(model.concreteTables(), System.out)
    val concret = model.concreteTables()
    concret.forEach {
        println(it.name)
    }

    val inStream = object {}.javaClass.getResourceAsStream("/model.yaml")

    val map = yaml.loadAs(inStream, Map::class.java)
    val fromMapModel: List<Table> = Table.fromMap(map)

    println(yaml.dumpAsMap(mapOf("tables" to listOf(model.toMap()))))

    fun printInsert(appendable: Appendable, table: Table) {

        val oderedFields = table.orderedFields
        var fieldsIterator = oderedFields.iterator()
        appendable.append("insert into ${table.name} (")
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val comma = if (fieldsIterator.hasNext()) ", " else ""
            appendable.append("${field.name}$comma")
        }
        appendable.append(") into (")

        fieldsIterator = oderedFields.iterator()
        while (fieldsIterator.hasNext()) {
            val field = fieldsIterator.next()
            val comma = if (fieldsIterator.hasNext()) ", " else ""
            val value = if (field.type is SimpleType) {
                if (field.type.type == SimpleType.Type.INT || field.type.type == SimpleType.Type.DATE) "${Random.nextInt(
                    0,
                    1000
                )}"
                else "'${RandomStringUtils.randomAlphabetic(20)}'"
            } else if (field.type is CustomType) {
                "'not yet generated'"
            } else error("type not expected")
            appendable.append("${value}$comma")
        }
        appendable.append(");")
    }

    printInsert(System.out, concret[0])
}