package ca.mcnallydawes.justrecord;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by H100173 on 11/12/13.
 */
public class RecordService extends Service {

    private String mRecordingPath;
    private MediaRecorder mMediaRecorder;

    @Override
    public IBinder onBind(Intent intent) {
        Bundle b = intent.getExtras();
        if(b != null) {
            mRecordingPath = b.getString(MyConstants.SERVICE_RECORDING_PATH);
        } else {
            mRecordingPath = null;
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mRecordingPath != null) {

        } else {
            Toast.makeText(this, "Unable to start recording.", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
