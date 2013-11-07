package ca.mcnallydawes.justrecord;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by H100173 on 11/7/13.
 */
public class SavedRecordingsAdapter extends ArrayAdapter<String> {

    private Activity mContext;
    private int mLayoutResourceId;
    private ArrayList<String> mData;

    public SavedRecordingsAdapter(Activity context, int layoutResourceId, ArrayList<String> objects) {
        super(context, layoutResourceId, objects);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if(convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.nameTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameTV.setText(mData.get(position));

        return convertView;
    }

    public static class ViewHolder {
        public TextView nameTV;
    }
}
