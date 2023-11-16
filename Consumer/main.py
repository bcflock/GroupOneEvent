from kafka import KafkaConsumer
import requests as rq

SERVER  = 'localhost'
PORT    = 9092
EVENT_TOPIC = "events"
PARTICIPANT_TOPIC = "participants"
GATEWAY = "http://ec2-54-145-190-43.compute-1.amazonaws.com:3000/api"
EVENT_ENDPOINT = "event"
PARTICIPANT_ENDPOINT = "participant"

def events():
    consumer = KafkaConsumer(EVENT_TOPIC, request_timeout_ms=30000)
    for msg in consumer:
        rsp = rq.post(f'{GATEWAY}/{EVENT_ENDPOINT}', data=msg.value)
def participants():
    consumer = KafkaConsumer(PARTICIPANT_TOPIC, request_timeout_ms=30000)
    for msg in consumer:
        rsp = rq.port(f'{GATEWAY}/{PARTICIPANT_ENDPOINT}')


if __name__ == "__main__":
    while(True):
        events()
        participants()