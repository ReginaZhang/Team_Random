"""
	Module Name: Food API
	
	Author: Regina Zhang
	
	Description:
		Just exist for creating the folder at the moment :p
		
	Module created on 11/03/2015
	
	Modification logs:
		12/03/2015: get_list function added
		12/03/2015: got CORS working
	
"""

import requests
import json
import unicodedata
from constants import common
from constants import list
import cherrypy

debug = 0

class FoodAPI:
	
	def __init__(self):
		self.params = {}
		self.url = None
	
	def get_list(self, lt = 'f', max = 10, offset = 0, sort = "n"):
		self.params = {"api_key":common["api_key"], "max":max, "offset":offset, "sort":sort}
		self.url = list["url"]
		r = requests.get(self.url, params = self.params)
		cherrypy.response.headers["content-type"] = "application/json"
		return json.dumps(r.json())
	get_list.exposed = True

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