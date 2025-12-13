package model;

public class AlertMessage {

    private final String timestamp;
    private final String level;
    private final String section;
    private final String message;

    public AlertMessage(String timestamp, String level, String section, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.section = section;
        this.message = message;
    }

    @Override
    public String toString() {
        return "AlertMessage{" +
                "timestamp='" + timestamp + '\'' +
                ", level='" + level + '\'' +
                ", section='" + section + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public String getLevel(){
        return level;
    }
    public String getSection(){
        return section;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
