'''
Created on 02-01-2012

@author: Piotr Jastrzebski
'''
import serverData
import time

#if zone in web interface changed, updates in server
def updateZones(uImei, uArray):
    try:
        if(serverData.zoneMap.get(uImei, -1) != -1):
            serverData.zoneMap.pop(uImei)
            serverData.timestampMap.pop(uImei)
        serverData.zoneMap[uImei] = uArray
        serverData.timestampMap[uImei] = time.time()
    except Exception, e:
            print "Wyjatek updater: ", e

