package uk.ac.gla.dcs.friendfinder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private FriendListAdapter adapter;
    private Activity monitoringActivity;

    static final long SHORT_TIMEOUT_INTERVAL = 30 * 1000;
    static final long LONG_TIMEOUT_INTERVAL = 5 * 60 * 1000;

    private final int FULL_REFRESH_INTERVAL = 5000;
    private Handler mHandler;

    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ListView listView = (ListView) findViewById(R.id.listview);

        setAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                intent.putExtra("friendId", ((Friend)listView.getAdapter().getItem(position)).getId() + "");
                logToDisplay("Putting intent extra: " + ((Friend)listView.getAdapter().getItem(position)).getId() + "");
                startActivity(intent);
            }
        });

        FloatingActionButton addbutton = (FloatingActionButton) findViewById(R.id.addbutton);
        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        this.monitoringActivity = null;

        verifyBluetooth();
        logToDisplay("Application just launched");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.location_title));
                builder.setMessage(getString(R.string.location_body));
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                refreshScanResults(true);
            } finally {
                mHandler.postDelayed(mStatusChecker, FULL_REFRESH_INTERVAL);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.limited_title));
                    builder.setMessage(getString(R.string.limited_body));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshScanResults(true);
        ((FriendFinderApplication) this.getApplicationContext()).setMonitoringActivity(this);

        mHandler = new Handler();
        mStatusChecker.run();
        beaconManager.bind(this);
        logToDisplay("Setting backgroundMode to false");
        beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((FriendFinderApplication) this.getApplicationContext()).setMonitoringActivity(null);
        mHandler.removeCallbacks(mStatusChecker);
        beaconManager.unbind(this);
        logToDisplay("Setting backgroundMode to true");
        beaconManager.setBackgroundMode(true);
    }

    private void setAdapter() {
        ListView listView = (ListView) findViewById(R.id.listview);
        adapter = new FriendListAdapter(this.getBaseContext(), DatabaseHelper.getInstance(getBaseContext()).getAllFriends(), this);
        listView.setAdapter(adapter);
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.bluetooth_not_enabled));
                builder.setMessage(getString(R.string.please_enable_bluetooth));
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_le));
            builder.setMessage(getString(R.string.sorry_no_support_for_le));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();
        }
    }

    public void logToDisplay(final String line) {
        Log.d(TAG, "logToDisplay: " + line);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Log.d(TAG, "Tapped on Options menu");
        return true;
    }

    public synchronized void refreshScanResults(boolean full) {

        ListView listView = (ListView) findViewById(R.id.listview);
        logToDisplay("refreshScanResults: starting");

        List<Friend> friends = ((FriendListAdapter)listView.getAdapter()).values;

        if(full) {
            List<Friend> realFriends = DatabaseHelper.getInstance(getBaseContext()).getAllFriends();
            if(realFriends.size() != friends.size()) {
                logToDisplay("refreshScanResults: some friends are missing! -- refreshing");
                setAdapter();
            }
        }

        for(int i = 0; i < friends.size(); i++) {
            logToDisplay("refreshScanResults: iterating at " + i);

            View v = listView.getChildAt(i -
                    listView.getFirstVisiblePosition());
            if(v == null) {
                logToDisplay("refreshScanResults: listView not found");
            } else {
                logToDisplay("refreshScanResults: trying to fetch Friend with id " + friends.get(i).getId());
                Friend upToDateFriend = DatabaseHelper.getInstance(getBaseContext()).getFriendById(friends.get(i).getId());

                ImageView strengthIcon = (ImageView) v.findViewById(R.id.icon);
                TextView secondLine = (TextView) v.findViewById(R.id.secondLine);
                int drawable;

                if(upToDateFriend.getLastUpdate().after(new Date(System.currentTimeMillis() - SHORT_TIMEOUT_INTERVAL))) {
                    if (upToDateFriend.getLastStrength() < 10000) {
                        drawable = R.drawable.signal3;
                    } else if (upToDateFriend.getLastStrength() < 20000) {
                        drawable = R.drawable.signal2;
                    } else {
                        drawable = R.drawable.signal1;
                    }
                } else if(upToDateFriend.getLastUpdate().after(new Date(System.currentTimeMillis() - LONG_TIMEOUT_INTERVAL))) {
                    drawable = R.drawable.signal0;
                } else {
                    drawable = R.drawable.signalempty;
                }

                strengthIcon.setImageResource(drawable);
                secondLine.setText(getString(R.string.last_seen) + " " + DateUtils.getRelativeTimeSpanString(upToDateFriend.getLastUpdate().getTime()));
            }

            logToDisplay("refreshScanResults: updated");
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                logToDisplay("didRangeBeaconsInRegion -- DATABASE: " + DatabaseHelper.getInstance(getBaseContext()).getAllFriends().toString());

                boolean addedSomething = false;
                if (beacons.size() > 0) {
                    for(Beacon beacon : beacons) {
                        int updated = DatabaseHelper.getInstance(getBaseContext()).updateTimestampAndStrengthForBeacon(beacon, (int)(beacon.getDistance() * 10000));
                        logToDisplay("Set distance to " + (int)(beacon.getDistance() * 10000));
                        if(updated > 0) {
                            final Beacon beaconToPass = beacon;
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    refreshScanResults(false);
                                }
                            });
                        } else {
                            DatabaseHelper.getInstance(getBaseContext()).addNewFriend("Test Friend","07514700183",beacon);
                        }
                    }
                }
            }

        });

        try {
            logToDisplay("Starting again");
            beaconManager.startRangingBeaconsInRegion(new Region("everywhere", null, null, null));

        } catch (RemoteException e) {
            logToDisplay("Exception!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setMonitoringActivity(Activity activity) {
        this.monitoringActivity = activity;
    }
}
