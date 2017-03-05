package uk.ac.gla.dcs.friendfinder;


import java.util.Date;

public class Friend {
    private long id;
    private String name;
    private String phone;
    private String beacon;
    private long lastStrength;
    private long timestamp;
    private long added;

    public Friend(long id, String name, String phone, String beacon, long lastStrength, long timestamp, long added) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.beacon = beacon;
        this.lastStrength = lastStrength;
        this.timestamp = timestamp;
        this.added = added;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getBeacon () {
        return beacon;
    }

    public long getLastStrength() {
        return lastStrength;
    }

    public Date getLastUpdate() {
        return new Date(timestamp);
    }
    @Override
    public String toString() {
        return this.name + " / " + this.phone + " / " + this.beacon + " / " + this.lastStrength + " / " + this.timestamp + " / " + this.added;
    }
}


