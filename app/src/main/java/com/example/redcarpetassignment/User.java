package com.example.redcarpetassignment;

public class User {
    private String number = "";
    private String url = "";
    private int visit_count;

    public User() {
    }

    public User(String number, String url, int visit_count) {
        this.number = number;
        this.url = url;
        this.visit_count = visit_count;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVisit_count() {
        return visit_count;
    }

    public void setVisit_count(int visit_count) {
        this.visit_count = visit_count;
    }
}
