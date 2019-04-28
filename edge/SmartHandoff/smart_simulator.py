from utils import *
from networking import *
import math

def get_velocity_vectors(points):
    x = [e[0] for e in points]
    y = [e[1] for e in points]
    dx = [x[i+1]-x[i] for i in range(len(x)-1)]
    dy = [y[i+1]-y[i] for i in range(len(y)-1)]
    normalized = [[dx[i]/math.sqrt(dx[i]**2 + dy[i]**2), dy[i]/math.sqrt(dx[i]**2 + dy[i]**2)] for i in range(len(dx))]
    normalized.append([0.0,0.0])
    return normalized

def smart_simulation(points, fogs):
    global interval
    global distance
    pos_vel = get_velocity_vectors(points)

    curr_node = -1
    sock = None
    time_dc = 0.0
    working_thread = None
    start = time.time()
    total_alive = 0.0
    alive_time = None

    for idx, point in enumerate(points):
        if working_thread and working_thread.isAlive():
            print("Blocked")
            time.sleep(interval)
            continue

        #First boot-up sequence is just distance based
        if curr_node == -1:
            curr_node = closest_point(fogs, point)
            sock = connect_to('localhost', 9050+curr_node)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9050+curr_node])
            working_thread.start()
            continue

        print(pos_vel[idx])
        send_task_message(sock, pos_vel[idx][0], pos_vel[idx][1], point[0], point[1], "1", 9050+curr_node)

        '''
        #Send pos,vel vectors
        if alive_time == None:
            alive_time = time.time()
        working_thread = None
        '''


def simulation1():
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    points = parse_simulation("test")
    #draw_map(start, end, fog_locations, "simulation1.html")
    smart_simulation(points, fog_locations)

simulation1()
