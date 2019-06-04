package com.clearcrane.logic;


public class Organism {

    private LifeCycle mLifeCycle;

    protected String name;

    private boolean isWorking = false;


        public boolean isWorking() {
        return isWorking;
    }


    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public Organism() {

    }


    /*
     * init Organism's lifecycle
     */
    public void init(String start_time, String end_time) {
        mLifeCycle = new LifeCycle(start_time, end_time);
    }

    public void init(String start_time, long duration) {
        mLifeCycle = new LifeCycle(start_time, duration);
    }


    public void init(String time_triggle) {
        mLifeCycle = new LifeCycle(time_triggle);
    }

    public boolean isAlive() {
        return mLifeCycle == null ? false : mLifeCycle.isInLifeCycle();
    }


}
