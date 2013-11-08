package ca.mcnallydawes.justrecord;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by H100173 on 11/8/13.
 */
public class UIUtils {
    public static class FileDurationWorkerTask extends AsyncTask<File, Void, String> {

        private final WeakReference<TextView> textViewWeakReference;
        private final Context mContext;
        private File mFile;

        public FileDurationWorkerTask(TextView textView, Context context) {
            textViewWeakReference = new WeakReference<TextView>(textView);
            mContext = context;
        }

        @Override
        protected String doInBackground(File... params) {
            mFile = params[0];

            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                player.setDataSource(mFile.getPath());
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

        @Override
        protected void onPostExecute(String s) {
            if(textViewWeakReference != null && s != null) {
                final TextView textView = textViewWeakReference.get();
                if(textView != null) {
                    textView.setText(s);
                }
            }
        }
    }
}
