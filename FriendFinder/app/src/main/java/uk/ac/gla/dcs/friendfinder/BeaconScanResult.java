package uk.ac.gla.dcs.friendfinder;

import org.altbeacon.beacon.Beacon;

import java.util.Date;

public class BeaconScanResult {
    public String beacon;
    public String name;
    public Date date;
    public double strength;

    public BeaconScanResult(String beacon, String name, Date date, double strength) {
        this.beacon = beacon;
        this.name = name;
        this.date = date;
        this.strength = strength;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof BeaconScanResult))return false;
        BeaconScanResult otherMyClass = (BeaconScanResult) other;

        return otherMyClass.beacon.toString().equals(this.beacon.toString());
    }
}
