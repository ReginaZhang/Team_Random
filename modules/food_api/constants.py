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

nutrients = {'Water':{'unit':'g', 'group':'Proximates'},
			'Energy':{'unit':'kcal', 'group':'Proximates'},
			'Protein':{'unit':'g', 'group':'Proximates'},
			'Total lipid (fat)':{'unit':'g', 'group':'Proximates'},
			'Carbohydrate, by difference':{'unit':'g', 'group':'Proximates'},
			'Fiber, total dietary':{'unit':'g', 'group':'Proximates'},
			'Calcium, Ca':{'unit':'mg', 'group':'Minerals'},
			'Iron, Fe':{'unit':'mg', 'group':'Minerals'},
			'Magnesium, Mg':{'unit':'mg', 'group':'Minerals'},
			'Phosphorus, P':{'unit':'mg', 'group':'Minerals'},
			'Potassium, K':{'unit':'mg', 'group':'Minerals'},
			'Sodium, Na':{'unit':'mg', 'group':'Minerals'},
			'Zinc, Zn':{'unit':'mg', 'group':'Minerals'},
			'Vitamin C, total ascorbic acid':{'unit':'mg', 'group':'Vitamins'},
			'Thiamin':{'unit':'mg', 'group':'Vitamins'},
			'Riboflavin':{'unit':'mg', 'group':'Vitamins'},
			'Niacin':{'unit':'mg', 'group':'Vitamins'},
			'Vitamin B-6':{'unit':'mg', 'group':'Vitamins'},
			'Folate, DFE':{'unit':'micro g', 'group':'Vitamins'},
			'Vitamin B-12':{'unit':'micro g', 'group':'Vitamins'},
			'Vitamin A, RAE':{'unit':'micro g', 'group':'Vitamins'},
			'Vitamin A, IU':{'unit':'IU', 'group':'Vitamins'},
			'Cholesterol':{'unit':'mg', 'group':'Lipids'},
			'Caffeine':{'unit':'mg', 'group':'Other'}}

micro_unicode = u'\xb5'

"""CatsKillDeadMen"""