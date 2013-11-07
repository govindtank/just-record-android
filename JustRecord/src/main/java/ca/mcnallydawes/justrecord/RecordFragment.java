package ca.mcnallydawes.justrecord;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by jeffrey on 11/6/13.
 */
public class RecordFragment extends Fragment {

    public interface OnRecordingSavedListener {
        public void onRecordingSavedListener();
    }

    private static final String RECORD_PREFERENCES = "recordPreferences";
    private static final String NEXT_RECORDING_NUMBER = "nextRecordingNumber";

    private MediaRecorder mRecorder = null;
    private String mFileDirectory;
    private Chronometer mChronometer;
    private boolean mChronometerRunning = false;
    private long mPauseTime = 0;
    private Button mRecordButton;
    private Button mCancelButton;
    private Button mFinishButton;
    private int mNextRecordingNumber;
    private OnRecordingSavedListener mOnRecordingSavedListener;

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    public RecordFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFileDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord";
        mNextRecordingNumber = getNextRecordingNumber();

        /*
        Create the directory if it doesn't already exist.
         */
        File directory = new File(mFileDirectory);
        directory.mkdirs();

        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = (Chronometer) rootView.findViewById(R.id.record_chronometer_timer);

        mRecordButton = (Button) rootView.findViewById(R.id.record_button_record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChronometerRunning) {
                    recordPause();
                } else {
                    recordStart();
                }
            }
        });

        mCancelButton = (Button) rootView.findViewById(R.id.record_button_record_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCancelButton.getText().toString().equalsIgnoreCase(getString(R.string.record_button_cancel))) {
                    recordCancel();
                } else {
                    recordStart();
                }
            }
        });

        mFinishButton = (Button) rootView.findViewById(R.id.record_button_finish);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordFinish();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnRecordingSavedListener = (OnRecordingSavedListener) activity;
        } catch(ClassCastException e){
            throw new ClassCastException((activity.toString() +
                    "must implement OnRecordingSavedListener"));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void recordPause() {
        mPauseTime = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.stop();

        mChronometerRunning = false;
        mRecordButton.setSelected(mChronometerRunning);

        stopRecording();
    }

    private void recordStart() {
        if(isExternalStorageWritable()) {
            mChronometer.setBase(SystemClock.elapsedRealtime() + mPauseTime);
            mChronometer.start();

            mFinishButton.setVisibility(View.VISIBLE);
            mCancelButton.setText(getString(R.string.record_button_cancel));

            mChronometerRunning = true;
            mRecordButton.setSelected(mChronometerRunning);

            startRecording();
        } else {
            Toast.makeText(getActivity(), "Can't read/write to storage.", Toast.LENGTH_SHORT).show();
        }
    }

    private void recordCancel() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        mPauseTime = 0;

        mChronometerRunning = false;
        mRecordButton.setSelected(mChronometerRunning);

        mFinishButton.setVisibility(View.GONE);
        mCancelButton.setText(getString(R.string.record_button_record));

        stopRecording();

        Toast.makeText(getActivity(), "Ask if certain here.", Toast.LENGTH_SHORT).show();
    }

    private void recordFinish() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        mPauseTime = 0;

        mChronometerRunning = false;
        mRecordButton.setSelected(mChronometerRunning);

        mFinishButton.setVisibility(View.GONE);
        mCancelButton.setText(getString(R.string.record_button_record));

        stopRecording();

        Toast.makeText(getActivity(), "Prompt for save here.", Toast.LENGTH_SHORT).show();
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileDirectory + "/JustARecording_" + getRecordingString(mNextRecordingNumber) + ".mp4");
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.start();

        mNextRecordingNumber++;
        setNextRecordingNumber(mNextRecordingNumber);
    }

    private void stopRecording() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mOnRecordingSavedListener.onRecordingSavedListener();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private int getNextRecordingNumber() {
        SharedPreferences preferences = getActivity().getSharedPreferences(RECORD_PREFERENCES, getActivity().MODE_PRIVATE);
        return preferences.getInt(NEXT_RECORDING_NUMBER, 0);
    }

    private void setNextRecordingNumber(int nextRecordingNumber) {
        SharedPreferences.Editor edit = getActivity().getSharedPreferences(RECORD_PREFERENCES, getActivity().MODE_PRIVATE).edit();
        edit.putInt(NEXT_RECORDING_NUMBER, nextRecordingNumber);
        edit.commit();
    }

    private String getRecordingString(int num) {
        if(num < 10) {
            return "000" + String.valueOf(num);
        } else if(num < 100) {
            return "00" + String.valueOf(num);
        } else if(num < 1000) {
            return "0" + String.valueOf(num);
        } else {
            return String.valueOf(num);
        }
    }
}
