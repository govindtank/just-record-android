package ca.mcnallydawes.justrecord;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by H100173 on 11/7/13.
 */
public class SavedRecordingsAdapter extends ArrayAdapter<File> {

    private Activity mContext;
    private int mLayoutResourceId;
    private ArrayList<File> mData;

    public SavedRecordingsAdapter(Activity context, int layoutResourceId, ArrayList<File> objects) {
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
            holder.dateTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_date);
//            holder.durationTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_duration);
            holder.sizeTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameTV.setText(mData.get(position).getName());
        holder.dateTV.setText(new SimpleDateFormat("dd/MM/yyyy, hh:mm aa").format(new Date(mData.get(position).lastModified())));
//        holder.durationTV.setText("");
        holder.sizeTV.setText(getFileSize(mData.get(position).length()));

        return convertView;
    }

    private String getFileSize(long length) {
        if(length < 1024) {
            return String.valueOf(length) + " B";
        } else if(length < 1048576) {
            return String.valueOf(length / 1024) + " KB";
        } else {
            return String.valueOf(length / 1048576) + " MB";
        }
    }

    public static class ViewHolder {
        public TextView nameTV;
        public TextView dateTV;
        public TextView durationTV;
        public TextView sizeTV;
    }
}
