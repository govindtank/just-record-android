package ca.mcnallydawes.justrecord;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by H100173 on 11/8/13.
 */
public class MyConstants {
    public static String APP_DIRECTORY_STRING = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord").toString();
    public static File  APP_DIRECTORY_FILE() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord");
    }
    public static String APP_IDENTIFIER = "ca.mcnallydawes.justrecord";
    public static String EDIT_ACTIVITY_FILE_PATH = APP_IDENTIFIER + ".pathToEditFile";
    public static String EDIT_ACTIVITY_BUNDLE = APP_IDENTIFIER + ".EditActivity.Bundle";
}
