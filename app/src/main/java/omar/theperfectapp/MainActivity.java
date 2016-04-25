package omar.theperfectapp;

import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    static final int GRID_SIZE = 300;
    static final int MINE_COUNT = 20;
    static final int COLUMN_COUNT = 15;
    static final int ROW_COUNT = GRID_SIZE / COLUMN_COUNT;

    boolean hasMine[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    int board[][] = new int[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];

    int delta[][] = new int[][]{
            {1, 0}, {0, 1}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    boolean vis[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    int revealedCounter = 0;
    boolean isActive = true;

    void generateBoard() {
        /* generate Mines */
        for (int i = 0; i < MINE_COUNT; i++) {
            Random rand = new Random();
            int randomNum = -1;
            while (randomNum == -1 || hasMine[randomNum / COLUMN_COUNT][randomNum % COLUMN_COUNT])
                randomNum = (rand.nextInt(GRID_SIZE) * 20707) % GRID_SIZE; // multiplied by some prime

            hasMine[randomNum / COLUMN_COUNT][randomNum % COLUMN_COUNT] = true;
        }

        /* generate Numbers*/
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                int neighbours = 0;
                if (!hasMine[i][j]) {
                    for (int k = 0; k < 8; k++) {
                        int X = i + delta[k][0];
                        int Y = j + delta[k][1];
                        if (0 <= X && X < GRID_SIZE / COLUMN_COUNT && 0 <= Y && Y < COLUMN_COUNT && hasMine[X][Y])
                            neighbours++;
                    }
                    board[i][j] = neighbours;
                }
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        final int height = getBaseContext().getResources().getDisplayMetrics().heightPixels;
        timer.setTextSize(height / 25f);
        timer.start();

        final ImageView smiley = (ImageView) findViewById(R.id.smiley);
        smiley.getLayoutParams().height = height / 10;
        smiley.getLayoutParams().width = height / 10;

        generateBoard();

        final GridView fields = (GridView) findViewById(R.id.fields);
        fields.setNumColumns(COLUMN_COUNT);
        fields.setAdapter(new ImageAdapter(this));
        fields.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                int X = position / COLUMN_COUNT;
                int Y = position % COLUMN_COUNT;

                if (!isActive || vis[X][Y])
                    return;

                ImageView tmp = (ImageView) v;

                if (hasMine[position / COLUMN_COUNT][position % COLUMN_COUNT]) {
                    tmp.setImageResource(ImageAdapter.images[0]);
                    tmp.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    smiley.setImageResource(R.drawable.vulnerable);

                    timer.stop();
                    findViewById(R.id.restart).setVisibility(View.VISIBLE);
                    isActive = false;
                } else if (board[position / COLUMN_COUNT][position % COLUMN_COUNT] == 0) {
                    floodFill(position / COLUMN_COUNT, position % COLUMN_COUNT, fields);
                } else {
                    tmp.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    tmp.setImageResource(ImageAdapter.images[board[position / COLUMN_COUNT][position % COLUMN_COUNT]]);
                    vis[X][Y] = true;
                    revealedCounter++;
                }

                if (revealedCounter == GRID_SIZE - MINE_COUNT) {
                    timer.stop();
                    findViewById(R.id.restart).setVisibility(View.VISIBLE);
                    isActive = false;
                    smiley.setImageResource(R.drawable.stoned);
                }
            }
        });

    }


    void floodFill(int X, int Y, GridView v) {
        ImageView cell = (ImageView) v.getChildAt(X * COLUMN_COUNT + Y);

        if (0 <= X && X < ROW_COUNT && 0 <= Y && Y < COLUMN_COUNT) {
            if (vis[X][Y])
                return;

            vis[X][Y] = true;

            revealedCounter++;

            if (board[X][Y] == 0) {
                cell.setImageResource(R.drawable.clear);
                cell.setImageAlpha(40);
                for (int k = 0; k < 8; k++) {
                    floodFill(X + delta[k][0], Y + delta[k][1], v);
                }
            } else {
                cell.setScaleType(ImageView.ScaleType.FIT_CENTER);
                cell.setImageResource(ImageAdapter.images[board[X][Y]]);
            }
        }
    }

    public void restartButton(View view) {
        ImageView smiley = (ImageView) findViewById(R.id.smiley);
        smiley.setImageResource(R.drawable.salivating);


        view.setVisibility(View.INVISIBLE);
        isActive = true;
        revealedCounter = 0;

        for (int i = 0; i < ROW_COUNT; i++)
            for (int j = 0; j < COLUMN_COUNT; j++)
                hasMine[i][j] = vis[i][j] = false;

        generateBoard();

        GridView fields = (GridView) findViewById(R.id.fields);
        fields.setAdapter(new ImageAdapter(getBaseContext()));

        Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }
}
