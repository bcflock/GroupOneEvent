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

                ev.put("Date", date);
                ev.put("Time", time);
                ev.put("AMPM", ampm);
                ev.put("Title", title);
                ev.put("Description", description);
                ev.put("Host Email", hostEmail);
                ev.putOpt("Event ID", eventID);

                uri = URI.create("http://ec2-54-145-190-43.compute-1.amazonaws.com:3000/api/event");
                
                HttpRequest request = HttpRequest.newBuilder(uri).
                        POST(BodyPublishers.ofString(ev.toString()))
                        .header("Content-type","application/x-www-form-urlencoded")
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

                JSONObject ev = new JSONObject();

                ev.put("Event ID", eventID);
                ev.put("Name", name);
                ev.put("Email", email);
                ev.putOpt("Participant ID", participantID);
                if (participantID != null) {
                    //dbClient.addParticipant(
                    //        Participant.create(participantID, eventID, name, email)
                   // );
                } else {
                   // dbClient.addParticipant(
                     //       Participant.create(eventID, name, email)
                    //);
                }
            } catch (Event.HandledIllegalValueException e){
                System.out.println("Failed to create participant: " + e.getMessage());
            }
            return 0;
        }

    }

    @Command(name = "list-events", mixinStandardHelpOptions = true, version="1.0")
    private static class ListEventsCommand implements Callable<Integer> {

        @Override
        public Integer call() throws SQLException {
            List<Event> events = getEvents();
            StringBuilder sb = new StringBuilder();

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
            return 0;
        }

    }

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
                case "list-participants" -> new CommandLine(new ListParticipantsCommand()).execute(commandArgs);
                default -> System.out.println("Error: unknown command");
            }
            System.out.print("$> ");
        }
    }
}
