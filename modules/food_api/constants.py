"""
	Module name: Constants
	
	Author: Regina Zhang
	
	Description:
		A python file for storing constants needed.
		
	Module created on 11/03/2015
	
	Modification logs:
	
"""

common = {"api_key" : "AB66LrhBWbvW9wEXTKCwuVkfclEnOxoWZinWjIY7",
		"format": "json"}

url = {"list":"http://api.nal.usda.gov/usda/ndb/list",
		"search":"http://api.nal.usda.gov/usda/ndb/search",
		"report":"http://api.nal.usda.gov/usda/ndb/reports"}

db = {"host": "45.56.85.191",
	"port": 3306,
	"user": "kimjongun",
	"password": "KimJongUnIsGreat",
	"schema": "HealthDB"}

nutrients = {'Water':{'db_field': 'Water','unit':'g', 'group':'Proximates'},
			'Energy':{'db_field': 'Energy','unit':'kcal', 'group':'Proximates'},
			'Protein':{'db_field': 'Protein','unit':'g', 'group':'Proximates'},
			'Total lipid (fat)':{'db_field': 'TotalLipid_Fat','unit':'g', 'group':'Proximates'},
			'Carbohydrate, by difference':{'db_field': 'CarbohydrateByDifference','unit':'g', 'group':'Proximates'},
			'Fiber, total dietary':{'db_field': 'FiberTotalDietary','unit':'g', 'group':'Proximates'},
			'Calcium, Ca':{'db_field': 'Calcium','unit':'mg', 'group':'Minerals'},
			'Iron, Fe':{'db_field': 'Iron','unit':'mg', 'group':'Minerals'},
			'Magnesium, Mg':{'db_field': 'Magnesium','unit':'mg', 'group':'Minerals'},
			'Phosphorus, P':{'db_field': 'Phosphorus','unit':'mg', 'group':'Minerals'},
			'Potassium, K':{'db_field': 'Potassium','unit':'mg', 'group':'Minerals'},
			'Sodium, Na':{'db_field': 'Sodium','unit':'mg', 'group':'Minerals'},
			'Zinc, Zn':{'db_field': 'Zinc','unit':'mg', 'group':'Minerals'},
			'Vitamin C, total ascorbic acid':{'db_field': 'VitaminCTotalAscorbicAcid','unit':'mg', 'group':'Vitamins'},
			'Thiamin':{'db_field': 'Thiamin','unit':'mg', 'group':'Vitamins'},
			'Riboflavin':{'db_field': 'Riboflavin','unit':'mg', 'group':'Vitamins'},
			'Niacin':{'db_field': 'Niacin','unit':'mg', 'group':'Vitamins'},
			'Vitamin B-6':{'db_field': 'VitaminB6','unit':'mg', 'group':'Vitamins'},
			'Folate, DFE':{'db_field': 'FolateDFE','unit':'micro g', 'group':'Vitamins'},
			'Vitamin B-12':{'db_field': 'VitaminB12','unit':'micro g', 'group':'Vitamins'},
			'Vitamin A, RAE':{'db_field': 'VitaminARAE','unit':'micro g', 'group':'Vitamins'},
			'Vitamin A, IU':{'db_field': 'VitaminAIU','unit':'IU', 'group':'Vitamins'},
			'Cholesterol':{'db_field': 'Cholesterol','unit':'mg', 'group':'Lipids'},
			'Caffeine':{'db_field': 'Caffeine','unit':'mg', 'group':'Other'}}

micro_unicode = u'\xb5'
NUM_LIST_ITEM = 10

"""CatsKillDeadMen"""