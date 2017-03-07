package uk.ac.gla.dcs.friendfinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


public class SettingsActivity extends AppCompatActivity {

    protected static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.settings));

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPreferences", getApplicationContext().MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();

        CheckBox enableAllNotificationsCheckbox = (CheckBox) this.findViewById(R.id.enableAllNotificationsCheckbox);
        enableAllNotificationsCheckbox.setChecked(preferences.getBoolean("enableAllNotifications", true));

        enableAllNotificationsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
           editor.putBoolean("enableAllNotifications", isChecked);
           editor.apply();
           }
       });

        CheckBox enableVibrationCheckbox = (CheckBox) this.findViewById(R.id.enableVibrationCheckbox);
        enableVibrationCheckbox.setChecked(preferences.getBoolean("enableVibration", true));

        enableVibrationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                editor.putBoolean("enableVibration", isChecked);
                editor.apply();
            }
        });
        CheckBox notificationNoTimeout = (CheckBox) this.findViewById(R.id.notificationNoTimeout);
        notificationNoTimeout.setChecked(preferences.getBoolean("notificationNoTimeout", false));

        notificationNoTimeout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                editor.putBoolean("notificationNoTimeout", isChecked);
                editor.apply();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
