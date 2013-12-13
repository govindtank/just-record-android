package ca.mcnallydawes.justrecord;

import android.app.Fragment;

import java.io.File;

/**
 * Created by jeffrey on 12/12/13.
 */
public interface IPlayServiceFunctions {
    public void registerFragment(Fragment fragment, IPlayListenerFunctions callback);
    public void unregisterFragment(Fragment fragment);
    public void startPlayback(File file);
    public void togglePlayback();
    public void stopPlayback();
}
