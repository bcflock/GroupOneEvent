package eventservice;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity("events")
public class EventPojo {
    @Id
    UUID uuid;
    LocalDateTime eventDateTime;
    String title;
    String description;
    String hEmail;

    public EventPojo(UUID uuid, LocalDateTime eventDateTime, String title, String description, String hEmail) {
        this.uuid = uuid;
        this.eventDateTime = eventDateTime;
        this.title = title;
        this.description = description;
        this.hEmail = hEmail;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String gethEmail() {
        return hEmail;
    }

    public void sethEmail(String hEmail) {
        this.hEmail = hEmail;
    }
}
