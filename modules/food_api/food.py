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
import os, glob
from random import shuffle

debug = 0

class FoodAPI:

	def send_request(self, url, param_dict = {}):
		'''
		the function for sending the http request
		'''
		r = requests.get(url, params = param_dict)
		cherrypy.response.headers["content-type"] = "application/json"
		if (r.status_code < 200 or r.status_code > 399):
			return None
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
		params = {"api_key":common["api_key"], "q": term, "max":max, "offset":offset, "sort":"r", "format":common["format"]}
		results = self.send_request(url["search"], params)
		new_r = {"items":[]}
		if results:
			items = results["list"]["item"]
			for item in items:
				new_dict = {"foodname": item["name"], "ndbno": item["ndbno"]}
				new_r["items"].append(new_dict)
		return new_r

	def db_search(self, term = "", max = ITEM_NUM, offset = 0):
		'''
			search the food from our database
		'''
		(d,c) = connect_db()
		chars = list(term)
		pattern = "%" + "%".join(chars) + "%"
		c.execute("Select foodname, ndbno from Food where foodname like '%s' limit %d, %d;" % (pattern, offset, max))
		db_results = c.fetchall()
		items = list(db_results)
		new_r = {"items": items}
		return new_r

	def search_food(self, term="", max=ITEM_NUM, dboffset=0, apioffset=0):
		'''
			search the food from our database first and then from the American database
		'''
		dboffset = int(dboffset)
		apioffset = int(apioffset)
		if dboffset and apioffset:
			raise Exception("Either dboffset or apioffset must be 0.")
		if ((dboffset == 0) and (apioffset == 0)) or ((dboffset != 0) and (apioffset == 0)):
			result = self.db_search(term, max, dboffset)
			length = len(result["items"])
			if length < max:
				dboffset = 0
				left = max-length
				more = self.api_search(term, max = left)
				for item in more["items"]:
					result["items"].append(item)
				apioffset = len(more["items"])
		else:
			result = self.api_search(term, max, apioffset)
			apioffset += len(result["items"])
		result["dboffset"] = dboffset
		result["apioffset"] = apioffset
		return json.dumps(result)
	search_food.exposed = True

	def get_food_report(self, ndbno):
		'''
			get nutrient report of a food.
			look for the food in our database first and if not found
			send the request to the American database
			and store the information into our database
		'''
		(d,c) = connect_db()
		c.execute("select * from Food where Ndbno='%s'" % ndbno)
		r = c.fetchone()
		if r:
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
			if not result:
				return json.dumps({})
			new_report = {"ndbno":result["report"]["food"]["ndbno"],
						"name":result["report"]["food"]["name"],
						"nutrients":{}}
			nutrients_list = result["report"]["food"]["nutrients"]
			field_names = "FoodName,Ndbno,"
			field_values = "'" + new_report["name"]+"','"+new_report["ndbno"]+"',"
			for nutrient in nutrients_list:
				if nutrient["name"] not in nutrients.keys():
					continue
				new_report["nutrients"][nutrient["name"]] = {"unit":nutrient["unit"],"value":nutrient["value"]}
				field_names += nutrients[nutrient["name"]]["db_field"] + ","
				field_values += nutrient["value"] + ","
			sql_str = "insert into Food (%s) values (%s);" % (field_names[:-1], field_values[:-1])
			c.execute("select FoodName from Food where Ndbno='%s';" % new_report["ndbno"])
			if not c.fetchall():
				try:
					c.execute(sql_str)
					d.commit()
				except:
					d.rollback()
			else:
				pass
		return json.dumps(new_report)
	get_food_report.exposed = True

	def get_random_filenames(self, subdir_name, file_type):
		'''
			a service to get the list of files in a specific folder
			specially for the carousel to get the images
		'''
		file_list = []
		os.chdir("/home/operat/team_random"+subdir_name)
		for file in glob.glob("*."+file_type):
			file_list.append(file)
		shuffle(file_list)
		result = {"files":file_list}
		return json.dumps(result)
	get_random_filenames.exposed = True

	def user_bmi(self, user_id):
		(db, c) = connect_db()
		c.execute("select height, weight from User where UserID = %s;" % user_id)
		r = c.fetchone()
		bmi = r["weight"]/((float(r["height"])/100)**2)
		r["bmi"] = round(bmi,2)
		return json.dumps(r)
	user_bmi.exposed = True


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
		cherrypy.quickstart(FoodAPI(), '/food', conf)
	else:
		a = FoodAPI()
		a.api_search("")
