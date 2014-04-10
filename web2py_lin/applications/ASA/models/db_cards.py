GOOGLEMAP_KEY='ABQIAAAA2e9hcP6AjVfnY6GUEkSnABRS4ow2_WeqlXB9NuQVDDUrr3G6mhQNryy1G1KD7_Ks7lndRm69aKVu3A' # localhost
GOOGLEMAP_KEY='ABQIAAAA2e9hcP6AjVfnY6GUEkSnABRS4ow2_WeqlXB9NuQVDDUrr3G6mhQNryy1G1KD7_Ks7lndRm69aKVu3A' # web2py.com

db.define_table('postcard',
    Field('created_by',db.auth_user,default=auth.user_id,writable=False,readable=False),
    Field('created_on','datetime',default=request.now,writable=False,readable=False),
    Field('from_nickname',default=('%(first_name)s %(last_name)s' % auth.user) if auth.user else ''),
    Field('from_location',requires=IS_NOT_EMPTY()),
    Field('latitude','double'),
    Field('longitude','double'),
    Field('image','upload'),
    Field('comment','text'))


db.define_table('pracownik',
    Field('imei', 'integer'),
    Field('imie','text'),
    Field('nazwisko','text'),
    Field('stanowisko','text'),
    Field('czas','datetime'),
    Field('latitude','double'),
    Field('longitude','double'),
    Field('dokladnosc','double'),
    Field('w_obszarze','integer'),
    Field('image','upload'),
    Field('nowy_obszar','integer'))
    
db.define_table('log',
    Field('id_pracownik', db.pracownik),
    Field('czas','datetime'),
    Field('latitude','double'),
    Field('longitude','double'),
    Field('dokladnosc','double'),
    Field('w_obszarze','integer'))
    
db.define_table('punkt_obszaru',
    Field('id_pracownik', db.pracownik),
    Field('latitude','double'),
    Field('longitude','double'))
