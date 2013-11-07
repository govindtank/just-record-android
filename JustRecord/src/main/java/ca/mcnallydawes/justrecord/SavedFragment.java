package ca.mcnallydawes.justrecord;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 11/6/13.
 */
public class SavedFragment extends Fragment {

    private List<Button> savedFilterButtons;

    public static SavedFragment newInstance() {
        return new SavedFragment();
    }

    public SavedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved, container, false);

        savedFilterButtons = new ArrayList<Button>();

        savedFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_date));
        savedFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_name));
        savedFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_length));
        savedFilterButtons.add((Button) rootView.findViewById(R.id.saved_button_filter_size));

        for(Button btn : savedFilterButtons) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    savedFilterButtonTap(view);
                }
            });
        }

        savedFilterButtons.get(0).setSelected(true);

        TextView titleTV = (TextView) rootView.findViewById(R.id.saved_textView_title);
        titleTV.setText("Saved");

        return rootView;
    }

    private void savedFilterButtonTap (View btn) {
        for(Button item : savedFilterButtons) {
            item.setSelected(false);
        }
        btn.setSelected(true);
    }
}
