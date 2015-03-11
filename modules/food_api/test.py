import requests

p = {'api_key':'AB66LrhBWbvW9wEXTKCwuVkfclEnOxoWZinWjIY7'}
r = requests.get("http://api.nal.usda.gov/usda/ndb/list", params = p)
print r.text