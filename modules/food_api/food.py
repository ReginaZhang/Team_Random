"""
	Module Name: Food API
	
	Author: Regina Zhang
	
	Description:
		Just exist for creating the folder at the moment :p
		
	Module created on 11/03/2015
	
	Modification logs:
	
"""

import requests
import unicodedata
from constants import common
from constants import list
import cherrypy

class FoodAPI:
	
	def __init__(self):
		self.params = {}
		self.url = None
	
	def get_list(self, lt = 'f', max = 10, offset = 0, sort = "n"):
		self.params = {"api_key":common["api_key"], "max":max, "offset":offset, "sort":sort}
		self.url = list["url"]
		r = requests.get(self.url, params = self.params)
		return r.text
	get_list.exposed = True
	
cherrypy.config.update({"server.socket_port": 1234})
#cherrypy.config.update({"server.socket_host":"45.56.85.191"})
cherrypy.quickstart(FoodAPI())