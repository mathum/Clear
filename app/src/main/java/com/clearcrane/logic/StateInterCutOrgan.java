package com.clearcrane.logic;

public class StateInterCutOrgan extends Organism {

    private String source_url;
    private String interCutType;
    private String interCutTitle;

    public String getSource_url() {
        return source_url;
    }

    public String getInterCutType() {
        return interCutType;
    }


    public String getInterCutTitle() {
        return interCutTitle;
    }

    public StateInterCutOrgan(String url, String type, String title) {
        this.source_url = url;
        this.interCutType = type;
        this.interCutTitle = title;
    }

    public StateInterCutOrgan(String url) {
        this.source_url = url;

    }

}
