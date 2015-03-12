import requests
from constants import *

p = common
r = requests.get("http://api.nal.usda.gov/usda/ndb/list", params = p)
print r.text
print r.json()
print ('list' in r.json().keys())
