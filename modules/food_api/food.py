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

	def send_request(self, url, param_dict = {}):
		r = requests.get(url, params = param_dict)
		cherrypy.response.headers["content-type"] = "application/json"
		return r.json()
	
	'''def get_list(self, lt = 'f', max = 100, offset = 0, sort = "n"):
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
	get_list.exposed = True'''
	
	def api_search(self, term = "", max=ITEM_NUM, offset = 0):
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
		params = {"api_key":common["api_key"], "q": term, "max":ITEM_NUM, "offset":offset, "sort":"r", "format":common["format"]}
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
		return new_r

	def db_search(self, term = "", max = ITEM_NUM, offset = 0):
		(d,c) = connect_db()
		chars = list(term)
		pattern = "%" + "%".join(chars) + "%"
		c.execute("Select foodname, ndbno from Food where foodname like '%s' limit %d, %d;" % (pattern, offset, max))
		db_results = c.fetchall()
		items = list(db_results)
		new_r = {"items": items}
		return new_r

	@cherrypy.tools.json_in()
	def search_food(self):#, term="", max=100, dboffset=None, apioffset=None):
		data = cherrypy.request.json
		dboffset = data["dboffset"]
		apioffset = data["apioffset"]
		term = data["term"]
		max = ITEM_NUM
		if dboffset and apioffset:
			raise Exception("Either dboffset or apioffset must be None.")
		if (dboffset == None) and (apioffset == None):
			dboffset = 0
		if dboffset != None:
			result = self.db_search(term, max, dboffset)
			length = len(result["items"])
			if length < max:
				dboffset = None
				left = max-length
				more = self.api_search(term, left)
				for item in more["items"]:
					result["items"].append(item)
				apioffset = left
		else:
			result = self.api_search(term, max, apioffset)
			apioffset += max
		result["dboffset"] = dboffset
		result["apioffset"] = apioffset
		return json.dumps(result)
	search_food.exposed = True

	def get_food_report(self, ndbno):
		if (ndbno < MIN_NDBNO) and (ndbno > MAX_NDBNO):
			return json.dumps({})
		(d,c) = connect_db()
		c.execute("select * from Food where Ndbno='%s'" % ndbno)
		r = c.fetchone()
		if r:
			print r
			new_report = {"name":r["FoodName"], "ndbno":r["Ndbno"],"nutrients":{}}
			for n in nutrients.keys():
				unit = nutrients[n]["unit"]
				if "micro" in unit:
					unit = u'\xb5'+"g"
				value = r[nutrients[n]["db_field"]]
				if not value:
					value = 0
				new_report["nutrients"][n] = {"unit":unit, "value":value}
		else:
			params = {"api_key":common["api_key"], "format":common["format"], "type":"b","ndbno":ndbno}
			result = self.send_request(url["report"], params)
			new_report = {"ndbno":result["report"]["food"]["ndbno"],
						"name":result["report"]["food"]["name"],
						"nutrients":{}}
			nutrients_list = result["report"]["food"]["nutrients"]
			#f = open("nutrient_info.txt", "w")
			#f.write("nutrients = {")
			field_names = "FoodName,Ndbno,"
			field_values = "'" + new_report["name"]+"','"+new_report["ndbno"]+"',"
			for nutrient in nutrients_list:
				if nutrient["name"] not in nutrients.keys():
					continue
				new_report["nutrients"][nutrient["name"]] = {"unit":nutrient["unit"],"value":nutrient["value"]}
				field_names += nutrients[nutrient["name"]]["db_field"] + ","
				field_values += nutrient["value"] + ","
				#print nutrient.keys()
				#print nutrient["name"]
				#print nutrient["unit"]
				#if nutrient["name"] == "Folate, DFE" or nutrient["name"] == "Niacin":
					#print nutrient["name"]
					#print u'\xb5'
				#try:
					#f.write("'%s':{'unit':'%s', 'group':'%s'},\n" % (nutrient["name"], nutrient["unit"], nutrient["group"]))
				#except Exception as e:
					#print e.__str__()
					#if "u'\\xb5'" in e.__str__():
						#f.write("'%s':{'unit':'%s', 'group':'%s'},\n" % (nutrient["name"], "micro g", nutrient["group"]))
					#else:
						#raise e
			#f.write("}")
			#f.close()
			#print nutrients
			#for nutrient in nutrients.keys():
				#print nutrients[nutrient]["unit"]
			sql_str = "insert into Food (%s) values (%s);" % (field_names[:-1], field_values[:-1])
			print sql_str
			c.execute("select FoodName from Food where Ndbno='%s';" % new_report["ndbno"])
			if not c.fetchall():
				try:
					c.execute(sql_str)
					d.commit()
					#pass
				except:
					d.rollback()
			else:
				print "aaaaa"
		return json.dumps(new_report)
	get_food_report.exposed = True

	'''def get_food_from_db(self):
		(d,c) = connect_db()
		c.execute("select * from Food")
		print list(c.fetchall())'''

	'''@cherrypy.tools.json_in()
	def request_body_test(self):
		data = cherrypy.request.json
		print data["a"]
		return "abc"
	request_body_test.exposed = True'''



def connect_db():
	database = MySQLdb.connect(host=db["host"], user=db["user"], passwd=db["password"], db=db["schema"])
	cursor = database.cursor(MySQLdb.cursors.DictCursor)
	return (database, cursor)


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
		cherrypy.config.update({"server.socket_port": 8888})
		cherrypy.config.update({"server.socket_host":"45.56.85.191"})
		cherrypy.quickstart(FoodAPI(), '/', conf)
	else:
		a = FoodAPI()
		#a.get_list()
		#a.get_food_from_db()
		#a.search_food("pe")
		#a.get_food_report(ndbno="01010")
		a.db_search("")