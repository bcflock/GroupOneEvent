from kafka import KafkaConsumer
import requests as rq
import threading
import time
SERVER  = 'localhost'
PORT    = 9092
EVENT_TOPIC = "events"
PARTICIPANT_TOPIC = "participants"
GATEWAY = "http://ec2-54-145-190-43.compute-1.amazonaws.com:3000/api"
EVENT_ENDPOINT = "event"
PARTICIPANT_ENDPOINT = "participant"
consumer = KafkaConsumer()
def events():
    consumer = KafkaConsumer(EVENT_TOPIC)
    for msg in consumer:
        while True:
            try:
                rsp = rq.post(f'{GATEWAY}/{EVENT_ENDPOINT}', data=msg.value)
                break
            except Exception:
                time.sleep(10)

def participants():
    consumer = KafkaConsumer(PARTICIPANT_TOPIC)
    for msg in consumer:
        while True:
            try:
                rsp = rq.post(f'{GATEWAY}/{PARTICIPANT_ENDPOINT}', data=msg.value)
                break
            except Exception:
                time.sleep(10)


if __name__ == "__main__":
    threading.Thread(target=events).start()
    threading.Thread(target=participants).start()