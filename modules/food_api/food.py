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

debug = 0

class FoodAPI:
	
	def __init__(self):
		self.params = {}
		self.url = None
	
	def get_list(self, lt = 'f', max = 10, offset = 0, sort = "n"):
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
		self.params = {"api_key":common["api_key"], "max":max, "offset":offset, "sort":sort}
		self.url = url["list"]
		r = requests.get(self.url, params = self.params)
		cherrypy.response.headers["content-type"] = "application/json"
		return json.dumps(r.json())
	get_list.exposed = True
	
	def search_food(self, term = "", max = 10, offset = 0, sort = "r"):
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
		self.params = {"api_key":common["api_key"], "q": term, "max":max, "offset":offset, "sort":sort}
		self.url = url["search"]
		r = requests.get(self.url, params = self.params)
		cherrypy.response.headers["content-type"] = "application/json"
		return json.dumps(r.json())
	search_food.exposed = True
		

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