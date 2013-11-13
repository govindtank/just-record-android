package ca.mcnallydawes.justrecord;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jeffrey on 11/6/13.
 */
public class SavedFragment extends Fragment {

    public enum MediaPlayerState {
        STOPPED, PLAYING, PAUSED
    }

    private List<Button> mFilterButtons;
    private ListView mListView;
    private TextView mInfoTV;
    private SavedRecordingsAdapter mAdapter;
    private ArrayList<Recording> mData;
    private MediaPlayer mMediaPlayer;
    private MediaPlayerState mPlayerState;
    private int mClickedItem = -1;
    private ActionMode mActionMode;
    private SeekBar mSeekBar;
    private Handler mHandler = new Handler();
    private Runnable mSeekBarRunnable;
    private TextView mPlaybackTimeTV;
    private TextView mPlaybackDurationTV;

    public static SavedFragment newInstance() {
        return new SavedFragment();
    }

    public SavedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved, container, false);

        mPlayerState = MediaPlayerState.STOPPED;

        mFilterButtons = new ArrayList<Button>();

        mFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_date));
        mFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_name));
        mFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_type));
        mFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_size));

        mSeekBar = (SeekBar) rootView.findViewById(R.id.saved_seekBar);
        mPlaybackTimeTV = (TextView) rootView.findViewById(R.id.saved_textView_playback_time);
        mPlaybackDurationTV = (TextView) rootView.findViewById(R.id.saved_textView_playback_duration);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mPlaybackTimeTV.setText(getTimeString(progress));
                } else if(mMediaPlayer == null && fromUser) {
                    mPlaybackTimeTV.setText(getTimeString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if(mMediaPlayer != null){
                    int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                    mSeekBar.setProgress(mCurrentPosition);
                    mPlaybackTimeTV.setText(getTimeString(mCurrentPosition));
                }
                mHandler.postDelayed(this, 100);
            }
        };

        mHandler.postDelayed(mSeekBarRunnable, 100);

        for(int i = 0; i < mFilterButtons.size(); i++) {
            Button btn = mFilterButtons.get(i);
            final int index = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mActionMode == null) {
                        savedFilterButtonTap(view);
                        switch(index) {
                            case 0:
                                sortByDateModified();
                                break;
                            case 1:
                                sortByName();
                                break;
                            case 2:
    //                            sortByLength();
                                sortByFileType();
                                break;
                            case 3:
                                sortBySize();
                                break;
                            default:
                                break;
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "Can't sort while selecting", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        mFilterButtons.get(0).setSelected(true);

        mListView = (ListView) rootView.findViewById(R.id.saved_listView_list);

        mInfoTV = (TextView) rootView.findViewById(R.id.saved_textView_info);

        mData = new ArrayList<Recording>();
        ArrayList<File> files = getListFiles(MyConstants.APP_DIRECTORY_FILE());
        for(File file : files) {
            mData.add(new Recording(file.getName(), file.getAbsolutePath(), file.length(), file.lastModified()));
        }

        mAdapter = new SavedRecordingsAdapter(getActivity(), R.layout.list_item_saved, mData);

        sortByDateModified();

        mInfoTV.setVisibility(mData.size() > 0 ? View.GONE : View.VISIBLE);
        mListView.setVisibility(mData.size() > 0 ? View.VISIBLE : View.GONE);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mActionMode == null) {
                    if(mClickedItem == i && mPlayerState == MediaPlayerState.PLAYING) {
                        pausePlayback();
                        mAdapter.showPausedItem(i);
//                        Log.d("playback", "PAUSE FROM PLAY");
                    } else if(mClickedItem == i && mPlayerState == MediaPlayerState.PAUSED) {
                        resumePlayback();
                        mAdapter.showPlayingItem(i);
//                        Log.d("playback", "PLAY FROM PAUSE");
                    } else if(mClickedItem == i && mPlayerState == MediaPlayerState.STOPPED) {
                        playFile(new File(mData.get(i).getAbsolutePath()), mSeekBar.getMax() - mSeekBar.getProgress() <= 100 ? 0 : mSeekBar.getProgress());
                        mAdapter.showPlayingItem(i);
//                        Log.d("playback", "PLAY FROM STOPPED");
                    } else if(mClickedItem != i) {
                        stopPlayback();
                        mSeekBar.setMax((int)mData.get(i).getDuration());
                        playFile(new File(mData.get(i).getAbsolutePath()), 0);
                        mAdapter.showPlayingItem(i);
                        mPlaybackDurationTV.setText(mData.get(i).getDurationString());
//                        Log.d("playback", "PLAY NEW");
                    }
                    mClickedItem = i;
                } else {
                    onListItemSelect(i);
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                onListItemSelect(i);
                return true;
            }
        });

        return rootView;
    }

    private void onListItemSelect(int position) {
        mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        if(hasCheckedItems && mActionMode == null) {
            mActionMode = getActivity().startActionMode(new ActionModeCallback());
        } else if(!hasCheckedItems && mActionMode != null) {
            mActionMode.finish();
        }

        if(mActionMode != null) {
            mActionMode.invalidate();
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount() + " selected"));
        }
    }

    public void finishActionMode() {
        if(mActionMode != null) {
            mActionMode.finish();
        }
    }

    private void playFile(File file, int start) {
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlayback();
                mAdapter.showPausedItem(mClickedItem);
                mAdapter.notifyDataSetChanged();
            }
        });

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.setDataSource(file.getPath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.seekTo(start);
        mMediaPlayer.start();
        mPlayerState = MediaPlayerState.PLAYING;
    }

    private void pausePlayback() {
        mMediaPlayer.pause();
        mPlayerState = MediaPlayerState.PAUSED;
    }

    private void resumePlayback() {
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
            mPlayerState = MediaPlayerState.PLAYING;
        } else {
            mPlayerState = MediaPlayerState.STOPPED;
        }
    }

    private void stopPlayback() {
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mPlayerState = MediaPlayerState.STOPPED;
    }

    private void savedFilterButtonTap (View btn) {
        for(Button item : mFilterButtons) {
            item.setSelected(false);
        }
        btn.setSelected(true);
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

    public void refreshListView() {
        mData.clear();
        ArrayList<File> fileNames = getListFiles(MyConstants.APP_DIRECTORY_FILE());
        for(File file : fileNames) {
//            if(!file.getName().contains("ca.mcnallydawes.justrecord"))
                mData.add(new Recording(file.getName(), file.getAbsolutePath(), file.length(), file.lastModified()));
        }

        mInfoTV.setVisibility(mData.size() > 0 ? View.GONE : View.VISIBLE);
        mListView.setVisibility(mData.size() > 0 ? View.VISIBLE : View.GONE);

        sortByDateModified();
        mAdapter.initActiveItem();
        mAdapter.notifyDataSetChanged();
    }

    private void sortByDateModified() {
        Recording activeRecording = getActiveRecording();

        Collections.sort(mData, new Comparator<Recording>() {
            public int compare(Recording a, Recording b) {
                if (a.getDateModified() > b.getDateModified()) return -1;
                else if (a.getDateModified() < b.getDateModified()) return 1;
                else return 0;
            }
        });

        if(activeRecording != null) updateActiveItem(activeRecording);
//        mAdapter.notifyDataSetChanged();
    }

    private void sortByName() {
        Recording activeRecording = getActiveRecording();

        Collections.sort(mData, new Comparator<Recording>() {
            public int compare(Recording a, Recording b) {
                return a.getName().compareTo(b.getName());
            }
        });

        if(activeRecording != null) updateActiveItem(activeRecording);
//        mAdapter.notifyDataSetChanged();
    }

    private void sortByFileType() {
        Recording activeRecording = getActiveRecording();

        Collections.sort(mData, new Comparator<Recording>() {
            public int compare(Recording a, Recording b) {
                return a.getName().compareTo(b.getName());
            }
        });

        if(activeRecording != null) updateActiveItem(activeRecording);
//        mAdapter.notifyDataSetChanged();
    }

    private void sortByLength() {
        Recording activeRecording = getActiveRecording();

        Collections.sort(mData, new Comparator<Recording>() {
            public int compare(Recording a, Recording b) {
                if (a.getDuration() > b.getDuration()) return 1;
                else if (a.getDuration() < b.getDuration()) return -1;
                else return 0;
            }
        });

        if(activeRecording != null) updateActiveItem(activeRecording);
//        mAdapter.notifyDataSetChanged();
    }

    private void sortBySize() {
        Recording activeRecording = getActiveRecording();

        Collections.sort(mData, new Comparator<Recording>() {
            public int compare(Recording a, Recording b) {
                if (a.getSize() > b.getSize()) return -1;
                else if (a.getSize() < b.getSize()) return 1;
                else return 0;
            }
        });

        if(activeRecording != null) updateActiveItem(activeRecording);
//        mAdapter.notifyDataSetChanged();
    }

    private Recording getActiveRecording() {
        int activeIndex = mAdapter.getActiveItemIndex();
        if(activeIndex >= 0) return mData.get(activeIndex);
        else return null;
    }

    private void updateActiveItem(Recording activeRecording) {
        for(int i = 0; i < mData.size(); i++) {
            Recording recording = mData.get(i);
            if(recording == activeRecording) {
                mAdapter.updateActiveItemIndex(i);
                mClickedItem = i;
                break;
            }
        }
    }

    private String getTimeString(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds - (minutes * 60);

        String minuteString = minutes > 9 ? Long.toString(minutes) : "0" + Long.toString(minutes);
        String secondsString = seconds > 9 ? Long.toString(seconds) : "0" + Long.toString(seconds);

        return minuteString + ":" + secondsString;
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.saved, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            if(mAdapter.getSelectedCount() > 1) {
                menu.findItem(R.id.saved_action_share).setVisible(false);
                menu.findItem(R.id.saved_action_edit).setVisible(false);
                return true;
            } else {
                menu.findItem(R.id.saved_action_share).setVisible(true);
                menu.findItem(R.id.saved_action_edit).setVisible(true);
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            SparseBooleanArray selected = mAdapter.getSelectedIds();
            ArrayList<Integer> selectedPositions = new ArrayList<Integer>();
            int selectedPosition = 0;

            for(int i = (selected.size() - 1); i >= 0; i--) {
                if(selected.valueAt(i)) {
                    selectedPosition = selected.keyAt(i);
                    selectedPositions.add(selected.keyAt(i));
                }
            }

            switch (menuItem.getItemId()) {
                case R.id.saved_action_edit:
                    Intent editIntent = new Intent(getActivity(), EditActivity.class);
                    editIntent.putExtra(MyConstants.EDIT_ACTIVITY_FILE_PATH, mData.get(selectedPosition).getAbsolutePath());
                    startActivity(editIntent);

                    mAdapter.removeSelection();
                    if(mActionMode != null) mActionMode.finish();
                    return true;
                case R.id.saved_action_share:
                    Uri uri = Uri.parse(mData.get(selectedPosition).getAbsolutePath());
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("audio/*");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(share, "Share " + mData.get(selectedPosition).getName()));

                    mAdapter.removeSelection();
                    if(mActionMode != null) mActionMode.finish();
                    return true;
                case R.id.saved_action_delete:
                    final ArrayList<Integer> finalSelectedPositions = selectedPositions;

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete")
                            .setMessage("Are you sure you want to delete " +
                                    (selectedPositions.size() > 1 ?
                                            "these " + selectedPositions.size() + " items?" :
                                            mData.get(selectedPositions.get(0)).getName() + "?"))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    for (int index : finalSelectedPositions) {
                                        new File(mData.get(index).getAbsolutePath()).delete();
                                    }
                                    refreshListView();
                                    mAdapter.removeSelection();
                                    if(mActionMode != null) mActionMode.finish();
                                    dialogInterface.cancel();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    builder.create().show();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mAdapter.removeSelection();
            mActionMode = null;
        }
    }
}
