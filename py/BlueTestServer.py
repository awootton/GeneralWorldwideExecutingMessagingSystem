#!/usr/bin/env python3

"""
A simple Python script to receive messages from a client over
Bluetooth using Python sockets (with Python 3.3 or above).

fails on mac. works in ubuntu bit not with this address. 


"""

import socket

import bluetooth

import platform
print(platform.python_version())

#s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#s.bind(("192.168.1.17",50001)) # The Bluetooth MAC Address and RFCOMM port is replaced with an IP Address and a TCP port.


hostMACAddress = '34-36-3B-C4-2C-17' # The MAC address of a Bluetooth adapter on the server. The server might have multiple Bluetooth adapters.
#hostMACAddress = '127.0.0.1' 
port = 3 # 3 is an arbitrary choice. However, it must match the port used by the client.
#port = 8081
backlog = 1
size = 1024
#s = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM )
s.bind((hostMACAddress,port))
s.listen(backlog)
try:
    client, address = s.accept()
    while 1:
        data = client.recv(size)
        if data:
            print(data)
            client.send(data)
except:    
    print("Closing socket")    
    client.close()
    s.close()