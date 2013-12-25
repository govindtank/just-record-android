package ca.mcnallydawes.justrecord;

/**
 * Created by jeffrey on 12/12/13.
 */
public interface IPlayListenerFunctions {
    public void setPlaybackTime(long seconds);
    public void finishedPlaying();
    public void updateProgressBar(int position);
}
