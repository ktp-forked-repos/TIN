'''
Created on 02-01-2012

@author: Piotr Jastrzebski
'''
import server
import serverData
import updater

#main application launcher
def main():    
#    updater.updateZones(serverData.WKimei, serverData.WKarray)
#    updater.updateZones(serverData.PJimei, serverData.PJarray) 
#    updater.updateZones(serverData.POimei, serverData.POarray) 
    
    server.runServer()
    
if __name__ == '__main__':
    main()
