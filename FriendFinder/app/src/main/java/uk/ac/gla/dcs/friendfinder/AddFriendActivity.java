package uk.ac.gla.dcs.friendfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class AddFriendActivity extends AppCompatActivity {

    protected static final String TAG = "AddFriendActivity";
    private ArrayList<String> keyArray;

    private BeaconTriplet beacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addfriend);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.add_friend));


        EditText nameInput = (EditText) findViewById(R.id.name_inputfield);
        EditText phoneInput = (EditText) findViewById(R.id.phone_inputfield);

        nameInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        phoneInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        beacon = null;
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    public void saveClick(View view) {
        EditText nameInput = (EditText) findViewById(R.id.name_inputfield);
        EditText phoneInput = (EditText) findViewById(R.id.phone_inputfield);

        if(beacon == null || nameInput.getText().equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
        } else {
            DatabaseHelper.getInstance(getApplicationContext()).addNewFriend(nameInput.getText().toString(), phoneInput.getText().toString(), beacon.beaconId1, beacon.beaconId2, beacon.beaconId3);
            finish();
        }
    }

    public void pickBeaconClick(View view) {

        final FriendFinderApplication app = ((FriendFinderApplication) this.getApplicationContext());
        keyArray = new ArrayList<String>(app.unknownBeacons.keySet());
        CharSequence[] cs = keyArray.toArray(new CharSequence[keyArray.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_beacon_button)
                .setItems(cs, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TextView selectedBeaconLabel = (TextView) findViewById(R.id.selectedBeaconLabel);
                        selectedBeaconLabel.setText(keyArray.get(which));
                        beacon = app.unknownBeacons.get(keyArray.get(which));

                        Button saveButton = (Button) findViewById(R.id.saveNewFriendButton);
                        saveButton.setEnabled(true);
                    }
                });

        builder.create().show();



        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        builder.show();*/
    }

}
