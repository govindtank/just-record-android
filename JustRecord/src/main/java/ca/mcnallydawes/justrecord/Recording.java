package ca.mcnallydawes.justrecord;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jeffrey on 11/8/13.
 */
public class Recording {
    private String mName;
    private String mAbsolutePath;
    private long mSize;
    private String mSizeString;
    private long mDuration;
    private String mDurationString;
    private long mDateModified;
    private String mDateModifiedString;

    public Recording(String name, String path, long size, long dateModified) {
        mName = name.replace(".mp4", "").replace(MyConstants.APP_IDENTIFIER + ".", "");
        mAbsolutePath = path;
        setFileSizes(size);
        setFileDurations(path);
        setDates(dateModified);
    }

    public String getName() {
        return mName;
    }

    public String getAbsolutePath() {
        return mAbsolutePath;
    }

    public long getSize() {
        return mSize;
    }

    public String getSizeString() {
        return mSizeString;
    }

    public long getDuration() {
        return mDuration;
    }

    public String getDurationString() {
        return mDurationString;
    }

    public long getDateModified() {
        return mDateModified;
    }

    public String getDateModifiedString() {
        return mDateModifiedString;
    }

    private void setFileSizes(long size) {
        mSize = size;
        if(mSize < 1024) {
            mSizeString = String.valueOf(size) + " B";
        } else if(mSize < 1048576) {
            mSizeString = String.valueOf(size / 1024) + " KB";
        } else {
            mSizeString = String.valueOf(size / 1048576) + " MB";
        }
    }

    public void setFileDurations(String path) {
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            player.setDataSource(path);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mDuration = player.getDuration();
        if(mDuration < 0) {
            mDurationString = "Error";
        } else {
            long seconds = mDuration / 1000;
            long minutes = seconds / 60;
            seconds = seconds - (minutes * 60);

            String minuteString = minutes > 9 ? Long.toString(minutes) : "0" + Long.toString(minutes);
            String secondsString = seconds > 9 ? Long.toString(seconds) : "0" + Long.toString(seconds);

            mDurationString = minuteString + ":" + secondsString;
        }
    }

    public void setDates(long dateModified) {
        mDateModified = dateModified;
        mDateModifiedString = new SimpleDateFormat("dd/MM/yyyy, hh:mm aa").format(new Date(dateModified));
    }
}
