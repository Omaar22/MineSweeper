package omar.theperfectapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    final int GRID_SIZE = 300;
    final int MINE_COUNT = 50;
    static final int COLUMN_SIZE = 20;


    int indices[] = new int[MINE_COUNT];
    boolean hasMine[] = new boolean[GRID_SIZE];
    int board[][] = new int[GRID_SIZE / COLUMN_SIZE][COLUMN_SIZE];
    boolean vis = false;
    int delta[][] = new int[][]{
            {1, 0},
            {0, 1},
            {0, -1},
            {-1, 0},
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
    };

    void generateBoard() {
        /* generate Mines */
        for (int i = 0; i < MINE_COUNT; i++) {
            Random rand = new Random();
            int randomNum = -1;
            while (randomNum == -1 || hasMine[randomNum])
                randomNum = (rand.nextInt(GRID_SIZE) * 997) % GRID_SIZE;

            indices[i] = randomNum;
            hasMine[randomNum] = true;
            board[randomNum / COLUMN_SIZE][randomNum % COLUMN_SIZE] = 1;
        }

        /* generate Numbers */
        for (int i = 0; i < GRID_SIZE / COLUMN_SIZE; i++) {
            for (int j = 0; j < COLUMN_SIZE; j++) {
                int neighbours = 0;
                if (!hasMine[i * COLUMN_SIZE + j]) {
                    for (int k = 0; k < 8; k++) {
                        int X = i + delta[k][0];
                        int Y = j + delta[k][1];
                        if (0 <= X && X < GRID_SIZE / COLUMN_SIZE && 0 <= Y && Y < COLUMN_SIZE && hasMine[X * COLUMN_SIZE + Y])
                            neighbours++;
                    }
                    board[i][j] = neighbours;
                }
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startChronometer(findViewById(R.id.chronometer));

        generateBoard();


        final GridView fields = (GridView) findViewById(R.id.fields);
        fields.setNumColumns(COLUMN_SIZE);

        fields.setAdapter(new ImageAdapter(this));

        fields.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                ImageView tmp = (ImageView) v;

                if (hasMine[position]) {
                    tmp.setImageResource(ImageAdapter.images[0]);
                } else if (board[position / COLUMN_SIZE][position % COLUMN_SIZE] == 0) {
                    show(position / COLUMN_SIZE, position % COLUMN_SIZE, fields);
                } else
                    tmp.setImageResource(ImageAdapter.images[board[position / COLUMN_SIZE][position % COLUMN_SIZE]]);

            }
        });
    }


    void show(int X, int Y, GridView v) {
        ImageView cell = (ImageView) v.getChildAt(X * COLUMN_SIZE + Y);

        if (0 <= X && X < GRID_SIZE / COLUMN_SIZE && 0 <= Y && Y < COLUMN_SIZE) {
            if (cell.getImageAlpha() == 40)
                return;

            if (board[X][Y] == 0) {
                cell.setImageResource(R.drawable.clear);
                cell.setImageAlpha(40);
                for (int k = 0; k < 8; k++) {
                    show(X + delta[k][0], Y + delta[k][1], v);
                }
            } else {
                cell.setImageResource(ImageAdapter.images[board[X][Y]]);
            }
        }
    }


    public void startChronometer(View view) {
        ((Chronometer) findViewById(R.id.chronometer)).start();
    }
}
