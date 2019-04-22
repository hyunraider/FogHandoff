import fog_pb2 as proto
import socket
import struct

def network_test():
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serversocket.bind(('localhost', 7777))
    serversocket.listen(1)
    (clientsocket, address) = serversocket.accept()

    amt_to_read = struct.unpack("H", serversocket.read(2))[0]
    data = serversocket.read(amt_to_read)
    task_message = proto.TaskMessage()
    task_message.ParseFromString(data)
    print(task_message.SerializeToString())

if __name__ == '__main__':
    network_test()