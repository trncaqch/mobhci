package uk.ac.gla.dcs.friendfinder;

import android.content.Context;
import android.view.View;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends ArrayAdapter<Friend> {
    private final Context context;
    public List<Friend> values;

    private MainActivity superview;

    public FriendListAdapter(Context context, List<Friend> values, MainActivity superview) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.superview = superview;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.adapter_friendlist, parent, false);

        RelativeLayout rowView = (RelativeLayout) view.findViewById(R.id.rowView);

        TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
        TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        firstLine.setText(values.get(position).getName());
        secondLine.setText("");

        if(superview != null) {
            superview.refreshScanResults(false);
        }

        return rowView;
    }
}