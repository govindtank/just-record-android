package ca.mcnallydawes.justrecord;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by H100173 on 11/7/13.
 */
public class SavedRecordingsAdapter extends ArrayAdapter<Recording> {

    public static class ViewHolder {
        public TextView nameTV;
        public TextView dateTV;
        public TextView durationTV;
        public TextView sizeTV;
        public ImageView thumbnailIV;
    }

    public class ActiveItem {
        public int index;
        public boolean isPlaying;

        public ActiveItem() {
            index = -1;
            isPlaying = false;
        }
    }

    private Activity mContext;
    private int mLayoutResourceId;
    private ArrayList<Recording> mData;
    private SparseBooleanArray mSelectedItemsIds;
    private ActiveItem mActiveItem;

    public SavedRecordingsAdapter(Activity context, int layoutResourceId, ArrayList<Recording> objects) {
        super(context, layoutResourceId, objects);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mData = objects;
        mSelectedItemsIds = new SparseBooleanArray();
        mActiveItem = new ActiveItem();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if(convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.thumbnailIV = (ImageView) convertView.findViewById(R.id.saved_list_item_imageView_thumbnail);
            holder.nameTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_name);
            holder.dateTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_date);
            holder.durationTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_duration);
            holder.sizeTV = (TextView) convertView.findViewById(R.id.saved_list_item_textView_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Recording recording = mData.get(position);

        holder.nameTV.setText(recording.getName());
        holder.dateTV.setText(recording.getDateModifiedString());
        holder.durationTV.setText(recording.getDurationString());
        holder.sizeTV.setText(recording.getSizeString());

        convertView.setBackgroundColor(mSelectedItemsIds.get(position) ? mContext.getResources().getColor(R.color.holo_red_dark) : Color.TRANSPARENT);

        if(position == mActiveItem.index) {
            UIUtils.BitmapWorkerTask bitmapWorkerTask = new UIUtils.BitmapWorkerTask(holder.thumbnailIV, mContext);
            if(mActiveItem.isPlaying) bitmapWorkerTask.execute(R.drawable.ic_action_pause);
            else bitmapWorkerTask.execute(R.drawable.ic_action_play);
        } else {
            holder.thumbnailIV.setImageResource(R.drawable.saved_ic_record);
        }

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

    private String getFileDuration(File file) {
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            player.setDataSource(file.getPath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int duration = player.getDuration();
        if(duration < 0) {
            return "Error";
        }

        int seconds = duration / 1000;
        int minutes = seconds / 60;
        seconds = seconds - (minutes * 60);

        String minuteString = minutes > 9 ? Integer.toString(minutes) : "0" + Integer.toString(minutes);
        String secondsString = seconds > 9 ? Integer.toString(seconds) : "0" + Integer.toString(seconds);

        return minuteString + ":" + secondsString;
    }

    public void showPlayingItem(int index) {
        mActiveItem.index = index;
        mActiveItem.isPlaying = true;
        notifyDataSetChanged();
    }

    public void showPausedItem(int index) {
        mActiveItem.index = index;
        mActiveItem.isPlaying = false;
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if(value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}
