from json2xml import json2xml
from json2xml.utils import readfromjson

print(json2xml.Json2xml(readfromjson('schedule.json')).to_xml())
