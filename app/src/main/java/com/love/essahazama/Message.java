package com.love.essahazama;

public class Message {
    private String id;
    private String sender;
    private String text;
    private String timestamp;

    public Message() {
        // Required for Firebase
    }

    public Message(String id, String sender, String text, String timestamp) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public String getTimestamp() { return timestamp; }
}
