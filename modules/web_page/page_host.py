import cherrypy
import os,os.path

path=os.path.abspath(os.path.dirname('index.html'))

class HealthOverflow(object):
    @cherrypy.expose
    def index(self):
        return file('index.html')

conf={
        '/css':
        { 'tools.staticdir.on':True,
          'tools.staticdir.dir': os.path.join(path,'css')
        }
        ,
        '/js':
        { 'tools.staticdir.on':True,
          'tools.staticdir.dir': os.path.join(path,'js')
        }
        ,
        '/asserts':
        { 'tools.staticdir.on':True,
          'tools.staticdir.dir': os.path.join(path,'asserts')
        },
        '/':
        { 'tools.staticdir.on':True,
          'tools.staticdir.dir': os.path.join(path)
        }

    }
cherrypy.server.socket_host="45.56.85.191"
cherrypy.quickstart(HealthOverflow(), '/', conf)
