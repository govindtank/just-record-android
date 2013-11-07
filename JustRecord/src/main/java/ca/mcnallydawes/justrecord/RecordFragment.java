package ca.mcnallydawes.justrecord;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

/**
 * Created by jeffrey on 11/6/13.
 */
public class RecordFragment extends Fragment {
    private Chronometer recordChronometer;
    private boolean recordChronometerRunning = false;
    private long recordPauseTime = 0;
    Button recordBtn;
    Button recordCancelBtn;
    Button recordFinishBtn;

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    public RecordFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

        recordChronometer = (Chronometer) rootView.findViewById(R.id.record_chronometer_timer);

        recordBtn = (Button) rootView.findViewById(R.id.record_button_record);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recordChronometerRunning) {
                    recordPause();
                } else {
                    recordStart();
                }
            }
        });

        recordCancelBtn = (Button) rootView.findViewById(R.id.record_button_record_cancel);
        recordCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recordCancelBtn.getText().toString().equalsIgnoreCase(getString(R.string.record_button_cancel))) {
                    recordCancel();
                } else {
                    recordStart();
                }
            }
        });

        recordFinishBtn = (Button) rootView.findViewById(R.id.record_button_finish);
        recordFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordFinish();
            }
        });

        return rootView;
    }

    private void recordPause() {
        recordPauseTime = recordChronometer.getBase() - SystemClock.elapsedRealtime();
        recordChronometer.stop();

        recordChronometerRunning = false;
        recordBtn.setSelected(recordChronometerRunning);
    }

    private void recordStart() {
        recordChronometer.setBase(SystemClock.elapsedRealtime() + recordPauseTime );
        recordChronometer.start();

        recordFinishBtn.setVisibility(View.VISIBLE);
        recordCancelBtn.setText(getString(R.string.record_button_cancel));

        recordChronometerRunning = true;
        recordBtn.setSelected(recordChronometerRunning);
    }

    private void recordCancel() {
        recordChronometer.setBase(SystemClock.elapsedRealtime());
        recordChronometer.stop();
        recordPauseTime = 0;

        recordChronometerRunning = false;
        recordBtn.setSelected(recordChronometerRunning);

        recordFinishBtn.setVisibility(View.GONE);
        recordCancelBtn.setText(getString(R.string.record_button_record));

        Toast.makeText(getActivity(), "Ask if certain here.", Toast.LENGTH_SHORT).show();
    }

    private void recordFinish() {
        recordChronometer.setBase(SystemClock.elapsedRealtime());
        recordChronometer.stop();
        recordPauseTime = 0;

        recordChronometerRunning = false;
        recordBtn.setSelected(recordChronometerRunning);

        recordFinishBtn.setVisibility(View.GONE);
        recordCancelBtn.setText(getString(R.string.record_button_record));

        Toast.makeText(getActivity(), "Prompt for save here.", Toast.LENGTH_SHORT).show();
    }
}
