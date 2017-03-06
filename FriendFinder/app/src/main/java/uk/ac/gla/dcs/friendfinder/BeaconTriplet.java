package uk.ac.gla.dcs.friendfinder;

import java.util.Date;

public class BeaconTriplet {
    public String beaconId1;
    public String beaconId2;
    public String beaconId3;
    public Date date;

    @Override public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof BeaconTriplet))return false;
        BeaconTriplet otherMyClass = (BeaconTriplet)other;
        return (beaconId1 == otherMyClass.beaconId1) && (beaconId2 == otherMyClass.beaconId2) && (beaconId3 == otherMyClass.beaconId3);
    }

    @Override public String toString() {
        return beaconId1 + " / " + beaconId2 + " / " + beaconId3 + " // " + date.toString();
    }
}