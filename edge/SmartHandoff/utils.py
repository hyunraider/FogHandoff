import fog_pb2 as proto
import socket
import struct

def send_data(sock, message):
    if(sock == None):
        return
    sock.send(struct.pack(">I", len(message)))
    sock.send(message)

def send_connection_message(sock, edge_id):
    conn = proto.ConnectionMessage()
    conn.edgeId = "9000"
    conn.type = proto.ConnectionMessage.NEW
    acceptMsg = proto.AcceptMessage()
    send_data(sock, bytearray(conn.SerializeToString()))
    print "Sent Connection Message"
    data = sock.recv(1024)
    print(data)

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
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((ip_addr, port))
    return sock

if __name__ == '__main__':
    # testing
    sock = connect_to('localhost', 9001)
    send_connection_message(sock, 1)
    exit()
    loc = proto.Location()
    loc.latitude = 20.0
    loc.longitude = 30.0
    send_task_message(sock, 10.0, 10.0, loc)
