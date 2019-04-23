import numpy as np
import geopy.distance
import gmplot
import threading
import time

interval = .200

def parse_simulation(filename):
    locations = []
    with open('simulations/%s.txt' % filename, 'r') as f:
        for line in f.readlines():
            split = line.strip().split(' ')
            if len(split) == 3:
                split = split[1:]
            locations.append([float(x) for x in split])
    return locations

def closest_point(arr, pos):
    dist = []
    for fog in arr:
        dist.append(geopy.distance.distance(fog, pos).m)
    print(dist)
    return np.argmax(dist)

def draw_map(start, end, fog, name):
    lat_list = [x[0] for x in fog]
    long_list = [x[1] for x in fog]
    s = [float(x) for x in start.split(",")]
    e = [float(x) for x in end.split(",")]
    center = (s[0] + e[0]) / 2, (s[1] + e[1]) / 2
    gmap = gmplot.GoogleMapPlotter(center[0], center[1], 16, apikey="AIzaSyAgIf9YhLFUikyJaicEzeQUVv---4n7a0Y")
    gmap.scatter(lat_list, long_list, '#FF0000', size=10, marker=False)
    gmap.scatter([s[0], e[0]], [s[1], e[1]], "#0000FF", size=10, marker=True)
    gmap.draw('images/%s' % name)
