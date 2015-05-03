from constants import *

f = open("../../sql/food_db.sql", "w")
f.write("use HealthDB;\n")
f.write("alter table Food (\n")
f.write("drop column Energy,\n")
for nutrient in nutrients.keys():
	tokens = nutrient.split(" ")
	name = ""
	for elem in tokens:
		elem = elem.strip(",")
		name += elem[0].upper() + elem[1:]
	f.write("add column %s float,\n" % name)
f.write(");")
f.close()