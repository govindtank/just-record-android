package ca.mcnallydawes.justrecord;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

/**
 * Created by H100173 on 11/8/13.
 */
public class EditFragment extends Fragment {

    private File mFile;

    public static EditFragment newInstance() {
        return new EditFragment();
    }

    public static EditFragment newInstance(String filePath) {
        EditFragment fragment = new EditFragment();

        Bundle args = new Bundle();
        args.putString(MyConstants.EDIT_ACTIVITY_FILE_PATH, filePath);
        fragment.setArguments(args);

        return fragment;
    }

    public EditFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        Bundle b = getArguments();
        if(b != null) {
            mFile = new File(b.getString(MyConstants.EDIT_ACTIVITY_FILE_PATH));
        } else {

        }

        if(mFile != null) {
            TextView textView = (TextView) rootView.findViewById(R.id.edit_textView_title);
            textView.setText(mFile.getName());
        } else {

        }

        return rootView;
    }
}
