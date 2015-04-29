"""
	Module Name: Food API
	
	Author: Regina Zhang
	
	Description:
		Just exist for creating the folder at the moment :p
		
	Module created on 11/03/2015
	
	Modification logs:
		12/03/2015: get_list function added
		12/03/2015: got CORS working
		12/03/2015: search_food function added
	
"""

import requests
import json
import unicodedata
from constants import *
import cherrypy
import MySQLdb

debug = 0

class FoodAPI:
	
	'''def __init__(self):
		self.params = {}
		self.url = None'''

	def send_request(self, url, param_dict):
		r = requests.get(url, params = param_dict)
		cherrypy.response.headers["content-type"] = "application/json"
		return r.json()
	
	def get_list(self, lt = 'f', max = 100, offset = 0, sort = "n"):
		"""
			get_list(lt, max, offset, sort)
			
			@params:
				lt: List type. food "f", all nutrients "n", pecial nutrients "ns",
					standard release nutrients only "nr", food group "g".
					Default = "f"
				max: The maximum number of items returned. Default = 10
				offset: The offset of the first item in the result. Default = 0
				sort: Sort the result by food name or id "n". Default = "n"
				
			@return:
				JSON object of the list of items requested.
				Including offset, id and food name.
		"""
		params = {"api_key":common["api_key"], "max":max, "offset":offset, "sort":sort}
		r = self.send_request(url["list"], params)
		#r = requests.get(self.url, params = self.params)
		#cherrypy.response.headers["content-type"] = "application/json"
		return json.dumps(r)#.json())
	get_list.exposed = True
	
	def search_food(self, term = "", max = 100, offset = 0, sort = "r"):
		"""
			search_food(term, max, offset, sort)
			
			@params:
				term: Search term. Default = ""
				max: The maximum number of items returned. Default = 10
				offset: The offset of the first item in the result. Default = 0
				sort: Sort the result by food name("n");
					sort by search relevance("r").
					Default = "r"
					
			@return:
				JSON object of the list of items requested.
		"""
		params = {"api_key":common["api_key"], "q": term, "max":max, "offset":offset, "sort":sort, "format":common["format"]}
		results = self.send_request(url["search"], params)
		#r = requests.get(self.url, params = self.params)
		#cherrypy.response.headers["content-type"] = "application/json"
		#return json.dumps(r.json())
		#results = r.json()
		items = results["list"]["item"]
		new_r = {"items":[]}
		for item in items:
			new_dict = {"name": item["name"], "ndbno": item["ndbno"]}
			new_r["items"].append(new_dict)
		return json.dumps(new_r)
	search_food.exposed = True

	def get_food_report(self, ndbno):
		params = {"api_key":common["api_key"], "format":common["format"], "type":"b","ndbno":ndbno}
		result = self.send_request(url["report"], params)
		new_report = {"ndbno":result["report"]["food"]["ndbno"],
					"name":result["report"]["food"]["name"],
					"nutrients":{}}
		nutrients_list = result["report"]["food"]["nutrients"]
		f = open("nutrient_info.txt", "w")
		f.write("nutrients = {")
		for nutrient in nutrients_list:
			new_report["nutrients"][nutrient["name"]] = {"unit":nutrient["unit"],"value":nutrient["value"]}
			#print nutrient.keys()
			#print nutrient["name"]
			#print nutrient["unit"]
			if nutrient["name"] == "Folate, DFE" or nutrient["name"] == "Niacin":
				print nutrient["name"]
				print u'\xb5'
			try:
				f.write("'%s':{'unit':'%s', 'group':'%s'},\n" % (nutrient["name"], nutrient["unit"], nutrient["group"]))
			except Exception as e:
				print e.__str__()
				if "u'\\xb5'" in e.__str__():
					f.write("'%s':{'unit':'%s', 'group':'%s'},\n" % (nutrient["name"], "u\\\'\\\\xb5\\\'g", nutrient["group"]))
				else:
					raise e
		f.write("}")
		f.close()
		print nutrients
		for nutrient in nutrients.keys():
			print nutrients[nutrient]["unit"]
		return json.dumps(new_report)
	get_food_report.exposed = True

	def get_food_from_db(self):
		cursor = connect_db()
		cursor.execute("select * from Food")
		print cursor.fetchall()


def connect_db():
	database = MySQLdb.connect(host=db["host"], user=db["user"], passwd=db["password"], db=db["schema"])
	return database.cursor()


def CORS():
	cherrypy.response.headers["Access-Control-Allow-Origin"] = "*"

if __name__ == '__main__':
	if not debug:
		conf = {
				'/': {
					'tools.CORS.on': True,
					'tools.response_headers.on': True,
					}
				}
		cherrypy.tools.CORS = cherrypy.Tool("before_finalize", CORS)
		cherrypy.config.update({"server.socket_port": 1234})
		#cherrypy.config.update({"server.socket_host":"45.56.85.191"})
		cherrypy.quickstart(FoodAPI(), '/', conf)
	else:
		a = FoodAPI()
		a.get_list()
		a.get_food_from_db()
