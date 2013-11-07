package ca.mcnallydawes.justrecord;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private List<Button> mFilterButtons;
    private ListView mListView;
    private SavedRecordingsAdapter mAdapter;
    private ArrayList<File> mData;
    private MediaPlayer mPlayer;
    private int mClickedItem = -1;

    public static SavedFragment newInstance() {
        return new SavedFragment();
    }

    public SavedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved, container, false);

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
                            sortByLength();
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
                if(mClickedItem == i) {
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = null;
                } else {
                    if(mPlayer != null) {
                        mPlayer.release();
                        mPlayer = null;
                    }

                    mPlayer = new MediaPlayer();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    try {
                        mPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord/" + mData.get(i));
                        mPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mPlayer.start();
                    mClickedItem = i;
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int index = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete " + mData.get(i) + "?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord/" + mData.get(index)).delete();
                                refreshListView();
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
                return false;
            }
        });

        return rootView;
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

    private void sortByLength() {

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
