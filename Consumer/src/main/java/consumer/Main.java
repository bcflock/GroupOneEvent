package consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class Main {

    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
    private static final String GROUP_ID = "GroupOneEvent";
    private static final String EVENT_TOPIC = "events";
    private static final String PARTICIPANT_TOPIC = "participants";

    public static void main(String[] args){
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try{
            consumer.subscribe(Arrays.asList(EVENT_TOPIC, PARTICIPANT_TOPIC));

            while(true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for(ConsumerRecord<String, String> record : records){
                    //log.info("Key: " + record.key() + ", Value: " + record.value());
                    //log.info("Partition: " + record.partition() + ", Offset:" + record.offset());
                }
            }

        }catch(WakeupException e){
            //log.info("Wake up exception!");
            // we ignore this as this is an expected exception when closing a consumer
        }catch(Exception e){
            //log.error("Unexpected exception", e);
        }finally{
            consumer.close(); // this will also commit the offsets if need be.
            //log.info("The consumer is now gracefully closed.");
        }
    }

}
