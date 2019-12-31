package pt.fabm

val expectedSerialization = """
types:
- name: myCustomType1
  fields:
    ctf11:
      type: text
    ctf12:
      type: text
- name: myCustomType2
  fields:
    ctf21:
      type: text
    ctf22:
      type: text
    ctf23:
      custom: myCustomType1
- name: myCustomType3
  fields:
    ctf31:
      type: text
    ctf32:
      type: text
    ctf33:
      custom: myCustomType2
tables:
- sub:
  - name: =super_a
    fields:
      myField4:
        type: text
        order: 0
        key: partition
      myField5:
        type: text
        order: 1
        key: cluster
  - name: =super_b
    fields:
      myField4:
        type: text
        key: partition
  name: myTable
  fields:
    myField1:
      type: text
    myField2:
      type: timestamp
    myField3:
      type: int
    myField4:
      type: date
    frozen5:
      frozen:
        custom: myCustomType1
    mySetField1:
      set:
        type: text
    myField5:
      frozen:
        custom: myCustomType2
    myField6:
      frozen:
        custom: myCustomType3
      order: 2

    """.trimIndent()
val expectedDDL1PartitionKey = """
create table myTable(
  myPk1   text primary key,
  myField1   text,
  myField2   timestamp,
  myField6   frozen<mycustomtype3>,
  myField3   int,
  myField4   date,
  frozen5   frozen<mycustomtype1>,
  mySetField1   set<text>,
  myField5   frozen<mycustomtype2>
);

""".trimIndent()
val expectedDDL2PartitionKeys = """
create table myTable(
  myPk1   text,
  myPk2   text,
  myField1   text,
  myField2   timestamp,
  myField6   frozen<mycustomtype3>,
  myField3   int,
  myField4   date,
  frozen5   frozen<mycustomtype1>,
  mySetField1   set<text>,
  myField5   frozen<mycustomtype2>,
  primary key((myPk1, myPk2))
);

""".trimIndent()
val expectedDDLType = """
create type myCustomType3(
  ctf31 text,
  ctf32 text,
  ctf33 myCustomType2
);

""".trimIndent()
val expectedDDLConcreteTable1 = """
create table myTable_a(
  myField4   text,
  myField5   text,
  myField6   frozen<mycustomtype3>,
  myField1   text,
  myField2   timestamp,
  myField3   int,
  frozen5   frozen<mycustomtype1>,
  mySetField1   set<text>,
  primary key((myField4), myField5)
);

""".trimIndent()
val expectedJsonExample = """
insert into myTable json '{
  "myField1": "1",
  "myField2": "2",
  "myField6": {
    "ctf31": "3",
    "ctf32": "4",
    "ctf33": {
      "ctf21": "5",
      "ctf22": "6",
      "ctf23": {
        "ctf11": "7",
        "ctf12": "8"
      }
    }
  },
  "myField3": 9,
  "myField4": "10",
  "frozen5": {
    "ctf11": "11",
    "ctf12": "12"
  },
  "mySetField1": ["13","14","15","16"],
  "myField5": {
    "ctf21": "17",
    "ctf22": "18",
    "ctf23": {
      "ctf11": "19",
      "ctf12": "20"
    }
  }
}';
""".trimIndent()