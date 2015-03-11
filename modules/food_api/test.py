import requests
from constants import *

p = common
r = requests.get("http://api.nal.usda.gov/usda/ndb/list", params = p)
print type(r.text)
print type(r.json())
print ('item' in r.json().keys())
