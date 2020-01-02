create type my_custom_type1(
  ct11 text,
  ct12 text
);
create type my_custom_type2(
  ct21 text,
  ct22 text,
  ct23 frozen<my_custom_type1>
);
create type my_custom_type3(
  ct31 text,
  ct32 text,
  ct33 frozen<my_custom_type2>
);
create table my_table(
  my_field1   text primary key,
  my_field2   timestamp,
  my_field6   frozen<my_custom_type3>,
  my_field3   int,
  my_field4   date,
  frozen_field   frozen<my_custom_type1>,
  my_set_field1   set<text>,
  my_field5   frozen<my_custom_type2>
);