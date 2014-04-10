'''
Created on 02-01-2012

@author: Piotr Jastrzebski
'''

from itertools import cycle, izip

# encrypt server's response making a xor on string with unique IMEI number
def xorString(ss, key):
    key = cycle(key)
    return ''.join(chr(ord(x) ^ ord(y)) for (x,y) in izip(ss, key))
