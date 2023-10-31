package cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.time.LocalTime;


import org.json.JSONObject;
import org.w3c.dom.events.Event;

public class Main {

    private static HttpClient client;
    private static URI uri; 

    @Command(name = "event", mixinStandardHelpOptions = true)
    private static class EventCommand implements Callable<Integer> {

        @Parameters(index = "0")
        String date;

        @Parameters(index = "1")
        String time;

        @Parameters(index = "2")
        String ampm;

        @Parameters(index = "3")
        String title;

        @Parameters(index = "4")
        String description;

        @Parameters(index = "5")
        String hostEmail;

        @Option(names = {"-ei", "--event-id"})
        String eventID;

        @Override
        public Integer call() {
            try {
                JSONObject ev = new JSONObject();

                LocalTime evTime = LocalTime.parse(time);
                if(ampm.equalsIgnoreCase("pm")){
                    evTime.plusHours(12);
                }

                ev.put("date", date);
                ev.put("time", evTime);
                ev.put("title", title);
                ev.put("desc", description);
                ev.put("email", hostEmail);
                ev.putOpt("uuid", eventID);
                System.out.println(ev.toString());

                uri = URI.create("http://ec2-54-145-190-43.compute-1.amazonaws.com:3000/api/event");
                
                HttpRequest request = HttpRequest.newBuilder(uri).
                        POST(BodyPublishers.ofString(ev.toString()))
                        .header("Content-type","application/x-www-form-urlencoded")
                        .build();

                var response = client.send(request, BodyHandlers.discarding());
                //assertEquals(200, response.statusCode());
                    System.out.println("Failed to create event"); 


            } catch(IOException | InterruptedException e){
                System.out.println("Failed to create event: " + e.getMessage());
            }
            return 0;
        }
    }

    @Command(name = "participant", mixinStandardHelpOptions = true, version="1.0")
    private static class ParticipantCommand implements Callable<Integer> {

        @Parameters(index = "0")
        String eventID;

        @Parameters(index = "1")
        String name;

        @Parameters(index = "2")
        String email;

        @Option(names = {"-pi", "--participant-id"})
        String participantID;

        @Override
        public Integer call() {
            try {
                JSONObject pp = new JSONObject();

                pp.put("eventID", eventID);
                pp.put("name", name);
                pp.put("email", email);
                pp.putOpt("uuid", participantID);
                
                uri = URI.create("http://ec2-54-145-190-43.compute-1.amazonaws.com:3000/api/participant");
                
                HttpRequest request = HttpRequest.newBuilder(uri).
                        POST(BodyPublishers.ofString(pp.toString()))
                        .header("Content-type","application/x-www-form-urlencoded")
                        .build();

                HttpResponse response = client.send(request, BodyHandlers.ofString());


            } catch(IOException | InterruptedException e){
                System.out.println("Failed to create participant: " + e.getMessage());
            }
            return 0;
        }
    }
 
    @Command(name = "list-events", mixinStandardHelpOptions = true, version="1.0")
    private static class ListEventsCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            try{
                uri = URI.create("http://ec2-54-145-190-43.compute-1.amazonaws.com:3000/api/list-events");
                //List<Event> events = new ArrayList<>();
                //StringBuilder sb = new StringBuilder();

                HttpRequest request = HttpRequest.newBuilder(uri).
                            GET()
                            .header("Content-type","application/x-www-form-urlencoded")
                            .build();

                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                String eList = response.body();
                System.out.println(eList);
            }
            catch(IOException | InterruptedException e){
                System.out.println("Failed to retrieve event list: " + e.getMessage());
            }
/* 
                for(Event event : events){
                    sb.append(String.format(
                            "%s on %s at %s | Host Email: %s | Event ID: %s | '%s' \n\n",
                            event.title(),
                            event.eventDateTime().toLocalDate().format(
                                    DateTimeFormatter.ISO_LOCAL_DATE
                            ),
                            //event.eventDateTime().getHour(), event.eventDateTime().getMinute(),
                            event.eventDateTime().toLocalTime().format(
                                    DateTimeFormatter.ofPattern("hh:mm a")
                            ),
                            event.hEmail(),
                            event.uuid(),
                            event.description()
                    ));
                }

                System.out.println(sb);
                */
            return 0;
        }

    }
/* 
    @Command(name = "list-participants", mixinStandardHelpOptions = true, version="1.0")
    private static class ListParticipantsCommand implements Callable<Integer> {

        @Parameters(index = "0")
        String eventID;

        @Override
        public Integer call() throws SQLException{
            List<Participant> participants = dbClient.getParticipants(eventID);
            StringBuilder sb = new StringBuilder();

            for(Participant participant : participants){
                sb.append(String.format(
                        "%s | Email : %s | Participant ID: %s",
                        participant.name(),participant.email(), participant.uuid()
                ));
            }

            System.out.println(sb);
            return 0;
            }
            
        }
        
    }*/

    
    public static void main(String[] args) throws IOException, SQLException {

        client = HttpClient.newHttpClient();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;

        System.out.print("$> ");
        while((line = input.readLine()) != null){
            String[] words = line.split("\\s");
            String[] commandArgs = new String[words.length - 1];
            System.arraycopy(words, 1, commandArgs, 0, commandArgs.length);

            switch(words[0]){
                case "event" -> new CommandLine(new EventCommand()).execute(commandArgs);
                case "participant" -> new CommandLine(new ParticipantCommand()).execute(commandArgs);
                case "list-events" -> new CommandLine(new ListEventsCommand()).execute(commandArgs);
                //case "list-participants" -> new CommandLine(new ListParticipantsCommand()).execute(commandArgs);
                default -> System.out.println("Error: unknown command");
            }
            System.out.print("$> ");
        }
    }
}
