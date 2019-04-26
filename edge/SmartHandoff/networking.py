import fog_pb2 as proto
import socket
import struct
import time

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

    length = b""
    while len(length) < 4:
        data = sock.recv(4-len(length))
        if not data:
            break
        length += data
    length = struct.unpack("!i", length)[0]
    total_data = b""
    while len(total_data) < length:
        data = sock.recv(1024)
        if not data:
            break
        total_data += data
    acceptMsg.ParseFromString(total_data)
    print("Connection Accepted from %d: %s" % (fog_port, acceptMsg))

def send_task_message(sock, delta_lat, delta_long, loc_x, loc_y, edge_id, fog_port):
    task = proto.TaskMessage()
    task.edgeId = edge_id #TODO
    task.type = proto.TaskMessage.INFO

    v = proto.Velocity()
    task.velocity.deltaLatitude = delta_lat
    task.velocity.deltaLongitude = delta_long
    task.velocity.loc.longitude = loc_y
    task.velocity.loc.latitude = loc_x
    task.velocity.speed = 1

    send_data(sock, bytearray(task.SerializeToString()))
    print("----------------------------------")
    print("Sent Task Message to %d" % fog_port)

    length = b""
    while len(length) < 4:
        data = sock.recv(4-len(length))
        if not data:
            break
        length += data
    length = struct.unpack("!i", length)[0]
    total_data = b""
    while len(total_data) < length:
        data = sock.recv(1024)
        if not data:
            break
        total_data += data

    candidates = proto.CandidateNodes()
    candidates.ParseFromString(total_data)
    print("Candidates received: %s" % (candidates.exists))


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
