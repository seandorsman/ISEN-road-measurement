#! /usr/bin/python

from google.cloud import pubsub_v1 as pub
import os
import json
from datetime import datetime
from pynmea2 import NMEASentence

os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = './isen-roadeo-creds.json'

project_id = "isen-roadeo"
topic_name = "atest-pub"

publisher = pub.PublisherClient()
# The `topic_path` method creates a fully qualified identifier
# in the form `projects/{project_id}/topics/{topic_name}`
topic_path = publisher.topic_path(project_id, topic_name)


def send_payload_to_pubsub(mac_address, nmea_start: NMEASentence, nmea_end: NMEASentence, data):
    gps_start = {
        "latitude": nmea_start.latitude,
        "longitude": nmea_start.longitude
    }
    gps_end = {
        "latitude": nmea_end.latitude,
        "longitude": nmea_end.longitude
    }

    epoch = datetime.utcfromtimestamp(0)
    today = datetime.today()

    timestamp_start = (datetime.combine(today, nmea_start.timestamp) - epoch).total_seconds() * 1000
    timestamp_end = (datetime.combine(today, nmea_end.timestamp) - epoch).total_seconds() * 1000

    payload = {
        "mac": mac_address,
        "gps_start": gps_start,
        "gps_end": gps_end,
        "timestamp_start": timestamp_start,
        "timestamp_end": timestamp_end,
        "data": data
    }

    # Data must be a bytestring
    payload = json.dumps(payload).encode('utf-8')
    # text_file = open("output/%s.txt" % timestamp_start, "w")
    # text_file.write(payload.decode('utf-8'))
    # text_file.close()

    # When you publish a message, the client returns a future.
    future = publisher.publish(topic_path, data=payload)

    return future
