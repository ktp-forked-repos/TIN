'''
Created on 31-12-2011

@author: Piotr Jastrzebski
'''
#server communication port
port = 8234
#web2py = "192.168.47.77"
web2py = "localhost"

#WKarray = [
#                    ["51.090693", "21.457744", "51.093928", "21.484351", "51.075057", "21.491132", "51.075758", "21.456885"], #WK Wies
#                    ["52.220018", "21.008638", "52.219913", "21.015333", "52.217284", "21.015", "52.217415", "21.009893"], # Politechnika
#                    ["52.174405", "21.004729", "52.179248", "21.019835", "52.175774", "21.039147", "52.165562", "21.037087", "52.166825", "21.007218"] #WK Waw
#          ]
#
#PJarray = [
#                    ["51.433708", "21.163412", "51.433832", "21.165429", "51.432722", "21.165537", "51.432692", "21.163391"], #PJ Rdm
#                    ["52.250298", "21.08463", "52.250193", "21.08845", "52.247911", "21.088761", "52.247306", "21.084148"], #PJ Waw
#                    ["52.220018", "21.008638", "52.219913", "21.015333", "52.217284", "21.015", "52.217415", "21.009893"] # Politechnika
#          ]
#
#POarray = [
#                    ["51.090693", "21.457744", "51.093928", "21.484351", "51.075057", "21.491132", "51.075758", "21.456885"], #WK Wies
#                    ["52.220018", "21.008638", "52.219913", "21.015333", "52.217284", "21.015", "52.217415", "21.009893"] # Politechnika
#          ]
#
#PJimei = 357160042368994
#WKimei = 354692046970949
#POimei = 353833048291063

#map keeps time of last zone update
timestampMap = {}

#map keeps zones for each IMEI
zoneMap = {}
