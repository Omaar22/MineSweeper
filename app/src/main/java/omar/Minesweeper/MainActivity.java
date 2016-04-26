package omar.Minesweeper;

import android.os.Bundle;
import android.os.Handler;
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
    static boolean isRevealed[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static int neighbors[][] = new int[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static boolean hasFlag[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static boolean hasQuestionMark[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];

    int revealedCounter = 0;
    boolean isActive = true;
    int deltaXY[][] = new int[][]{{1, 0}, {0, 1}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    void generateGrid() {
        /* Generate mines */
        for (int i = 0; i < MINE_COUNT; i++) {
            Random rand = new Random();
            int randomNum = -1;
            while (randomNum == -1 || hasMine[randomNum / COLUMN_COUNT][randomNum % COLUMN_COUNT])
                randomNum = (rand.nextInt(GRID_SIZE) * 20707) % GRID_SIZE; // multiplied by some prime

            hasMine[randomNum / COLUMN_COUNT][randomNum % COLUMN_COUNT] = true;
        }

        /* Generate numbers*/
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

    void setupGrid() {
        final GridView grid = (GridView) findViewById(R.id.grid);
        grid.setNumColumns(COLUMN_COUNT);
        grid.setAdapter(new ImageAdapter(this));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int row = position / COLUMN_COUNT;
                int column = position % COLUMN_COUNT;
                if (!isActive || isRevealed[row][column] || hasFlag[row][column] || hasQuestionMark[row][column])
                    return;

                if (hasMine[row][column]) {
                    isActive = false;
                    ((Chronometer) findViewById(R.id.chronometer)).stop();

                    for (int i = 0; i < ROW_COUNT; i++) {
                        for (int j = 0; j < COLUMN_COUNT; j++) {
                            ImageView cell = (ImageView) (grid.getChildAt(i * COLUMN_COUNT + j));

                            if (hasFlag[i][j] && !hasMine[i][j])
                                cell.setImageResource(R.drawable.wrong_flag);
                            if (!hasFlag[i][j] && hasMine[i][j])
                                cell.setImageResource(R.drawable.mine);
                        }
                    }

                    ((ImageView) v).setImageResource(R.drawable.opened_mine);

                    findViewById(R.id.restart).setVisibility(View.VISIBLE);
                    changeSmiley(findViewById(R.id.smiley));
                } else
                    floodFill(row, column);

                if (revealedCounter == GRID_SIZE - MINE_COUNT) {
                    ((Chronometer) findViewById(R.id.chronometer)).stop();
                    isActive = false;

                    changeSmiley(findViewById(R.id.smiley));
                    for (int i = 0; i < ROW_COUNT; i++) {
                        for (int j = 0; j < COLUMN_COUNT; j++) {
                            if (!isRevealed[i][j]) {
                                ImageView cell = (ImageView) (grid.getChildAt(i * COLUMN_COUNT + j));
                                cell.setImageResource(R.drawable.flag);
                            }
                        }
                    }
                    findViewById(R.id.restart).setVisibility(View.VISIBLE);
                }
            }
        });
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                int row = position / COLUMN_COUNT;
                int column = position % COLUMN_COUNT;
                if (!isActive || isRevealed[row][column])
                    return true;

                if (hasFlag[row][column]) {
                    ((ImageView) v).setImageResource(R.drawable.question_mark);
                    hasFlag[row][column] = false;
                    hasQuestionMark[row][column] = true;
                } else if (hasQuestionMark[row][column]) {
                    ((ImageView) v).setImageResource(R.drawable.field);
                    hasQuestionMark[row][column] = false;
                } else {
                    ((ImageView) v).setImageResource(R.drawable.flag);
                    hasFlag[row][column] = true;
                }

                return true;
            }
        });
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        /* Hide the status bar. */
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // TODO: The system bars are visible. Make any desired
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            // Actions to do after 1 second
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                        }
                    }, 1000);
                }
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Set up the chronometer. */
        Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        int screenHeight = getBaseContext().getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getBaseContext().getResources().getDisplayMetrics().widthPixels;
        timer.setTextSize(screenHeight / 40);
        timer.start();

        /* Set up the smiley face. */
        ImageView smiley = (ImageView) findViewById(R.id.smiley);
        smiley.getLayoutParams().height = screenHeight / 10;
        smiley.getLayoutParams().width = screenHeight / 10;

        /* Set up the grid images and on click functions. */
        setupGrid();

        /* Generate the grid mines and numbers. */
        generateGrid();

        /* Set up the restart button. */
        Button restart = (Button) findViewById(R.id.restart);
        restart.setTextSize(screenHeight / 30f);
    }


    /* Reveal neighbors non-mine cells. */
    void floodFill(int row, int column) {
        GridView grid = (GridView) findViewById(R.id.grid);
        ImageView cell = (ImageView) grid.getChildAt(row * COLUMN_COUNT + column);

        if (0 <= row && row < ROW_COUNT && 0 <= column && column < COLUMN_COUNT) {
            if (hasMine[row][column] || isRevealed[row][column] || hasFlag[row][column] || hasQuestionMark[row][column])
                return;
            isRevealed[row][column] = true;

            if (neighbors[row][column] == 0) {
                cell.setImageResource(R.drawable.clear);
                for (int k = 0; k < 8; k++) {
                    floodFill(row + deltaXY[k][0], column + deltaXY[k][1]);
                }
            } else {
                cell.setImageResource(ImageAdapter.numbers[neighbors[row][column]]);
            }
            revealedCounter++;
        }
    }

    /* On click restart button. */
    public void restartButton(View view) {
        view.setVisibility(View.INVISIBLE);
        isActive = true;
        revealedCounter = 0;

        for (int i = 0; i < ROW_COUNT; i++)
            for (int j = 0; j < COLUMN_COUNT; j++)
                hasMine[i][j] = isRevealed[i][j] = hasQuestionMark[i][j] = hasFlag[i][j] = false;

        changeSmiley(findViewById(R.id.smiley));

        generateGrid();
        ((GridView) findViewById(R.id.grid)).setAdapter(new ImageAdapter(getBaseContext()));

        Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }

    /* On click smiley change smiley face depends on current state. */
    public void changeSmiley(View view) {
        ImageView smiley = (ImageView) view;
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

// TODO: 25/04/2016  GUI Again => setup resolution
// TODO: 26/04/2016 save flags state when keyboard shows
// TODO: 26/04/2016 show all mines when lose
// TODO: 25/04/2016  Add difficulties
// TODO: 25/04/2016  Scoring
// TODO: 25/04/2016  Add some sounds
// TODO: 25/04/2016  Create BOT with a remarkable name
