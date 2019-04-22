import fog_pb2 as proto
import socket
import struct

def send_data(sock, message):
    if(sock == None):
        return
    sock.write(struct.pack("H", len(message)))
    sock.write(message)

def send_connection_message(sock, edge_id):
    conn = proto.ConnectionMessage()
    acceptMsg = proto.AcceptMessage()
    

def send_task_message(sock, delta_lat, delta_long, location, edge_id):
    task = proto.TaskMessage()
    task.edgeId = 1 #TODO
    task.type = OpType.INFO

    v = proto.Velocity()
    v.deltaLatitude = delta_lat
    v.deltaLongitude = delta_long
    v.loc = location

    task.velocity = v
    send_data(sock, bytearray(task.SerializeToString()))

def send_kill_message(sock, edge_id):
    task = proto.TaskMessage()
    task.edgeId = edge_id
    task.type = OpType.KILL
    send_data(sock, bytearray(task.SerializeToString()))

def connect_to(ip_addr, port):
    sock = socket.socket(socket.AF_INET, sock.SOCK_STREAM)
    sock.connect((ip_addr, port))
    return sock

if __name__ == '__main__':
    # testing
    sock = connect_to('localhost', 7777)
    Location loc = proto.Location()
    loc.latitude = 20.0
    loc.longitude = 30.0
    send_task_message(soc, 10.0, 10.0, loc)
