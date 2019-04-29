from utils import *
from networking import *
import json

def dumb_simulation(points, fogs):
    global interval
    global distance
    curr_node = -1
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
        if curr_node == -1:
            curr_node = conn
            sock = connect_to('localhost', 9050+curr_node)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9050+curr_node])
            working_thread.start()

        elif conn != curr_node:
            print("Switch from %d to %d" % (9050+curr_node, 9050+conn))
            total_alive += time.time() - alive_time
            alive_time = None
            send_kill_message(sock, "1", 9050+curr_node)
            sock.close()
            curr_node = conn
            sock = connect_to('localhost', 9050+curr_node)
            working_thread = threading.Thread(target=send_connection_message, args=[sock, "1", 9050+curr_node])
            working_thread.start()

        else:
            if alive_time == None:
                alive_time = time.time()
            working_thread = None
            send_dumb_task_message(sock, "1", 9050+curr_node)

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
        dumb_simulation(points, fog_locations)

simulate("loop_simulation", "loop")
