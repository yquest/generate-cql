create type myCustomType1(
  ctf11 text,
  ctf12 text
);
create type myCustomType2(
  ctf21 text,
  ctf22 text,
  ctf23 frozen<myCustomType1>
);
create type myCustomType3(
  ctf31 text,
  ctf32 text,
  ctf33 frozen<myCustomType2>
);
create table myTable(
  myField1   text primary key,
  myField2   timestamp,
  myField6   myCustomType3,
  myField3   int,
  myField4   date,
  frozen5   frozen<mycustomtype1>,
  mySetField1   set<text>,
  myField5   frozen<myCustomType2>
);
