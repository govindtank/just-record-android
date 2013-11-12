package ca.mcnallydawes.justrecord;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by H100173 on 11/8/13.
 */
public class UIUtils {
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;
        private final Context mContext;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView, Context context) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
            mContext = context;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            int width, height;
            data = params[0];
            if(params.length == 3) {
                width = params[1];
                height = params[2];
            } else {
                width = 50;
                height = 50;
            }
            return decodeSampledBitmapFromResource(mContext.getResources(), data, width, height);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewWeakReference != null && bitmap != null) {
                final ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

//    public static class FileDurationWorkerTask extends AsyncTask<File, Void, String> {
//
//        private final WeakReference<TextView> textViewWeakReference;
//        private final Context mContext;
//        private File mFile;
//
//        public FileDurationWorkerTask(TextView textView, Context context) {
//            textViewWeakReference = new WeakReference<TextView>(textView);
//            mContext = context;
//        }
//
//        @Override
//        protected String doInBackground(File... params) {
//            mFile = params[0];
//
//            MediaPlayer player = new MediaPlayer();
//            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//            try {
//                player.setDataSource(mFile.getPath());
//                player.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            int duration = player.getDuration();
//            if(duration < 0) {
//                return "Error";
//            }
//
//            int seconds = duration / 1000;
//            int minutes = seconds / 60;
//            seconds = seconds - (minutes * 60);
//
//            String minuteString = minutes > 9 ? Integer.toString(minutes) : "0" + Integer.toString(minutes);
//            String secondsString = seconds > 9 ? Integer.toString(seconds) : "0" + Integer.toString(seconds);
//
//            return minuteString + ":" + secondsString;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            if(textViewWeakReference != null && s != null) {
//                final TextView textView = textViewWeakReference.get();
//                if(textView != null) {
//                    textView.setText(s);
//                }
//            }
//        }
//    }
}
