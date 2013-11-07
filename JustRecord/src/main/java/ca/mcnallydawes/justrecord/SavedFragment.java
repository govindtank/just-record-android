package ca.mcnallydawes.justrecord;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 11/6/13.
 */
public class SavedFragment extends Fragment {

    private List<Button> mFilterButtons;
    private ListView mListView;
    private SavedRecordingsAdapter mAdapter;
    private ArrayList<String> mData;

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

        for(Button btn : mFilterButtons) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    savedFilterButtonTap(view);
                }
            });
        }

        mFilterButtons.get(0).setSelected(true);

        mListView = (ListView) rootView.findViewById(R.id.saved_listView_list);
        mData = getListFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JustRecord"));
        mAdapter = new SavedRecordingsAdapter(getActivity(), R.layout.list_item_saved, mData);

        mListView.setAdapter(mAdapter);

        return rootView;
    }

    private void savedFilterButtonTap (View btn) {
        for(Button item : mFilterButtons) {
            item.setSelected(false);
        }
        btn.setSelected(true);
    }

    private ArrayList<String> getListFiles(File parentDir) {
        ArrayList<String> inFiles = new ArrayList<String>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                    inFiles.add(file.getName());
            }
        }
        return inFiles;
    }

    public void refreshListView() {

    }
}
