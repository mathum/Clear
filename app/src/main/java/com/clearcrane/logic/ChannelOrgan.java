package com.clearcrane.logic;

public class ChannelOrgan extends Organism {

    private String source_url;
    private String channelType;
    private String channelTitle;

    public String getSource_url() {
        return source_url;
    }

    public String getInterCutType() {
        return channelType;
    }


    public String getInterCutTitle() {
        return channelTitle;
    }

    public ChannelOrgan(String url, String type, String title) {
        this.source_url = url;
        this.channelType = type;
        this.channelTitle = title;
    }

    public ChannelOrgan(String url) {
        this.source_url = url;

    }

}
