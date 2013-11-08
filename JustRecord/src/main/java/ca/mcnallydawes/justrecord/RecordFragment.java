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
import android.widget.Space;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jeffrey on 11/6/13.
 */
public class RecordFragment extends Fragment {

    public interface OnRecordingSavedListener {
        public void onRecordingSavedListener();
    }

    private static final String RECORD_PREFERENCES = "recordPreferences";
    private static final String NEXT_RECORDING_NUMBER = "nextRecordingNumber";
    private static final String DEFAULT_RECORDING_NAME = "JustARecording_";
    private static final String PARTIAL_RECORDING_NAME = "ca.mcnallydawes.justrecord.JustAPartialRecording_";

    private MediaRecorder mRecorder = null;
    private Chronometer mChronometer;
    private boolean mChronometerRunning = false;
    private long mPauseTime = 0;
    private Button mRecordButton;
    private Button mRecordButtonAlt;
    private Button mCancelButton;
    private Button mFinishButton;
    private Space mSpace0;
    private int mNextRecordingNumber;
    private int mPartialRecordingNumber;
    private OnRecordingSavedListener mOnRecordingSavedListener;

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    public RecordFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNextRecordingNumber = getNextRecordingNumber();
        mPartialRecordingNumber = 0;

        /*
        Create the directory if it doesn't already exist.
         */
        File directory = new File(MyConstants.APP_DIRECTORY_STRING);
        directory.mkdirs();

        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = (Chronometer) rootView.findViewById(R.id.record_chronometer_timer);

        mRecordButton = (Button) rootView.findViewById(R.id.record_button_record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChronometerRunning) {
                    pauseRecording();
                } else {
                    startRecording();
                }
            }
        });

        mRecordButtonAlt = (Button) rootView.findViewById(R.id.record_button_record_alt);
        mRecordButtonAlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

        mCancelButton = (Button) rootView.findViewById(R.id.record_button_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRecording();
            }
        });

        mFinishButton = (Button) rootView.findViewById(R.id.record_button_finish);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishRecording();
            }
        });

        mSpace0 = (Space) rootView.findViewById(R.id.record_space_0);

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

    private void startRecording() {
        if(isExternalStorageWritable()) {
            setRecordButtonVisible(false);
            mChronometer.setBase(SystemClock.elapsedRealtime() + mPauseTime);
            mChronometer.start();

            mChronometerRunning = true;
            mRecordButton.setSelected(mChronometerRunning);

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(MyConstants.APP_DIRECTORY_STRING + "/" + PARTIAL_RECORDING_NAME + getRecordingString(mPartialRecordingNumber) + ".mp4");
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRecorder.start();

            mNextRecordingNumber++;
            setNextRecordingNumber(mNextRecordingNumber);
        } else {
            Toast.makeText(getActivity(), "Can't read/write to storage.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mOnRecordingSavedListener.onRecordingSavedListener();
        }
    }

    private void cancelRecording() {
        setRecordButtonVisible(true);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        mPauseTime = 0;

        mChronometerRunning = false;
        mRecordButton.setSelected(mChronometerRunning);

        stopRecording();

        Toast.makeText(getActivity(), "Ask if certain here.", Toast.LENGTH_SHORT).show();
    }

    private void finishRecording() {
        setRecordButtonVisible(true);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        mPauseTime = 0;

        mChronometerRunning = false;
        mRecordButton.setSelected(mChronometerRunning);

        stopRecording();

        ArrayList<File> allRecordings = getListFiles(MyConstants.APP_DIRECTORY_FILE());
        ArrayList<File> partialRecordings = new ArrayList<File>();
        for(File file : allRecordings) {
            if(file.getName().contains("ca.mcnallydawes.justrecord")) partialRecordings.add(file);
        }
        /*
        Combine all partial recordings here, reset the partial number, change the mNextRecordingNumber.
         */

        Toast.makeText(getActivity(), "Prompt for save here.", Toast.LENGTH_SHORT).show();
    }

    private void pauseRecording() {
        mPauseTime = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.stop();

        mChronometerRunning = false;
        mRecordButton.setSelected(mChronometerRunning);

        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mOnRecordingSavedListener.onRecordingSavedListener();
        }

        mPartialRecordingNumber++;
    }

    private void setRecordButtonVisible(boolean visible) {
        mRecordButtonAlt.setVisibility(visible ? View.VISIBLE : View.GONE);

        mFinishButton.setVisibility(visible ? View.GONE : View.VISIBLE);
        mCancelButton.setVisibility(visible ? View.GONE : View.VISIBLE);
        mSpace0.setVisibility(visible ? View.GONE : View.VISIBLE);
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

    private ArrayList<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                inFiles.add(file);
            }
        }
        return inFiles;
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
