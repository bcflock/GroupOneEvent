from kafka import KafkaProducer
import uuid
import json
import requests as rq
import random
SERVER  = 'localhost'
PORT    = 9092
EVENT_TOPIC = "events"
PARTICIPANT_TOPIC = "participants"
words = []

def init_word_list():
    global words
    words = rq.get(
        "https://raw.githubusercontent.com/bevacqua/correcthorse/master/wordlist.json"
    )
    words = json.loads(words.text)

def random_word():
    return words[random.randrange(len(words))]
def random_pair():
    return f'{random_word()} {random_word()}'
    
def random_mail():
    return f'{random_word()}.{random_word()}@{random_word()}.com'

def random_date():
    i = random.randint(1, 12)
    j = random.randint(1, 28)
    if i < 10: i=f'0{i}'
    if j < 10: j=f'0{j}'
    print(i, j)
    return f'20{random.randint(10,99)}-{i}-{j}'

def random_time():
    min = random.randint(0, 59)
    if min < 10: min = f'0{min}'  
    print(min)
    return f'{random.randint(0,23)}:{min}'

def random_event() -> (uuid.UUID, json):
    uid: uuid.UUID = uuid.uuid4()
    event = {
        'uuid': str(uid),
        'date': random_date(),
        'email': random_mail(),
        'name': random_pair(),
        'title': random_pair(),
        'desc': random_pair(),
        'time': random_time()
    }
    return uid, event

def random_participant(event_id: uuid.UUID) -> (uuid.UUID, json):
    uid: uuid.UUID = uuid.uuid4()
    participant = {
        "name" : random_pair(),
        "eventID": str(event_id),
        "email": random_mail(),
        "uuid": str(uid)
      }
    return participant

producer = KafkaProducer(bootstrap_servers=f'{SERVER}:{PORT}')
def publish(topic,  payload:json):
    producer.send(topic, json.dumps(payload).encode('ascii'))   

MAGIC_CONSTANT = 0xA #its magic because its hex
if __name__ == "__main__":
    init_word_list()
    events = []
    rand_multiplier = lambda : random.randint(5, 10)
    for i in range(0, 10 * rand_multiplier()):
        print(i)
        uid, event = random_event()
        publish(EVENT_TOPIC, event)
        for i in range(0, rand_multiplier()):
            participant = random_participant(uid)
            publish(PARTICIPANT_TOPIC, participant)