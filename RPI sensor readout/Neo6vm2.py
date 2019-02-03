import pynmea2
import serial
import time

gps_serial = serial.Serial("/dev/ttyUSB0", baudrate=38400, timeout=3.0)
prevNMEA = None
nextNMEA = None


def init():
    print("Initializing GPS")

    gps_serial.write(b'$PUBX,40,GLL,0,0,0,0*5C\r\n')
    gps_serial.write(b'$PUBX,40,GSA,0,0,0,0*4E\r\n')
    gps_serial.write(b'$PUBX,40,RMC,0,0,0,0*47\r\n')
    gps_serial.write(b'$PUBX,40,GSV,0,0,0,0*59\r\n')
    gps_serial.write(b'$PUBX,40,VTG,0,0,0,0*5E\r\n')
    gps_serial.flush()

    while (gps_serial.inWaiting()):
        gps_serial.read()


def read_loop(on_data):
    global prevNMEA
    global nextNMEA
    global gps_serial

    while True:
        while (gps_serial.inWaiting()):
            # print("IN WAITING")
            nmea_sentence = gps_serial.readline().decode('utf-8')
            # print(gps_serial.read())
            prevNMEA = nextNMEA
            nextNMEA = pynmea2.parse(nmea_sentence)

            on_data(prevNMEA, nextNMEA)

            time.sleep(1)

