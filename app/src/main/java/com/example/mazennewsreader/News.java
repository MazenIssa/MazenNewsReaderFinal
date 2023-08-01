package com.example.mazennewsreader;

public class News {

    private String title;
    private String description;
    private String pubDate;
    private String link;
    private long id;

    public News()
    {

        this.title="";
        this.description="";
        this.pubDate="";
        this.link="";
    }

    public News(long id, String title, String desc, String date, String url) {
        this.id=id;
        this.title = title;
        this.description = desc;
        this.pubDate = date;
        this.link = url;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void setPubDate(String pubDate){
        this.pubDate = pubDate;
    }

    public String getPubDate(){
        return pubDate;
    }

    public void setLink(String link){
        this.link = link;
    }

    public String getLink(){
        return link;
    }


}
