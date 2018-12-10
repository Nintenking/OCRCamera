package unipd.se18.ocrcamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Adapter for the view of the processing result of the pics
 * @author Pietro Prandini
 */
public class AdapterTestElement extends BaseAdapter
{
    /**
     * Context of the app
     */
    private Context context;

    /**
     * Elements of test
     */
    private TestElement[] entries;

    /**
     * String used for the logs of this class
     */
    private final String TAG = "AdapterTestElement";

    /**
     * Defines an object of AdapterTestElement type
     * @param context The reference to the activity where the adapter will be used
     * @param entries The list of the test elements containing data from photos test
     */
    AdapterTestElement(Context context, TestElement[] entries)
    {
        this.context = context;
        this.entries = entries;
    }

    @Override
    public int getCount() { return entries.length; }

    @Override
    public Object getItem(int position) { return entries[position]; }

    @Override
    public long getItemId(int position) {
        // The prefix is "foto", so the suffix starts at 4
        int suffix = 4;
        return Integer.parseInt(entries[position].getFileName().substring(suffix));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.test_element, parent, false);
        }

        Button viewDetailsButton = convertView.findViewById(R.id.view_details_button);
        viewDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TestElementDetails.class);
                TestElementDetails.entry = entries[position];
                context.startActivity(i);
            }
        });

        // Set the correctness value
        TextView correctness = convertView.findViewById(R.id.correctness_view);
        float confidence = entries[position].getConfidence();
        correctness.setText(TestElementDetails.formatPercentString(confidence));

        // Set the color of the correctness text value
        TestElementDetails.redUntil = 70;
        TestElementDetails.yellowUntil = 85;
        correctness.setTextColor(TestElementDetails.chooseColorOfValue(confidence,
                TestElementDetails.redUntil,TestElementDetails.yellowUntil));

        // Set the name of the pic
        TextView name = convertView.findViewById(R.id.pic_name_view);
        String picName = entries[position].getFileName();
        name.setText(picName);

        // Set the Tags text
        TextView tags = convertView.findViewById(R.id.tags_view);
        StringBuilder assignedTags = new StringBuilder();
        for (String tag : entries[position].getTags()) {
            assignedTags.append(tag).append(", ");
        }
        tags.setText(assignedTags.toString());

        // Set alterations view
        TestElementDetails.setAlterationsView(
                context,
                (RelativeLayout) convertView.findViewById(R.id.result_view),
                R.id.tags_view,
                entries[position]
        );
        return convertView;
    }
}