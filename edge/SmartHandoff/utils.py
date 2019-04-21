import fog_pb2 as proto
import socket

sock = None

def send_data(message):
    if(sock != None):
        sock.send(message)

def connect_to(ip_addr, port):
    sock = socket.socket(socket.AF_INET, sock.SOCK_STREAM)
    sock.connect((ip_addr, port))

