package uk.ac.gla.dcs.friendfinder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;


public class FriendActivity extends AppCompatActivity {

    protected static final String TAG = "FriendActivity";
    private Friend friend;
    private Handler mHandler;

    final int REFRESH_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if(getIntent().getStringExtra("friendId") == null) {
            finish();
        }

        Log.d(TAG, "onCreate: intent extra:" + getIntent().getStringExtra("friendId"));

        Friend contextFriend = DatabaseHelper.getInstance(getBaseContext()).getFriendById(Long.parseLong(getIntent().getStringExtra("friendId")));
        this.friend = contextFriend;

        if(friend == null) {
            finish();
        }

        CheckBox enableNotificationCheckbox = (CheckBox) this.findViewById(R.id.enableNotificationCheckbox);
        enableNotificationCheckbox.setChecked(friend.notificationsEnabled());

        enableNotificationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
               DatabaseHelper.getInstance(getBaseContext()).setNotificationsEnabledForFriend(friend.getId(), isChecked);
           }
       });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                refreshScanResults();
            } finally {
                mHandler.postDelayed(mStatusChecker, REFRESH_INTERVAL);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mHandler = new Handler();
        mStatusChecker.run();
        refreshScanResults();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public synchronized void refreshScanResults() {
        Friend upToDateFriend = DatabaseHelper.getInstance(getBaseContext()).getFriendById(friend.getId());
        ImageView strengthIcon = (ImageView) this.findViewById(R.id.signalImage);

        TextView firstLine = (TextView) this.findViewById(R.id.topLabel);
        TextView secondLine = (TextView) this.findViewById(R.id.bottomLabel);

        int drawable;

        if(upToDateFriend.getLastUpdate().after(new Date(System.currentTimeMillis() - MainActivity.SHORT_TIMEOUT_INTERVAL))) {
            if (upToDateFriend.getLastStrength() < MainActivity.CLOSE_SIGNAL_STRENGTH) {
                drawable = R.drawable.signal3;
            } else if (upToDateFriend.getLastStrength() < MainActivity.FAR_SIGNAL_STRENGTH) {
                drawable = R.drawable.signal2;
            } else {
                drawable = R.drawable.signal1;
            }
            secondLine.setText(getString(R.string.is_nearby));
        } else if(upToDateFriend.getLastUpdate().after(new Date(System.currentTimeMillis() - MainActivity.LONG_TIMEOUT_INTERVAL))) {
            drawable = R.drawable.signal0;
            secondLine.setText(getString(R.string.just_seen));
        } else {
            drawable = R.drawable.signalempty;
            secondLine.setText(getString(R.string.last_seen) + " " + DateUtils.getRelativeTimeSpanString(upToDateFriend.getLastUpdate().getTime()));
        }

        strengthIcon.setImageResource(drawable);
        firstLine.setText(upToDateFriend.getName());
        getSupportActionBar().setTitle(upToDateFriend.getName());
    }

    public void shareContact(View view) {
        Toast.makeText(getApplicationContext(), getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
    }

    public void callFriend(View view) {
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE);
            if(permissionCheck == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        1);
            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+friend.getPhone()));
                startActivity(callIntent);
            }
        }
        catch(SecurityException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_make_call), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Unable to make call");
        }
    }

    public void editPhoneNumber(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_phone_number);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameEdit = new EditText(this);
        final EditText phoneEdit = new EditText(this);

        nameEdit.setText(friend.getName());
        phoneEdit.setText(friend.getPhone());

        nameEdit.setHint(R.string.name_hint);
        phoneEdit.setHint(R.string.phone_hint);

        layout.addView(nameEdit);
        layout.addView(phoneEdit);

        builder.setView(layout);

        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseHelper.getInstance(getBaseContext()).setNameAndPhoneForFriend(friend.getId(),nameEdit.getText().toString(),phoneEdit.getText().toString());
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    public void deleteContact(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sure_delete);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DatabaseHelper.getInstance(getBaseContext()).deleteFriend(friend);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
