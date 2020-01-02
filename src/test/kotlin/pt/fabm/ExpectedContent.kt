package pt.fabm

import pt.fabm.types.CustomType
import pt.fabm.types.SimpleType

object FieldConst {
    const val myField1 = "my_field1"
    const val myField2 = "my_field2"
    const val myField3 = "my_field3"
    const val myField4 = "my_field4"
    const val myField5 = "my_field5"
    const val myField6 = "my_field6"
    const val mySetField1 = "my_set_field1"
    const val frozenField = "frozen_field"
    const val ct11 = "ct11"
    const val ct12 = "ct12"
    const val ct21 = "ct21"
    const val ct22 = "ct22"
    const val ct23 = "ct23"
    const val ct31 = "ct31"
    const val ct32 = "ct32"
    const val ct33 = "ct33"
}

object DDLConst {
    const val name = "my_table"
    const val myCustomType1 = "my_custom_type1"
    const val myCustomType2 = "my_custom_type2"
    const val myCustomType3 = "my_custom_type3"
}

val ct1
    get() = CustomType.name(DDLConst.myCustomType1) {
        simpleField(FieldConst.ct11, SimpleType.Type.TEXT)
        simpleField(FieldConst.ct12, SimpleType.Type.TEXT)
    }
val ct2
    get() = CustomType.name(DDLConst.myCustomType2) {
        simpleField(FieldConst.ct21, SimpleType.Type.TEXT)
        simpleField(FieldConst.ct22, SimpleType.Type.TEXT)
        frozen(FieldConst.ct23, ct1)
    }

val ct3
    get() = CustomType.name(DDLConst.myCustomType3) {
        simpleField(FieldConst.ct31, SimpleType.Type.TEXT)
        simpleField(FieldConst.ct32, SimpleType.Type.TEXT)
        frozen(FieldConst.ct33, ct2)
    }
val defaultTable
    get() = Table.name(DDLConst.name) {
        simpleField(FieldConst.myField1, SimpleType.Type.TEXT, Field.KeyType.PARTITION)
        simpleField(FieldConst.myField2, SimpleType.Type.TIMESTAMP)
        simpleField(FieldConst.myField3, SimpleType.Type.INT)
        simpleField(FieldConst.myField4, SimpleType.Type.DATE)
        frozen(FieldConst.frozenField, ct1)
        setField(FieldConst.mySetField1, SimpleType.Type.TEXT)
        frozen(FieldConst.myField5, ct2)
        frozen(FieldConst.myField6, ct3, Field.KeyType.NONE, 2)
        subTable("=super_a") {
            simpleField(FieldConst.myField1, SimpleType.Type.TEXT)
            simpleField(FieldConst.myField4, SimpleType.Type.TEXT, Field.KeyType.PARTITION, 0)
            simpleField(FieldConst.myField5, SimpleType.Type.TEXT, Field.KeyType.CLUSTER, 1)
        }
        subTable("=super_b") {
            simpleField(FieldConst.myField4, SimpleType.Type.TEXT, Field.KeyType.PARTITION)
        }

    }

val expectedSerialization = """
types:
- name: my_custom_type1
  fields:
    ct11:
      type: text
    ct12:
      type: text
- name: my_custom_type2
  fields:
    ct21:
      type: text
    ct22:
      type: text
    ct23:
      frozen:
        custom: my_custom_type1
- name: my_custom_type3
  fields:
    ct31:
      type: text
    ct32:
      type: text
    ct33:
      frozen:
        custom: my_custom_type2
tables:
- sub:
  - name: =super_a
    fields:
      my_field1:
        type: text
      my_field4:
        type: text
        order: 0
        key: partition
      my_field5:
        type: text
        order: 1
        key: cluster
  - name: =super_b
    fields:
      my_field4:
        type: text
        key: partition
  name: my_table
  fields:
    my_field1:
      type: text
      key: partition
    my_field2:
      type: timestamp
    my_field3:
      type: int
    my_field4:
      type: date
    frozen_field:
      frozen:
        custom: my_custom_type1
    my_set_field1:
      set:
        type: text
    my_field5:
      frozen:
        custom: my_custom_type2
    my_field6:
      frozen:
        custom: my_custom_type3
      order: 2

    """.trimIndent()
val expectedDDL1PartitionKey = """
create table my_table(
  my_pk1   text primary key,
  my_field1   text,
  my_field2   timestamp,
  my_field6   frozen<my_custom_type3>,
  my_field3   int,
  my_field4   date,
  frozen_field   frozen<my_custom_type1>,
  my_set_field1   set<text>,
  my_field5   frozen<my_custom_type2>
);

""".trimIndent()
val expectedDDL2PartitionKeys = """
create table my_table(
  my_pk2   text,
  my_field1   text,
  my_field2   timestamp,
  my_field6   frozen<my_custom_type3>,
  my_field3   int,
  my_field4   date,
  frozen_field   frozen<my_custom_type1>,
  my_set_field1   set<text>,
  my_field5   frozen<my_custom_type2>,
  primary key((my_field1, my_pk2))
);

""".trimIndent()
val expectedDDLType = """
create type my_custom_type3(
  ct31 text,
  ct32 text,
  ct33 frozen<my_custom_type2>
);

""".trimIndent()
val expectedDDLConcreteTable1 = """
create table my_table_a(
  my_field4   text,
  my_field5   text,
  my_field6   frozen<my_custom_type3>,
  my_field1   text,
  my_field2   timestamp,
  my_field3   int,
  frozen_field   frozen<my_custom_type1>,
  my_set_field1   set<text>,
  primary key((my_field4), my_field5)
);

""".trimIndent()

private const val jsonExample = """{
  "my_field1": "1",
  "my_field2": "2",
  "my_field6": {
    "ct31": "3",
    "ct32": "4",
    "ct33": {
      "ct21": "5",
      "ct22": "6",
      "ct23": {
        "ct11": "7",
        "ct12": "8"
      }
    }
  },
  "my_field3": 9,
  "my_field4": "10",
  "frozen_field": {
    "ct11": "11",
    "ct12": "12"
  },
  "my_set_field1": ["13","14","15","16"],
  "my_field5": {
    "ct21": "17",
    "ct22": "18",
    "ct23": {
      "ct11": "19",
      "ct12": "20"
    }
  }
}"""

val expectedInsertJsonExample = """
insert into my_table json '$jsonExample';
""".trimIndent()

fun main() {
    val sb = StringBuilder()
    (defaultTable.dependecies.reversed() + listOf<DDLAble>(defaultTable)).forEach {
        it.printDDL(sb)
    }
    println(sb)
}