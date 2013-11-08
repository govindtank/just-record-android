package ca.mcnallydawes.justrecord;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

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
    private SavedRecordingsAdapter mAdapter;
    private ArrayList<File> mData;
    private MediaPlayer mPlayer;
    private MediaPlayerState mPlayerState;
    private int mClickedItem = -1;

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
        mFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_length));
        mFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_size));

        for(int i = 0; i < mFilterButtons.size(); i++) {
            Button btn = mFilterButtons.get(i);
            final int index = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    savedFilterButtonTap(view);
                    switch(index) {
                        case 0:
                            sortByDateModified();
                            mAdapter.notifyDataSetChanged();
                            break;
                        case 1:
                            sortByName();
                            mAdapter.notifyDataSetChanged();
                            break;
                        case 2:
//                            sortByLength();
                            sortByFileType();
                            mAdapter.notifyDataSetChanged();
                            break;
                        case 3:
                            sortBySize();
                            mAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        mFilterButtons.get(0).setSelected(true);

        mListView = (ListView) rootView.findViewById(R.id.saved_listView_list);
        mData = getListFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord"));

        sortByDateModified();

        mAdapter = new SavedRecordingsAdapter(getActivity(), R.layout.list_item_saved, mData);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mClickedItem == i && mPlayerState == MediaPlayerState.PLAYING) {
                    pausePlayback();
                    Log.d("playback", "PAUSE FROM PLAY");
                } else if(mClickedItem == i && mPlayerState == MediaPlayerState.PAUSED) {
                    resumePlayback();
                    Log.d("playback", "PLAY FROM PAUSE");
                } else if(mClickedItem == i && mPlayerState == MediaPlayerState.STOPPED) {
                    playFile(mData.get(i));
                    Log.d("playback", "PLAY FROM STOPPED");
                } else if(mClickedItem != i) {
                    stopPlayback();
                    playFile(mData.get(i));
                    Log.d("playback", "PLAY NEW");
                }
                mClickedItem = i;
            }
        });

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.saved, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.saved_action_delete:
                        return false;
                    case R.id.saved_action_edit:
                        return false;
                    case R.id.saved_action_share:
                        return false;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });

//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                final int index = i;
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("Delete")
//                        .setMessage("Are you sure you want to delete " + mData.get(i) + "?")
//                        .setCancelable(false)
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord/" + mData.get(index)).delete();
//                                refreshListView();
//                                dialogInterface.cancel();
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.cancel();
//                            }
//                        });
//                builder.create().show();
//                return true;
//            }
//        });

        return rootView;
    }

    private void playFile(File file) {
        mPlayer = new MediaPlayer();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlayback();
            }
        });

        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mPlayer.setDataSource(file.getPath());
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.start();
        mPlayerState = MediaPlayerState.PLAYING;
    }

    private void pausePlayback() {
        mPlayer.pause();
        mPlayerState = MediaPlayerState.PAUSED;
    }

    private void resumePlayback() {
        if(mPlayer != null) {
            mPlayer.start();
            mPlayerState = MediaPlayerState.PLAYING;
        } else {
            mPlayerState = MediaPlayerState.STOPPED;
        }
    }

    private void stopPlayback() {
        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
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
        ArrayList<File> fileNames = getListFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord"));
        for(File file : fileNames) {
            mData.add(file);
        }

        sortByDateModified();
        mAdapter.notifyDataSetChanged();
    }

    private void sortByDateModified() {
        Collections.sort(mData, new Comparator<File>() {
            public int compare(File a, File b) {
                if(a.lastModified() > b.lastModified()) return 1;
                else if(a.lastModified() < b.lastModified()) return -1;
                else return 0;
            }
        });
    }

    private void sortByName() {
        Collections.sort(mData, new Comparator<File>() {
            public int compare(File a, File b) {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    private void sortByFileType() {
        Collections.sort(mData, new Comparator<File>() {
            public int compare(File a, File b) {
                return a.getName().split(".")[1].compareTo(b.getName().split(".")[1]);
            }
        });
    }

    private void sortByLength() {
        Collections.sort(mData, new Comparator<File>() {
            public int compare(File a, File b) {
                long aLength = getAudioFileDuration(a);
                long bLength = getAudioFileDuration(b);
                if(aLength > bLength) return 1;
                else if(aLength < bLength) return -1;
                else return 0;
            }
        });
    }

    private long getAudioFileDuration(File file) {
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            player.setDataSource(file.getPath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return player.getDuration();
    }

    private void sortBySize() {
        Collections.sort(mData, new Comparator<File>() {
            public int compare(File a, File b) {
                if(a.length() > b.length()) return 1;
                else if(a.length() < b.length()) return -1;
                else return 0;
            }
        });
    }
}
