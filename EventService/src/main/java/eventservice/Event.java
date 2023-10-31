package eventservice;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity("events")
public final class Event {

    private static final int TITLE_MAX_LENGTH = 255;
    private static final int  DESCRIPTION_MAX_LENGTH = 600;
    @Id String id;

    final UUID uuid;
    final LocalDateTime eventDateTime;
    final String title;
    final String description;
    final String hEmail;

    public Event(
            UUID uuid,
            LocalDateTime eventDateTime,
            String title,
            String description,
            String hEmail) {
        this.uuid = uuid;
        this.eventDateTime = eventDateTime;
        this.title = title;
        this.description = description;
        this.hEmail = hEmail;
    }

    //returns the LocalDateTime if the date and the time are valid
    private static LocalDateTime validateDateTime(String date, String time) throws HandledIllegalValueException {
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new HandledIllegalValueException("Date must be formatted YYYY-MM-DD, was " + date);
        }
        if (!time.matches("\\d{1,2}:\\d{1,2}")) {
            throw new HandledIllegalValueException("Time must be in the format \"HH:mm\", was " + time);
        }
        String[] dateSplit = date.split("-");
        String[] timeSplit = time.split(" |:");
        int hour = Integer.parseInt(timeSplit[0]);
        if (hour < 0 || hour > 23){
            throw new HandledIllegalValueException("Hour must be in the range [0,23]");
        }
        int minute = Integer.parseInt(timeSplit[1]);
        if (minute < 0 || minute > 59) {
            throw new HandledIllegalValueException("Minutes must be in the range [0, 59]");
        }
        int year = Integer.parseInt(dateSplit[0]);
        Month month = Month.of(Integer.parseInt(dateSplit[1]));
        int day = Integer.parseInt(dateSplit[2]);
        return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute));
    }

    public static String validateEmail(String email) throws HandledIllegalValueException {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        // note -- the actual email regex is much more sophisticated than this,
        // but there's no good way to add all the proper escapes for java, and
        // java has no raw strings
        if (!email.matches(emailRegex)) {
            throw new HandledIllegalValueException("Email %s is invalid".formatted(email));
        }
        return email;
    }

    public Event validateEmail() throws HandledIllegalValueException {
        //these should be handled
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new HandledIllegalValueException(String.format("Title may not exceed %d", TITLE_MAX_LENGTH));
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new HandledIllegalValueException(String.format("Description may not exceed %d", DESCRIPTION_MAX_LENGTH));
        }

        return this;
    }

    private static Event create(Optional<String> uuid, String date, String time, String title, String description, String hEmail) throws HandledIllegalValueException {
        UUID uid;
        try {
            uid = uuid.map(UUID::fromString).orElse(UUID.randomUUID());
        } catch (IllegalArgumentException e) {
            throw new HandledIllegalValueException("UUID is invalid, please try again");
        }
        return new Event(uid, validateDateTime(date, time), title, description, validateEmail(hEmail)).validateEmail();
    }

    public static Event create(String uuid, String date, String time, String title, String description, String hEmail) throws HandledIllegalValueException {
        return Event.create(Optional.ofNullable(uuid), date, time, title, description, hEmail).validateEmail();
    }

    public static class HandledIllegalValueException extends Exception {
        public HandledIllegalValueException(String message) {
            super(message);
        }
    }

    @Deprecated
    public static Event create(String date, String time, String title, String description, String hEmail) throws HandledIllegalValueException {
        return Event.create(UUID.randomUUID().toString(), date, time, title, description, validateEmail(hEmail));
    }

    public UUID uuid() {
        return uuid;
    }

    public LocalDateTime eventDateTime() {
        return eventDateTime;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String hEmail() {
        return hEmail;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Event) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.eventDateTime, that.eventDateTime) &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.hEmail, that.hEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, eventDateTime, title, description, hEmail);
    }

    @Override
    public String toString() {
        return "Event[" +
                "uuid=" + uuid + ", " +
                "eventDateTime=" + eventDateTime + ", " +
                "title=" + title + ", " +
                "description=" + description + ", " +
                "hEmail=" + hEmail + ']';
    }


}
