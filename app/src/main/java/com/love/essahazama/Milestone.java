package com.love.essahazama;

public class Milestone {
    private String id;
    private String title;
    private String date;
    private String emoji;

    public Milestone() {
        // Required for Firebase
    }

    public Milestone(String id, String title, String date, String emoji) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.emoji = emoji;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}
