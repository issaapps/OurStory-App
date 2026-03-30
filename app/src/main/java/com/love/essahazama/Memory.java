package com.love.essahazama;

public class Memory {
    private final String emoji;
    private final String title;
    private final String date;
    private final String tag;
    private final String desc;    // optional description (new)
    private final int    bgColor;
    private       boolean favourite;

    public Memory(String emoji, String title, String date,
                  String tag, String desc, int bgColor) {
        this.emoji   = emoji;
        this.title   = title;
        this.date    = date;
        this.tag     = tag;
        this.desc    = desc;
        this.bgColor = bgColor;
        this.favourite = false;
    }

    // backward-compat constructor (no desc)
    public Memory(String emoji, String title, String date, String tag, int bgColor) {
        this(emoji, title, date, tag, "", bgColor);
    }

    public String  getEmoji()     { return emoji; }
    public String  getTitle()     { return title; }
    public String  getDate()      { return date; }
    public String  getTag()       { return tag; }
    public String  getDesc()      { return desc; }
    public int     getBgColor()   { return bgColor; }
    public boolean isFavourite()  { return favourite; }
    public void    setFavourite(boolean f) { favourite = f; }
}
