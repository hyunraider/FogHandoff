from utils import *
from networking import *
import json
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
    curr_port = -1
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
            curr_port = 9050 + curr_node
            sock = connect_to('localhost', curr_port)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", curr_port])
            working_thread.start()
            time.sleep(interval)
            continue

        if alive_time == None:
            alive_time = time.time()

        if idx == len(points)-1: #last case where dx,dy = 0
            send_dumb_task_message(sock, "1", curr_port)
            time.sleep(interval)
            continue

        working_thread = None
        candidates = send_task_message(sock, pos_vel[idx][0], pos_vel[idx][1], point[0], point[1], "1", curr_port)


        if candidates.exists == 1:
            ports = {}
            handoffIndex = []
            for c in candidates.candidates:
                port = int(c.fogPort)
                id = int(c.fogId) - 9050
                if id not in handoffIndex:
                    handoffIndex.append(id)
                    ports[id] = port

            handoffFogs = [x if (i in handoffIndex) else None for i, x in enumerate(fogs)]
            handoffFogs[curr_node] = fogs[curr_node]
            handoffNode = closest_point(handoffFogs, point)

            if handoffNode != curr_node: #Handoff is triggered
                print("Switch from %d to %d" % (curr_port, ports[handoffNode]))

                total_alive += time.time() - alive_time
                alive_time = None

                #Killing ports
                send_kill_message(sock, "1", curr_port)
                sock.close()

                #Set new curr node and curr port
                curr_node = handoffNode
                curr_port = int(ports[handoffNode])
                print(curr_port)
                sock = connect_to('localhost', curr_port)
                working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", curr_port])
                working_thread.start()
        time.sleep(interval)
    if alive_time:
        total_alive += time.time() - alive_time
        alive_time = None

    total_time = time.time()-start
    print("Simulation Complete in %.2f secs" % total_time)
    print("Time connected: %.2f secs, Connectivity: %.2f percent" % (total_alive, total_alive/total_time * 100))
    print("Car went %.2f mph over %.2f miles" % (distance/interval*3.6/1.6, len(points)*5/1000/1.6))


def simulate(simu_name, points_file):
    with open('../../src/main/resources/fogTopo.json', 'r') as file:
        data = file.read()
        parsed_json = json.loads(data)
        simulation_info = parsed_json[simu_name]

        points = parse_simulation(points_file)
        fog_locations = []
        for item in simulation_info["nodes"]:
            fog_locations.append((float(item["latitude"]), float(item["longitude"])))

        start = str(simulation_info["start_point"]["latitude"]) + "," + str(simulation_info["start_point"]["longitude"])
        end = str(simulation_info["end_point"]["latitude"]) + "," + str(simulation_info["end_point"]["longitude"])
        output_file = simu_name + ".html"
        draw_map(start, end, fog_locations, output_file)
        smart_simulation(points, fog_locations)

simulate("simulation1", "test")
#simulate("loop_simulation", "loop")
