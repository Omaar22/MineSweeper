package omar.Minesweeper;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DifficultyDialog extends DialogFragment {
    Communicator communicator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communicator = (Communicator) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.difficulty_dialog, container, false);

        getDialog().setTitle("Difficulty");
        Button button[] = new Button[4];

        button[0] = (Button) rootView.findViewById(R.id.Easy);
        button[1] = (Button) rootView.findViewById(R.id.Medium);
        button[2] = (Button) rootView.findViewById(R.id.Hard);
        button[3] = (Button) rootView.findViewById(R.id.Insane);

        for (int i = 0; i < 4; i++) {
            final int finalI = i;
            button[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    communicator.onDialogMessage(finalI);
                }
            });
        }
        return rootView;
    }

    interface Communicator {
        void onDialogMessage(int message);
    }
}