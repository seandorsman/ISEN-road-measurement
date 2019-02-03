import Constant
import smbus2
from gpiozero import LED
from time import sleep
import math

mpu_pwr = LED(4)

bus = smbus2.SMBus(1)

def initMPU():
    bus.write_byte_data(Constant.MPU_ADDRESS, Constant.MPU_REG_POWER_OFF, 0)
    sleep(0.05)
    bus.write_byte_data(Constant.MPU_ADDRESS, Constant.MPU_REG_POWER_ON, 0)

def read_byte(reg):
    return bus.read_byte_data(Constant.MPU_ADDRESS, reg)

def read_word(reg):
    h = bus.read_byte_data(Constant.MPU_ADDRESS, reg)
    l = bus.read_byte_data(Constant.MPU_ADDRESS, reg+1)
    value = (h << 8) + l
    return value

def read_word_2c(reg):
    val = read_word(reg)
    if (val >= 0x8000):
        return -((65535 - val) + 1)
    else:
        return val

def getAccelerometerValues():
    try:
        x = read_word_2c(Constant.MPU_REG_ACCEL_X) / 16384.0
        y = read_word_2c(Constant.MPU_REG_ACCEL_Y) / 16384.0
        z = read_word_2c(Constant.MPU_REG_ACCEL_Z) / 16384.0

        return (x, y, z)
    except OSError:
        print("MPU CRASH")
        initMPU()       

    return (0, 0, 0)
