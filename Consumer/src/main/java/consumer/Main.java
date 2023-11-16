package consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class Main {

    private static final String GATEWAY = "http://ec2-54-145-190-43.compute-1.amazonaws.com:3000";
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

        try(consumer){
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(Arrays.asList(EVENT_TOPIC, PARTICIPANT_TOPIC));

            while(true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for(ConsumerRecord<String, String> record : records){
                    if(record.topic().equals(EVENT_TOPIC)){
                        /*try{
                            JSONObject ev = new JSONObject();
                            ev.put("date", date);
                            ev.put("time", time);
                            ev.put("title", title);
                            ev.put("desc", description);
                            ev.put("email", hostEmail);
                            ev.putOpt("uuid", eventID);
                            uri = URI.create(GATEWAY + "/api/event");
                            HttpRequest request = HttpRequest.newBuilder(uri)
                                    .POST(HttpRequest.BodyPublishers.ofString(ev.toString()))
                                    .header("Content-type","application/json")
                                    .build();
                            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
                        }catch(IOException | InterruptedException e){
                            System.out.println("Failed to create event: " + e.getMessage());
                        }*/
                    }else if(record.topic().equals(PARTICIPANT_TOPIC)){

                    }else{
                        System.out.println("WUT DA HELLLLL");
                    }
                }
            }

        }catch(WakeupException e){
            //log.info("Wake up exception!");
            // we ignore this as this is an expected exception when closing a consumer
        }catch(Exception e){
            //log.error("Unexpected exception", e);
        }
        // this will also commit the offsets if need be.
        //log.info("The consumer is now gracefully closed.");
    }

}
