import fog_pb2 as proto
import socket
import struct

sock = None

def send_data(message):
    if(sock == None):
        return
    sock.write(struct.pack("H", len(message)))
    sock.write(message)

def send_task_message(delta_lat, delta_long, location):
    task = proto.TaskMessage()
    task.edgeId = 1 #TODO
    task.type = OpType.INFO

    v = proto.Velocity()
    v.deltaLatitude = delta_lat
    v.deltaLongitude = delta_long
    v.loc = location 
    
    task.velocity = v
    send_data(bytearray(task.SerializeToString()))

def connect_to(ip_addr, port):
    sock = socket.socket(socket.AF_INET, sock.SOCK_STREAM)
    sock.connect((ip_addr, port))


if __name__ == '__main__':
    # testing
    connect_to('localhost', 7777)
    Location loc = proto.Location()
    loc.latitude = 20.0
    loc.longitude = 30.0
    send_task_message(10.0, 10.0, loc)