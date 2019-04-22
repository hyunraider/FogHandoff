import fog_pb2 as proto
import socket
import struct
import time

flag = False

def send_data(sock, message):
    if(sock == None):
        return
    sock.send(struct.pack(">I", len(message)))
    sock.send(message)

def send_connection_message(sock, edge_id, fog_port):
    conn = proto.ConnectionMessage()
    conn.edgeId = edge_id
    conn.type = proto.ConnectionMessage.NEW
    acceptMsg = proto.AcceptMessage()
    send_data(sock, bytearray(conn.SerializeToString()))
    print ("Sent Connection Message to %d" % fog_port)

    length = sock.recv(4)
    length = struct.unpack("!i", length)[0]
    total_data = b""
    while len(total_data) < length:
        data = sock.recv(1024)
        if not data:
            break
        total_data += data
    acceptMsg.ParseFromString(total_data)
    print(acceptMsg)

def send_task_message(sock, delta_lat, delta_long, location, edge_id):
    task = proto.TaskMessage()
    task.edgeId = edge_id #TODO
    task.type = proto.TaskMessage.INFO

    v = proto.Velocity()
    v.deltaLatitude = delta_lat
    v.deltaLongitude = delta_long
    v.loc = location

    task.velocity = v
    send_data(sock, bytearray(task.SerializeToString()))

def send_dumb_task_message(sock, edge_id, fog_port):
    task = proto.TaskMessage()
    task.edgeId = edge_id
    task.type = proto.TaskMessage.INFO
    send_data(sock, bytearray(task.SerializeToString()))
    print("Sent Dumb Task Message to %d" % fog_port)

def send_kill_message(sock, edge_id, fog_port):
    task = proto.TaskMessage()
    task.edgeId = edge_id
    task.type = proto.TaskMessage.KILL
    print("Sent Kill Message to %d" % fog_port)
    send_data(sock, bytearray(task.SerializeToString()))

def connect_to(ip_addr, port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((ip_addr, port))
    return sock

if __name__ == '__main__':
    # testing
    sock = connect_to('localhost', "1", 9001)
    send_connection_message(sock, "1", 9001)
    time.sleep(1)
    send_dumb_task_message(sock, "1")
    send_kill_message(sock, "1", 9001)
    exit()
    loc = proto.Location()
    loc.latitude = 20.0
    loc.longitude = 30.0
    send_task_message(sock, 10.0, 10.0, loc)
