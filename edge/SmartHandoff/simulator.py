import time
import fog_pb2 as proto
import googlemaps as gm
from datetime import datetime
import pprint
import polyline

interval = 300
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
    print x, y, dx, dy

def test():
    loc = proto.Location()
    loc.longitude = 0.0000323
    loc.latitude = 0.00012341
    ser = bytearray(loc.SerializeToString())
    print(ser)

if __name__ == '__main__':
    #parse_simulation("test")
    #gmaps = gm.Client(key="AIzaSyAgIf9YhLFUikyJaicEzeQUVv---4n7a0Y")
    simulator1()
