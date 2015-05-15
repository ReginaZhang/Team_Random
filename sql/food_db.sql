use HealthDB;

/*alter table Food
drop column Energy;*/

alter table Food 
add column Niacin float,
add column Iron float,
add column Thiamin float,
add column VitaminB6 float,
add column CarbohydrateByDifference float,
add column Calcium float,
add column Water float,
add column VitaminCTotalAscorbicAcid float,
add column Sodium float,
add column Phosphorus float,
add column VitaminAIU float,
add column VitaminARAE float,
add column Potassium float,
add column Caffeine float,
add column Riboflavin float,
add column Magnesium float,
add column Cholesterol float,
add column FiberTotalDietary float,
add column VitaminB12 float,
add column Energy float,
add column TotalLipid_Fat float,
add column Zinc float,
add column Protein float,
add column FolateDFE float,
add column SugarsTotal,
add column VitaminEAlphaTocopherol,
add column VitaminD_D2D3,
add column VitaminD,
add column VitaminKPhylloquinone,
add column Ndbno not null
;