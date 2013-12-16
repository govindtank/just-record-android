package ca.mcnallydawes.justrecord;

import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by H100173 on 11/12/13.
 */
public class RecordService extends Service {

    private long mStartTimeMillis;
    private String mRecordingPath;
    private MediaRecorder mMediaRecorder;
    private Map<Fragment, IRecordListenerFunctions> mClients = new ConcurrentHashMap<Fragment, IRecordListenerFunctions>();
    private final Binder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Bundle b = intent.getExtras();
        if(b != null) {
            mRecordingPath = b.getString(MyConstants.SERVICE_RECORDING_PATH);
        } else {
            mRecordingPath = null;
        }
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = intent.getExtras();
        if(b != null) {
            mRecordingPath = b.getString(MyConstants.SERVICE_RECORDING_PATH);
        } else {
            mRecordingPath = null;
        }

        if(mRecordingPath != null) {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(mRecordingPath);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaRecorder.start();
            Toast.makeText(this, "Recording.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to start recording.", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateRecordingTimeOnClient(final Fragment client) {
        IRecordListenerFunctions callback = mClients.get(client);
        callback.setRecordTime();
    }

    public class LocalBinder extends Binder implements IRecordServiceFunctions {

        // Registers a Fragment to receive updates
        public void registerFragment(Fragment fragment, IRecordListenerFunctions callback) {
            mClients.put(fragment, callback);
        }

        public void unregisterFragment(Fragment fragment) {
            mClients.remove(fragment);
        }

        @Override
        public void pauseRecording() {

        }

        @Override
        public void stopRecording(Fragment fragment) {
            if(mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            Toast.makeText(fragment.getActivity(), "Stop recording.", Toast.LENGTH_SHORT).show();
        }
    }
}
