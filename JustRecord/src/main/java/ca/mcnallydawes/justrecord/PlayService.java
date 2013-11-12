package ca.mcnallydawes.justrecord;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by H100173 on 11/12/13.
 */
public class PlayService extends Service {

    private String mFilePath;
    private MediaPlayer mMediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        Bundle b = intent.getExtras();
        if(b != null) {
            mFilePath = b.getString(MyConstants.SERVICE_RECORDING_PATH);
        } else {
            mFilePath = null;
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mFilePath != null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                }
            });

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mMediaPlayer.setDataSource(mFilePath);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaPlayer.start();
        } else {
            Toast.makeText(this, "Unable to play file.", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
