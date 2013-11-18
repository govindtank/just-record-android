package ca.mcnallydawes.justrecord;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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
    private boolean mFirstRun;

    public SavedRecordingsAdapter(Activity context, int layoutResourceId, ArrayList<Recording> objects) {
        super(context, layoutResourceId, objects);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mData = objects;
        mSelectedItemsIds = new SparseBooleanArray();
        mActiveItem = new ActiveItem();
        initActiveItem();
        mFirstRun = getFirstTimeRun();
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

        convertView.setBackgroundColor(mSelectedItemsIds.get(position) ? mContext.getResources().getColor(R.color.half_transparent_holo_red_dark) : Color.TRANSPARENT);

        if(position == mActiveItem.index) {
            UIUtils.BitmapWorkerTask bitmapWorkerTask = new UIUtils.BitmapWorkerTask(holder.thumbnailIV, mContext);
            if(mActiveItem.isPlaying) bitmapWorkerTask.execute(R.drawable.ic_action_pause);
            else bitmapWorkerTask.execute(R.drawable.ic_action_play);
        } else {
            holder.thumbnailIV.setImageResource(R.drawable.saved_ic_record);
        }

        return convertView;
    }

    public void initActiveItem() {
        mActiveItem.index = -1;
        mActiveItem.isPlaying = true;
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

    public int getActiveItemIndex() {
        return mActiveItem.index;
    }

    public void updateActiveItemIndex(int index) {
        mActiveItem.index = index;
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

    private boolean getFirstTimeRun() {
        SharedPreferences preferences = mContext.getSharedPreferences(MyConstants.APP_PREFERENCES, mContext.MODE_PRIVATE);
        return preferences.getBoolean(MyConstants.FIRST_RUN, true);
    }

    private void setFirstTimeRun(boolean firstTime) {
        SharedPreferences.Editor edit = mContext.getSharedPreferences(MyConstants.APP_PREFERENCES, mContext.MODE_PRIVATE).edit();
        edit.putBoolean(MyConstants.FIRST_RUN, firstTime);
        edit.commit();
    }
}
