from utils import *
from networking import *

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

def simulation1():
    fog_locations = [(40.092223, -88.211714), (40.093791, -88.211220), (40.094719, -88.212697)]
    start = "40.091919,-88.211532"
    end = "40.094997,-88.213801"
    points = parse_simulation("test")
    print(5*len(points))
    #draw_map(start, end, fog_locations, "simulation1.html")
    dumb_simulation(points, fog_locations)

simulation1()
