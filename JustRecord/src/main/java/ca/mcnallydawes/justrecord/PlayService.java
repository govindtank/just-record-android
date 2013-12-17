package ca.mcnallydawes.justrecord;

import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by H100173 on 11/12/13.
 */
public class PlayService extends Service {

    private String mFilePath;
    private MediaPlayer mMediaPlayer;
    private Map<Fragment, IPlayListenerFunctions> mClients = new ConcurrentHashMap<Fragment, IPlayListenerFunctions>();
    private final Binder mBinder = new LocalBinder();
    private Handler mHandler = new Handler();
    private Runnable mSeekBarRunnable;

    @Override
    public IBinder onBind(Intent intent) {
        Bundle b = intent.getExtras();
        if(b != null) {
            mFilePath = b.getString(MyConstants.SERVICE_RECORDING_PATH);
        } else {
            mFilePath = null;
        }
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int start = -1;
        Bundle b = intent.getExtras();
        if(b != null) {
            mFilePath = b.getString(MyConstants.SERVICE_RECORDING_PATH);
            start = b.getInt(MyConstants.SERVICE_RECORDING_START);
        } else {
            mFilePath = null;
        }

        if(mFilePath != null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    for (Fragment client : mClients.keySet()) {
                        finishPlayingForClient(client);
                    }
                    stopSelf();
                }
            });

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mMediaPlayer.setDataSource(mFilePath);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(start > 0) mMediaPlayer.seekTo(start);
            mMediaPlayer.start();
        } else {
            Toast.makeText(this, "Unable to play file.", Toast.LENGTH_SHORT).show();
        }

        mSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if(mMediaPlayer != null){
                    for(Fragment client : mClients.keySet()) {
                        updateProgressBarForclient(client);
                    }
                }
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.postDelayed(mSeekBarRunnable, 100);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void finishPlayingForClient(Fragment client) {
        IPlayListenerFunctions callback = mClients.get(client);
        callback.finishedPlaying();
    }

    public void updateProgressBarForclient(Fragment client) {
        IPlayListenerFunctions callback = mClients.get(client);
        callback.updateProgressBar(mMediaPlayer.getCurrentPosition());
    }

    public class LocalBinder extends Binder implements  IPlayServiceFunctions {

        @Override
        public void registerFragment(Fragment fragment, IPlayListenerFunctions callback) {
            mClients.put(fragment, callback);
        }

        @Override
        public void unregisterFragment(Fragment fragment) {
            mClients.remove(fragment);
        }

        @Override
        public void startPlayback(File file, int start) {
            if(mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            if(file.exists()) {
                mMediaPlayer = new MediaPlayer();

                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                        for (Fragment client : mClients.keySet()) {
                            finishPlayingForClient(client);
                        }
                        stopSelf();
                    }
                });

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    mMediaPlayer.setDataSource(file.getPath());
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mMediaPlayer.seekTo(start);
                mMediaPlayer.start();
            }
        }

        @Override
        public void togglePlayback() {
            if(mMediaPlayer != null) {
                if(mMediaPlayer.isPlaying())
                    mMediaPlayer.pause();
                else
                    mMediaPlayer.start();
            }
        }

        @Override
        public void stopPlayback() {
            if(mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        @Override
        public void seekTo(int progress) {
            if(mMediaPlayer != null) {
                mMediaPlayer.seekTo(progress);
            }
        }
    }
}
