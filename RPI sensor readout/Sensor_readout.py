import time
import threading
import Mpu
import Neo6vm2
import pubsub_connection
from pynmea2 import NMEASentence


sampleArray = []

def onStart():
    Mpu.initMPU()
    Neo6vm2.init()


def send(mac, gps_start, gps_end):
    global sampleArray
    send_list = sampleArray.copy()
    print("sample count: %d" % len(send_list))
    sampleArray.clear()    

    if gps_end == None or gps_start.latitude == 0.0: 
        print("GPS NOT AVAILABLE")
        send_list.clear()
        return

    pubsub_connection.send_payload_to_pubsub(mac, gps_start, gps_end, send_list)
    send_list.clear()


def onGPS(gps_start: NMEASentence, gps_end: NMEASentence):
    pubsub_thread = threading.Thread(target=send, args=("", gps_start, gps_end,))
    pubsub_thread.start()


if __name__ == '__main__':
    onStart()

    gps_thread = threading.Thread(target=Neo6vm2.read_loop, args=(onGPS,))
    gps_thread.start()

    while True:
        sampleArray.append(Mpu.getAccelerometerValues())


    # print("x=%f\ty=%f\tz=%f\t\tl=%f" % Mpu.getAccelerometerValues())
