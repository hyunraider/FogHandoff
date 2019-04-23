from utils import *
from networking import *

def get_velocity_vectors(points):
    x = [e[0] for e in arr]
    y = [e[1] for e in arr]
    dx = [x[i+1]-x[i] for i in range(len(x)-1)]
    dy = [y[i+1]-y[i] for i in range(len(y)-1)]
    dx.append(0)
    dy.append(0)
    return x,y,dx,dy

def smart_simulation(points, fogs):



def simulation1():
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    points = parse_simulation("test")
    print(5*len(points))
    #draw_map(start, end, fog_locations, "simulation1.html")
    dumb_simulation(points, fog_locations)
