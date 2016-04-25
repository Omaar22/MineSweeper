package omar.theperfectapp;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    static final int GRID_SIZE = 300;
    static final int MINE_COUNT = 10;
    static final int COLUMN_COUNT = 15;
    static final int ROW_COUNT = GRID_SIZE / COLUMN_COUNT;

    static boolean hasMine[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static int neighbors[][] = new int[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static boolean isRevealed[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    int revealedCounter = 0;
    boolean isActive = true;
    int deltaXY[][] = new int[][]{{1, 0}, {0, 1}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

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
                        int X = i + deltaXY[k][0];
                        int Y = j + deltaXY[k][1];
                        if (0 <= X && X < GRID_SIZE / COLUMN_COUNT && 0 <= Y && Y < COLUMN_COUNT && hasMine[X][Y])
                            neighbours++;
                    }
                    neighbors[i][j] = neighbours;
                }
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        final int screenHeight = getBaseContext().getResources().getDisplayMetrics().heightPixels;
        final int screenWidth = getBaseContext().getResources().getDisplayMetrics().widthPixels;

        timer.setTextSize(screenHeight / 40);
        timer.start();

        final ImageView smiley = (ImageView) findViewById(R.id.smiley);
        smiley.getLayoutParams().height = screenHeight / 10;
        smiley.getLayoutParams().width = screenHeight / 10;

        generateBoard();

        final GridView fields = (GridView) findViewById(R.id.board);
        fields.setNumColumns(COLUMN_COUNT);

        fields.setAdapter(new ImageAdapter(this));
        fields.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                int row = position / COLUMN_COUNT;
                int column = position % COLUMN_COUNT;

                if (!isActive || isRevealed[row][column])
                    return;

                if (hasMine[row][column]) {
                    ((ImageView) v).setImageResource(R.drawable.mine);
                    timer.stop();
                    findViewById(R.id.restart).setVisibility(View.VISIBLE);
                    isActive = false;
                    changeSmiley(smiley);
                } else {
                    floodFill(row, column, fields);
                }

                if (revealedCounter == GRID_SIZE - MINE_COUNT) {
                    timer.stop();
                    findViewById(R.id.restart).setVisibility(View.VISIBLE);
                    isActive = false;
                    changeSmiley(smiley);
                }
            }
        });


        Button restart = (Button) findViewById(R.id.restart);
        restart.setTextSize(screenHeight / 30f);
    }

    void floodFill(int X, int Y, GridView board) {
        ImageView cell = (ImageView) board.getChildAt(X * COLUMN_COUNT + Y);

        if (0 <= X && X < ROW_COUNT && 0 <= Y && Y < COLUMN_COUNT) {
            if (hasMine[X][Y] || isRevealed[X][Y])
                return;
            isRevealed[X][Y] = true;

            if (neighbors[X][Y] == 0) {
                cell.setImageResource(R.drawable.clear);
                for (int k = 0; k < 8; k++) {
                    floodFill(X + deltaXY[k][0], Y + deltaXY[k][1], board);
                }
            } else {
                cell.setImageResource(ImageAdapter.numbers[neighbors[X][Y]]);
            }
            revealedCounter++;
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
                hasMine[i][j] = isRevealed[i][j] = false;

        generateBoard();

        GridView fields = (GridView) findViewById(R.id.board);
        fields.setAdapter(new ImageAdapter(getBaseContext()));

        Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }

    public void changeSmiley(View view) {
        ImageView smiley = (ImageView) findViewById(R.id.smiley);
        Object tag = smiley.getTag();

        int newSmiley;
        if (isActive) {
            if (tag != null && (Integer) tag == R.drawable.mental)
                newSmiley = R.drawable.stoned;
            else if (tag != null && (Integer) tag == R.drawable.stoned)
                newSmiley = R.drawable.salivating;
            else
                newSmiley = R.drawable.mental;
            smiley.setTag(newSmiley);
            smiley.setImageResource(newSmiley);
        } else if (revealedCounter == GRID_SIZE - MINE_COUNT) {
            if (tag != null && (Integer) tag == R.drawable.happy)
                newSmiley = R.drawable.sunglasses;
            else if (tag != null && (Integer) tag == R.drawable.sunglasses)
                newSmiley = R.drawable.laughing;
            else
                newSmiley = R.drawable.happy;
            smiley.setTag(newSmiley);
            smiley.setImageResource(newSmiley);
        } else {
            if (tag != null && (Integer) tag == R.drawable.petrified)
                newSmiley = R.drawable.crying;
            else if (tag != null && (Integer) tag == R.drawable.vulnerable)
                newSmiley = R.drawable.petrified;
            else if (tag != null && (Integer) tag == R.drawable.crying)
                newSmiley = R.drawable.horrified;
            else
                newSmiley = R.drawable.vulnerable;

            smiley.setTag(newSmiley);
            smiley.setImageResource(newSmiley);
        }
    }
}

// TODO: 25/04/2016  GUI Again // setup resolution
// TODO: 25/04/2016  Add Flags and Question marks
// TODO: 25/04/2016  Add difficulties
// TODO: 25/04/2016  Scoring
// TODO: 25/04/2016  Add some sounds
// TODO: 25/04/2016  Create BOT with a remarkable name

