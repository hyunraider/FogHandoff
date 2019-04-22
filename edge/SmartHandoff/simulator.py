import time
import fog_pb2 as proto
from datetime import datetime
import pprint
import geopy.distance
import numpy as np
import threading
from utils import *

interval = .200
#Road east of UIUC Arboretum
pp = pprint.PrettyPrinter(indent=4)

def send_data():
    while True:
        pass

def parse_simulation(filename):
    locations = []
    with open('simulations/%s.txt' % filename, 'r') as f:
        for line in f.readlines():
            split = line.strip().split(' ')
            if len(split) == 3:
                split = split[1:]
            locations.append([float(x) for x in split])
    return locations

def simulator1():
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    points = parse_simulation("test")
    simulator(start, end, fog_locations, points)

def simulator(start, end, fog, arr):
    x = [e[0] for e in arr]
    y = [e[1] for e in arr]
    dx = [x[i+1]-x[i] for i in range(len(x)-1)]
    dy = [y[i+1]-y[i] for i in range(len(y)-1)]
    dx.append(0)
    dy.append(0)

def test():
    loc = proto.Location()
    loc.longitude = 0.0000323
    loc.latitude = 0.00012341
    ser = bytearray(loc.SerializeToString())
    print(ser)

def closest_point(arr, pos):
    dist = []
    for fog in arr:
        dist.append(geopy.distance.distance(fog, pos).m)
    print(dist)
    return np.argmax(dist)

def dumb_simulation(points, fogs):
    global interval
    curr_node = None
    sock = None
    time_dc = 0.0
    working_thread = None

    for point in points:
        if working_thread and working_thread.isAlive():
            print("Blocked")
            time.sleep(interval)
            continue

        conn = closest_point(fogs, point)
        if not curr_node:
            start = time.time()
            curr_node = conn
            sock = connect_to('localhost', 9000+curr_node)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9000+curr_node])
            working_thread.start()

        elif conn != curr_node:
            print("Switch from %d to %d" % (9000+curr_node, 9000+conn))
            curr_node = conn
            send_kill_message(sock, "1", 9000+curr_node)
            sock = connect_to('localhost', 9000+curr_node)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9000+curr_node])
            working_thread.start()

        else:
            send_dumb_task_message(sock, "1", 9000+curr_node)

        time.sleep(interval)

def dumb_simulator1():
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    points = parse_simulation("test")

    dumb_simulation(points, fog_locations)

if __name__ == '__main__':
    #parse_simulation("test")
    #gmaps = gm.Client(key="AIzaSyAgIf9YhLFUikyJaicEzeQUVv---4n7a0Y")
    dumb_simulator1()
