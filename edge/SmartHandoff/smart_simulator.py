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
            time.sleep(interval)
            continue

        if alive_time == None:
            alive_time = time.time()

        if idx == len(points)-1: #last case where dx,dy = 0
            send_dumb_task_message(sock, "1", 9050+curr_node)
            time.sleep(interval)
            continue

        working_thread = None
        candidates = send_task_message(sock, pos_vel[idx][0], pos_vel[idx][1], point[0], point[1], "1", 9050+curr_node)

        handoffFogs = []
        if candidates.exists == 1:
            for c in candidates.candidates:
                port = int(c.fogId)-9050
                if port not in handoffFogs:
                    handoffFogs.append(port)

            handoffFogs = [x if (i in handoffFogs) else None for i, x in enumerate(fogs)]
            handoffFogs[curr_node] = fogs[curr_node]
            handoffNode = closest_point(handoffFogs, point)

            if handoffNode != curr_node: #Handoff is triggered
                print("Switch from %d to %d" % (9050+curr_node, 9050+handoffNode))
                total_alive += time.time() - alive_time
                alive_time = None
                send_kill_message(sock, "1", 9050+curr_node)
                sock.close()
                curr_node = handoffNode
                sock = connect_to('localhost', 9050+curr_node)
                working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9050+curr_node])
                working_thread.start()
        time.sleep(interval)
    if alive_time:
        total_alive += time.time() - alive_time
        alive_time = None

    total_time = time.time()-start
    print("Simulation Complete in %.2f secs" % total_time)
    print("Time connected: %.2f secs, Connectivity: %.2f percent" % (total_alive, total_alive/total_time * 100))
    print("Car went %.2f mph over %.2f miles" % (distance/interval*3.6/1.6, len(points)*5/1000/1.6))


def simulation1():
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    points = parse_simulation("test")
    #draw_map(start, end, fog_locations, "simulation1.html")
    smart_simulation(points, fog_locations)

simulation1()
