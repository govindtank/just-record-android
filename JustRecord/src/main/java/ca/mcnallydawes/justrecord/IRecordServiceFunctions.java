package ca.mcnallydawes.justrecord;

import android.app.Fragment;

/**
 * Created by jeffrey on 12/12/13.
 */
public interface IRecordServiceFunctions {
    public void registerFragment(Fragment fragment, IRecordListenerFunctions callback);
    public void unregisterFragment(Fragment fragment);
    public void pauseRecording();
    public void stopRecording(Fragment fragment);
}
