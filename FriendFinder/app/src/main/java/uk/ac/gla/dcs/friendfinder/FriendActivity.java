package uk.ac.gla.dcs.friendfinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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


public class FriendActivity extends AppCompatActivity {

    protected static final String TAG = "FriendActivity";
    private Friend friend;

    private String phoneInputText = "";

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

        TextView topLabel = (TextView) this.findViewById(R.id.topLabel);
        ImageView strengthIcon = (ImageView) this.findViewById(R.id.signalImage);
        CheckBox enableNotificationCheckbox = (CheckBox) this.findViewById(R.id.enableNotificationCheckbox);
        enableNotificationCheckbox.setChecked(friend.notificationsEnabled());

        enableNotificationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
               DatabaseHelper.getInstance(getBaseContext()).setNotificationsEnabledForFriend(friend.getId(), isChecked);
           }
       });

        int drawable;

        String labelPart2 = getString(R.string.is_nearby);

        if(friend.getLastUpdate().after(new Date(System.currentTimeMillis() - MainActivity.SHORT_TIMEOUT_INTERVAL))) {
            if (friend.getLastStrength() < MainActivity.CLOSE_SIGNAL_STRENGTH) {
                drawable = R.drawable.signal3;
            } else if (friend.getLastStrength() < MainActivity.FAR_SIGNAL_STRENGTH) {
                drawable = R.drawable.signal2;
            } else {
                drawable = R.drawable.signal1;
            }
        } else if(friend.getLastUpdate().after(new Date(System.currentTimeMillis() - MainActivity.LONG_TIMEOUT_INTERVAL))) {
            drawable = R.drawable.signal0;
            labelPart2 = getString(R.string.just_seen);
        } else {
            drawable = R.drawable.signalempty;
            labelPart2 = getString(R.string.is_not_nearby);
        }

        topLabel.setText(friend.getName() + "\n" + labelPart2);

        strengthIcon.setImageResource(drawable);

        if(friend == null) {
            finish();
        }

        getSupportActionBar().setTitle(friend.getName());

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
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void shareContact(View view) {
        Toast.makeText(getApplicationContext(), getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
    }

    public void callFriend(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(friend.getPhone()));
        try {
            startActivity(callIntent);
        }
        catch(Exception e) {
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
