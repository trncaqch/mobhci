package uk.ac.gla.dcs.friendfinder;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FriendFinderApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private MainActivity monitoringActivity = null;
    private SQLiteDatabase database;

    public final long GENERIC_STRENGTH = 25000;

    public List<Region> regions;

    public void onCreate() {
        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        database = openOrCreateDatabase("friendfinder",MODE_PRIVATE,null);

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        setupRegions();

        backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager.setBackgroundBetweenScanPeriod(10000);
        beaconManager.setBackgroundMode(true);
    }

    public void setupRegions() {
        regionBootstrap = null;
        List<Friend> friends = DatabaseHelper.getInstance(getBaseContext()).getAllFriends();
        this.regions = new ArrayList<Region>();

        for(Friend friend : friends) {
            if(friend.getBeaconId1() == null && friend.getBeaconId2() == null && friend.getBeaconId3() == null) {
                Log.w(TAG, "setupRegions: Found friend with all-null regions");
            } else {
                Region region = new Region(Long.toString(friend.getId()),
                        Identifier.parse(friend.getBeaconId1()), Identifier.parse(friend.getBeaconId2()), Identifier.parse(friend.getBeaconId3()));
                Log.d(TAG, "setupRegions: Adding new region - " + region.toString());
                regions.add(region);
            }
        }
        regionBootstrap = new RegionBootstrap(this, regions);
    }

    @Override
    public void didEnterRegion(Region arg0) {
        if(arg0.getId1() == null && arg0.getId2() == null && arg0.getId3() == null) {
            return;
        }

        Log.d(TAG, "didEnterRegion: FOUND " + arg0.toString());
        if(monitoringActivity == null) {
            Log.d(TAG, "Sending notification.");
            DatabaseHelper.getInstance(getApplicationContext()).updateTimestampAndStrengthForBeacon(arg0.getId1().toString(), arg0.getId2().toString(), arg0.getId3().toString(), GENERIC_STRENGTH);
            sendNotification(arg0);
        } else {
            Log.d(TAG, "didRangeBeaconsInRegion: App is in foreground. No notification.");
        }
    }

    @Override
    public void didExitRegion(Region region) {
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
    }

    private void sendNotification(Region arg0) {
        Log.d(TAG, "sendNotification: sendNotification for " + arg0.getUniqueId());
        Friend friend = DatabaseHelper.getInstance(getApplicationContext()).getFriendById(Long.parseLong(arg0.getUniqueId()));
        if(friend != null) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setContentTitle(friend.getName() + getString(R.string.is_nearby))
                            .setContentText(getString(R.string.open_for_more))
                            .setSmallIcon(R.drawable.notification);

            builder.setVibrate(new long[] { 0, 500, 200, 500, 200 });

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());
        } else {
            Log.e(TAG, "sendNotification: Unable to find friend for notification ID");
        }
    }

    public void setMonitoringActivity(MainActivity activity) {
        this.monitoringActivity = activity;
    }

    public SQLiteDatabase getDb() {
        return database;
    }
}