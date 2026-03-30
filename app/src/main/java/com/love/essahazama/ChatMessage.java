package com.love.essahazama;

public class ChatMessage {
    // أنواع الرسائل
    public static final String TYPE_TEXT    = "text";
    public static final String TYPE_IMAGE   = "image";
    public static final String TYPE_AUDIO   = "audio";
    public static final String TYPE_VIDEO   = "video";
    public static final String TYPE_STICKER = "sticker";

    private String id;
    private String sender;    // "essa" or "hazama"
    private String text;      // نص الرسالة
    private String type;      // نوع الرسالة
    private String mediaUrl;  // رابط الصورة/الصوت/الفيديو
    private long   timestamp;

    public ChatMessage() {}

    public ChatMessage(String sender, String text, long timestamp) {
        this.sender    = sender;
        this.text      = text;
        this.type      = TYPE_TEXT;
        this.timestamp = timestamp;
    }

    public ChatMessage(String sender, String type, String mediaUrl, String text, long timestamp) {
        this.sender    = sender;
        this.type      = type;
        this.mediaUrl  = mediaUrl;
        this.text      = text;
        this.timestamp = timestamp;
    }

    public String getId()        { return id; }
    public void   setId(String id){ this.id = id; }
    public String getSender()    { return sender; }
    public String getText()      { return text; }
    public String getType()      { return type != null ? type : TYPE_TEXT; }
    public String getMediaUrl()  { return mediaUrl; }
    public long   getTimestamp() { return timestamp; }

    public void setSender(String s)   { sender = s; }
    public void setText(String t)     { text = t; }
    public void setType(String t)     { type = t; }
    public void setMediaUrl(String u) { mediaUrl = u; }
    public void setTimestamp(long ts) { timestamp = ts; }
}
