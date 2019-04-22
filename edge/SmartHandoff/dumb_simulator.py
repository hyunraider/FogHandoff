from utils import *
from networking import *

def dumb_simulation(points, fogs):
    global interval
    curr_node = None
    sock = None
    time_dc = 0.0
    working_thread = None
    start = time.time()
    total_alive = 0.0
    alive_time = None
    for point in points:
        if working_thread and working_thread.isAlive():
            print("Blocked")
            time.sleep(interval)
            continue

        conn = closest_point(fogs, point)
        if not curr_node:
            curr_node = conn
            sock = connect_to('localhost', 9000+curr_node)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9000+curr_node])
            working_thread.start()

        elif conn != curr_node:
            print("Switch from %d to %d" % (9000+curr_node, 9000+conn))
            curr_node = conn
            total_alive += time.time() - alive_time
            alive_time = None
            send_kill_message(sock, "1", 9000+curr_node)
            sock = connect_to('localhost', 9000+curr_node)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9000+curr_node])
            working_thread.start()

        else:
            if alive_time == None:
                alive_time = time.time()
            send_dumb_task_message(sock, "1", 9000+curr_node)

        time.sleep(interval)
    if alive_time:
        total_time += time.time() - alive_time
        alive_time = None

    total_time = time.time()-start
    print("Simulation Complete in %.2f secs" % total_time)
    print("Time connected: %.2f secs, Connectivity: %.2f percent" % (total_alive, total_alive/total_time * 100))


def simulation1():
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    points = parse_simulation("test")
    print(5*len(points))
    #draw_map(start, end, fog_locations, "simulation1.html")
    dumb_simulation(points, fog_locations)

simulation1()
