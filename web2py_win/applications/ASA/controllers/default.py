# -*- coding: utf-8 -*- 

#########################################################################
## This is a samples controller
## - index is the default action of any application
## - user is required for authentication and authorization
## - download is for downloading files uploaded in the db (does streaming)
## - call exposes all registered services (none by default)
#########################################################################  

import datetime

def index():
    if auth.user:
        postcard = db(db.postcard.created_by==auth.user_id).select().first()
        form = crud.update(db.postcard,postcard,next=URL(r=request))
    else:
        form = None    
    postcards = db(db.postcard.id>0).select()
    pracownicy = db(db.pracownik.imei>0).select()
    #logi       = db(db.log.id>0).select()
    return dict(form = form, postcards=postcards, pracownicy=pracownicy)  

def user():
    """
    exposes:
    http://..../[app]/default/user/login 
    http://..../[app]/default/user/logout
    http://..../[app]/default/user/register
    http://..../[app]/default/user/profile
    http://..../[app]/default/user/retrieve_password
    http://..../[app]/default/user/change_password
    use @auth.requires_login()
        @auth.requires_membership('group name')
        @auth.requires_permission('read','table name',record_id)
    to decorate functions that need access control
    """
    return dict(form=auth())
    
def zaznacz_obszar():
    txt = request.vars.id_prac
    txt_l = txt.split("?")
    id_prac=txt_l[0]
    
    pracownicy = db(db.pracownik.id==id_prac).select()
    return dict(form= None, pracownicy=pracownicy)
    
    
def history():
    txt = request.vars.id_prac
    txt_l = txt.split("?")
    id_prac=txt_l[0]
    if txt_l[1]=='n':    #ustawiamy nowy obszar
        print 'nowy_obszar'
        db(db.punkt_obszaru.id_pracownik==id_prac).delete()
        k = len(txt_l)
        i=2    #ile wczytanych
        while i+2<=k :
            db.punkt_obszaru.insert(id_pracownik = id_prac, latitude=txt_l[i], longitude=txt_l[i+1])   
            i=i+2
        okres = 10080
        akt_set = db(db.pracownik.id==id_prac)
        akt_set.update(nowy_obszar = 1)
       #normalna prosba o historie
    else:
        okres = int(txt_l[1])
    temp_str = str(datetime.datetime.today()-datetime.timedelta(minutes=okres))
    #query = (db.log.id_pracownik==id_prac) & (db.log.czas>'2012-01-07 23:34:30')
    query = (db.log.id_pracownik==id_prac) & (db.log.czas>temp_str)
    logi  = db(query).select()
    query = (db.punkt_obszaru.id_pracownik==id_prac)
    punkty_obszaru  = db(query).select()
    pracownicy = db(db.pracownik.id==id_prac).select()
    return dict(form= None, logi=logi, punkty_obszaru=punkty_obszaru, pracownicy=pracownicy)

def download():
    """
    allows downloading of uploaded files
    http://..../[app]/default/download/[filename]
    """
    return response.download(request,db)


def call():
    """
    exposes services. for example:
    http://..../[app]/default/call/jsonrpc
    decorate with @services.jsonrpc the functions to expose
    supports xml, json, xmlrpc, jsonrpc, amfrpc, rss, csv
    """
    session.forget()
    return service()
    
def new_position():
    txt = request.vars.position
    txt_l=txt.split("?")
    
    if db(db.pracownik.imei==txt_l[0]).select():    #jezeli istnieje taki pracownik w bazie danych
        id_prac = db(db.pracownik.imei==txt_l[0]).select().first().id     #id pracownika o danym imei
        akt_id = db.log.insert(id_pracownik = id_prac)    #nowy rekord do log, akt_id - id logu
        
        
        obszar_lat=[]
        obszar_lng=[]
        
        query_pkt = (db.punkt_obszaru.id_pracownik==id_prac) 
        punkty_obszaru  = db(query_pkt).select() 
        for row in punkty_obszaru:     #kolejne punkty obszaru
            obszar_lat.append(row.latitude)
            obszar_lng.append(row.longitude)
            

        lat = float(txt_l[1])
        lng = float(txt_l[2])
        
        
            
        czy_wewnatrz = point_inside_polygon(lat,lng,obszar_lat,obszar_lng)         
        #ustawienie tabeli log
        akt_set = db(db.log.id==akt_id)
        akt_set.update(latitude = txt_l[1])
        akt_set.update(longitude= txt_l[2])
        akt_set.update(dokladnosc= txt_l[3])
        akt_set.update(w_obszarze= czy_wewnatrz)
        akt_set.update(czas= datetime.datetime.today())
        #ustawienie tabeli pracownik
        akt_set = db(db.pracownik.imei==txt_l[0])
        akt_set.update(latitude = txt_l[1])
        akt_set.update(longitude= txt_l[2])
        akt_set.update(dokladnosc= txt_l[3])
        akt_set.update(w_obszarze= czy_wewnatrz)   
        akt_set.update(czas= datetime.datetime.today())
        print datetime.datetime.today(), 'aktualizuje [imei, lat, long, acc]:'
        print txt_l
        
        query = (db.pracownik.id==id_prac) 
        pracownicy  = db(query).select() 
        for row in pracownicy: 
            nowy_obszar = row.nowy_obszar
            
        if txt_l[3]=='0':
            nowy_obszar=1    
            
        if nowy_obszar==0:    #nie aktualizujemy
            return '# nie aktualizujemy obszaru'
            
        msg = ''
        query_pkt = (db.punkt_obszaru.id_pracownik==id_prac) 
        punkty_obszaru  = db(query_pkt).select() 
        for row in punkty_obszaru:     #kolejne punkty obszaru
            msg = msg + str(row.latitude )+' '
            msg = msg + str(row.longitude)+' '
        akt_set = db(db.pracownik.id==id_prac)
        akt_set.update(nowy_obszar = 0)
        return msg
    return '# nie ma takiego pracownika'
        #return XML(UL(*[LI(A('%s - %s ' % (row.model,row.year),_href=URL('details',args=row.id)))\
        #           for row in rows]))
        
def point_inside_polygon(x,y,p_lat, p_lng):

    n = len(p_lat)
    inside =False

    p1x = p_lat[0]
    p1y = p_lng[0]
    
    for i in range(n+1):
        p2x = p_lat[i%n]
        p2y = p_lng[i%n]
        if y > min(p1y,p2y):
            if y <= max(p1y,p2y):
                if x <= max(p1x,p2x):
                    if p1y != p2y:
                        xinters = (y-p1y)*(p2x-p1x)/(p2y-p1y)+p1x
                    if p1x == p2x or x <= xinters:
                        inside = not inside
        p1x,p1y = p2x,p2y

    return inside

def generate():
    txt = request.vars.id_prac
    txt_l = txt.split("?")
    id_prac=txt_l[0]
    
    pracownicy = db(db.pracownik.id==id_prac).select()
    return dict(form= None, pracownicy=pracownicy)

def add_points():
    txt = request.vars.id_prac
    txt_l = txt.split("?")
    id_prac=txt_l[0]
    if txt_l[1]=='n':    #ustawiamy nowy obszar
        print 'generate'
        k = len(txt_l)
        i=2    #ile wczytanych
        teraz = datetime.datetime.today() - datetime.timedelta(minutes=74)
        delta = datetime.timedelta(seconds=15)
        
        obszar_lat=[]
        obszar_lng=[]
        
        que_pkt = (db.punkt_obszaru.id_pracownik==id_prac) 
        punk_obszaru  = db(que_pkt).select() 
        for row in punk_obszaru:     #kolejne punkty obszaru
            obszar_lat.append(row.latitude)
            obszar_lng.append(row.longitude)
        
        while i+2<=k :
            czy_wewnatrz = point_inside_polygon(txt_l[i],txt_l[i+1],obszar_lat,obszar_lng)  #txt_l[i],txt_l[i+1]
            db.log.insert(id_pracownik = id_prac, czas = teraz, latitude=txt_l[i], longitude=txt_l[i+1], dokladnosc = 3.0, w_obszarze = czy_wewnatrz)   #todo
            i=i+2
            teraz=teraz+delta
        #ustawienie tabeli pracownik
        teraz=teraz-delta
        
        akt_set = db(db.pracownik.id==txt_l[0])
        akt_set.update(latitude = txt_l[i-2])
        akt_set.update(longitude= txt_l[i-1])
        akt_set.update(czas= teraz)
        akt_set.update(dokladnosc= 3.0)
        akt_set.update(w_obszarze= czy_wewnatrz)
        return 'dodano'
    return 'nie dodano'
