import time
import fog_pb2 as proto
import googlemaps as gm
from datetime import datetime
import pprint

interval = 300
#Road east of UIUC Arboretum
pp = pprint.PrettyPrinter(indent=4)

def send_data():
    while True:
        pass

def simulator1(gmaps):
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    direction_result = gmaps.directions(start, end, mode="driving", departure_time=datetime.now())
    pp.pprint(direction_result)

def test():
    loc = proto.Location()
    loc.longitude = 0.0000323
    loc.latitude = 0.00012341
    ser = bytearray(loc.SerializeToString())
    print(ser)

if __name__ == '__main__':
    gmaps = gm.Client(key="AIzaSyAgIf9YhLFUikyJaicEzeQUVv---4n7a0Y")
    simulator1(gmaps)
