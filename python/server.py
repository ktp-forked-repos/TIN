'''
Created on 19-12-2011
@author: Piotr Jastrzebski
'''
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
from serverData import web2py
from struct import *
from updater import updateZones
import SocketServer
import cgi
import encrypter
import serverData
import thread
import threading
import time
import urllib

class MyHandler(BaseHTTPRequestHandler):
    def address_string(self):                   #needed to speed up send_response function
        host, port = self.client_address[:2]
        return host

    def do_GET(self):
        try:
            self.send_response(200)
            return
        except IOError:
            self.send_error(404)
          
    def do_POST(self):
        global rootnode
        # odbieranie z telefonu
        try:          
            ctype, pdict = cgi.parse_header(self.headers.getheader('content-type'))
            if ctype == 'multipart/form-data':
                query = cgi.parse_multipart(self.rfile, pdict)
            self.send_response(200)
            self.end_headers()
            coordinates = query.get('upstring')
            coordinates = coordinates[0].split()
        except Exception, e:
            print "Wyjatek (Android -> Server): ", e
        else:
            imei = coordinates[0]
            phoneTS = coordinates[1]
            serverTS = time.time()
            print 'IMEI:', imei
            print 'Phone\'s Timestamp:', phoneTS,
            print '\nServer\'s Timestamp:', serverTS
            print 'Latitude, Longitude:', coordinates[2], ',', coordinates[3], '\n', 'Accuracy:', coordinates[4], 'm.\n'

                        
        #komunikacja z web2py        
            if(serverData.timestampMap.get(imei, -1) == -1):
                url = "http://" + web2py + ":8000/ASA/default/new_position?position=" + imei + "?" + coordinates[2] + "?" + coordinates[3] + "?0" 
            else:
                url = "http://" + web2py + ":8000/ASA/default/new_position?position=" + imei + "?" + coordinates[2] + "?" + coordinates[3] + "?" + coordinates[4] 
            try:                        
                data = urllib.urlopen(url).read()
                data = data.split()
                if(data[0] != "#"):
                    updateZones(imei, [data]) 
            except Exception, e: 
                print "Wyjatek (Web2Py): ", e 

        #odsylanie do telefonu
        put_hash_flag = False
        try:
            if(phoneTS == str(serverData.timestampMap.get(imei, 0))):
                self.wfile.write(pack('>i', 0))
                print 'Nic nie wyslano!\n'
            else:
                stringToBeSent = str(serverData.timestampMap.get(imei, 0)) + " "
                for each in serverData.zoneMap.get(imei, 0):
                    if(put_hash_flag):
                        stringToBeSent += "# # "
                    put_hash_flag = True
                    for each2 in each:
                        stringToBeSent += each2
                        stringToBeSent += " "
                print stringToBeSent, '\n'
                stringToBeSent = encrypter.xorString(stringToBeSent, imei)               
                self.wfile.write(pack('>i', len(stringToBeSent)))                    
                self.wfile.write(stringToBeSent)
        except Exception, e:
            print "Wyjatek (Server -> Android): ", e      
        else:
            self.wfile.close()                     
            
def runServer():    
    class HTTPServerMT(SocketServer.ThreadingMixIn, HTTPServer): pass          
    
    try:         
        server = HTTPServerMT(('', serverData.port), MyHandler)      
        print 'HTTPserverMT started. Port:', serverData.port
        server.serve_forever()
    except KeyboardInterrupt:
        print '^C received, shutting down server'
        server.socket.close()
        
