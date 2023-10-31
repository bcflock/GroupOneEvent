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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;


import org.json.JSONObject;
import org.w3c.dom.events.Event;

public class Main {

    private static HttpClient client;
    private static URI uri; 

    private static String GATEWAY = "http://ec2-54-145-190-43.compute-1.amazonaws.com:3000";
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

                int hour = Integer.parseInt(time.split(":")[0]);
                String minute = time.split(":")[1];
                if (hour > 11 ) {
                    hour = 0;
                }
                if (ampm.equals("PM")) {
                    hour += 12;
                }

                time = "%d:%s".formatted(hour, minute);
                ev.put("date", date);
                ev.put("time", time);
                ev.put("title", title);
                ev.put("desc", description);
                ev.put("email", hostEmail);
                ev.putOpt("uuid", eventID);
                System.out.println(date);
                uri = URI.create(GATEWAY + "/api/event");
                
                HttpRequest request = HttpRequest.newBuilder(uri)
                                .POST(BodyPublishers.ofString(ev.toString()))
                                .header("Content-type","application/json")
                                .build();

                client.send(request, BodyHandlers.discarding());

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
        public Integer call() throws SQLException {
            try {
                JSONObject pp = new JSONObject();

                pp.put("eventID", eventID);
                pp.put("name", name);
                pp.put("email", email);
                pp.putOpt("uuid", participantID);
                
                uri = URI.create(GATEWAY + "/api/participant");
                
                HttpRequest request = HttpRequest.newBuilder(uri).
                        POST(BodyPublishers.ofString(pp.toString()))
                        .header("Content-type","application/json")
                        .build();

                client.send(request, BodyHandlers.discarding());

            } catch(IOException | InterruptedException e){
                System.out.println("Failed to create participant: " + e.getMessage());
            }
            return 0;
        }
    }

    @Command(name = "list-events", mixinStandardHelpOptions = true, version="1.0")
    private static class ListEventsCommand implements Callable<Integer> {

        @Override
        public Integer call() throws SQLException {
            var request =HttpRequest.newBuilder()
                    .uri(URI.create(GATEWAY+"/api/list-events"))
                    .GET()
                    .build();
            try {
                var response = client.send(request, BodyHandlers.ofString());
                var body = new JSONObject(response.body());
                var array = body.getJSONArray("events");
                System.out.println(body);
                array.forEach(
                        (item) -> {
                            var obj = new JSONObject(item.toString());
                            System.out.println("Event ID: %s| Title: %s | Description: %s | Host Email: %s | Date: %s | Time(24h): %s".formatted(
                                    obj.get("uuid"),
                                    obj.get("title"),
                                    obj.get("desc"),
                                    obj.get("email"),
                                    obj.get("date"),
                                    obj.get("time")
                            ));
                        }
                );
            } catch (IOException | InterruptedException e){

            }
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
*/

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
