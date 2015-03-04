import cherrypy
import random

class HelloTeam(object):
    def index(self):
        return "Hello Team" + str(random.randint(1,10000)) + "!"
    index.exposed = True

cherrypy.config.update({'server.socket_port': 8080})
cherrypy.config.update({'server.socket_host': "45.56.85.191"})    
cherrypy.quickstart(HelloTeam())
