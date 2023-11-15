from kafka import KafkaProducer
import uuid
import json
import requests as rq

SERVER  = 'localhost'
PORT    = 9092
words = []
def init_word_list():
    words = rq.get(
        "https://github.com/bevacqua/correcthorse/blob/master/wordlist.json"
    )
    print(words.raw)
def random_pair():
    pass


def random_event() -> (uuid.UUID, json.json):
    uid: uuid.UUID = uuid.uuid4()
    event = {}
    return uuid, event


if __name__ == "__main__":
    producer = KafkaProducer(bootstrap_servers=f'{SERVER}:{PORT}')
    init_word_list()
    for i in range(0, 100):
        random_event()
