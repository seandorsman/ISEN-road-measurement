from google.cloud import pubsub_v1 as pub
import os
import json

os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = './isen-roadeo-creds.json'

project_id = "isen-roadeo"
topic_name = "atest-pub"

publisher = pub.PublisherClient()
# The `topic_path` method creates a fully qualified identifier
# in the form `projects/{project_id}/topics/{topic_name}`
topic_path = publisher.topic_path(project_id, topic_name)

for n in range(1, 10):
    data = {
        "x": n,
        "y": n+1,
        "z": n-1
    }
    # Data must be a bytestring
    data = json.dumps(data).encode('utf-8')
    # When you publish a message, the client returns a future.
    future = publisher.publish(topic_path, data=data)
    print('Published {} of message ID {}.'.format(data, future.result()))

print('Published messages.')