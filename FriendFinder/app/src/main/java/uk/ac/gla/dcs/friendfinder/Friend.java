package uk.ac.gla.dcs.friendfinder;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.Date;

public class Friend {
    private long id;
    private String name;
    private String phone;
    private String beaconId1;
    private String beaconId2;
    private String beaconId3;
    private long lastStrength;
    private long timestamp;
    private long lastNotification;
    private long added;
    private long notifications;


    public Friend(long id, String name, String phone, String beaconId1, String beaconId2, String beaconId3, long lastStrength, long timestamp, long lastNotification, long added, long notifications) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.beaconId1 = beaconId1;
        this.beaconId2 = beaconId2;
        this.beaconId3 = beaconId3;

        this.lastStrength = lastStrength;
        this.timestamp = timestamp;
        this.lastNotification = lastNotification;
        this.added = added;
        this.notifications = notifications;
    }

    public Friend(long id, String name, String phone, Beacon beacon, long lastStrength, long timestamp, long lastNotification, long added, long notifications) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.beaconId1 = beacon.getId1().toString();
        this.beaconId2 = beacon.getId2().toString();
        this.beaconId3 = beacon.getId3().toString();

        this.lastStrength = lastStrength;
        this.timestamp = timestamp;
        this.lastNotification = lastNotification;
        this.added = added;
        this.notifications = notifications;
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

    public String getBeaconId1 () {
        return beaconId1;
    }

    public String getBeaconId2 () {
        return beaconId2;
    }

    public String getBeaconId3 () {
        return beaconId3;
    }

    public long getLastStrength() {
        return lastStrength;
    }

    public Date getLastUpdate() {
        return new Date(timestamp);
    }

    public Date getLastNotification() {
        return new Date(lastNotification);
    }

    public boolean notificationsEnabled() {
        return notifications != 0;
    }

    @Override
    public String toString() {
        return this.name + " / " + this.phone + " / " + this.beaconId1 + " / " + this.beaconId2 + " / " + this.beaconId3 + " / " + this.lastStrength + " / " + this.timestamp + " / " + this.lastNotification + " / " + this.added;
    }
}


