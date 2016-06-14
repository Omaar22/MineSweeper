package omar.Minesweeper;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements DifficultyDialog.Communicator {

    static final int GRID_SIZE = 300;
    static final int COLUMN_COUNT = 15;
    static final int ROW_COUNT = GRID_SIZE / COLUMN_COUNT;
    boolean revealOnClick = true;
    boolean mute = false;
    int minesCount;

    static boolean hasMine[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static boolean isRevealed[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static int neighbors[][] = new int[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static boolean hasFlag[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    static boolean hasQuestionMark[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];

    int revealedCounter = 0;
    boolean isActive = true;
    int deltaXY[][] = new int[][]{{1, 0}, {0, 1}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    void generateGrid() {
        Log.d("Omar", "generateGrid: " + minesCount);
        /* Generate mines */
        for (int i = 0; i < minesCount; i++) {
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
                        if (0 <= X && X < ROW_COUNT && 0 <= Y && Y < COLUMN_COUNT && hasMine[X][Y])
                            neighbours++;
                    }
                    neighbors[i][j] = neighbours;
                }
            }
        }
    }

    boolean revealNeighbors(int row, int column) {
        if (isActive && isRevealed[row][column]) {
            int flagsCount = 0;
            if (!hasMine[row][column]) {
                for (int k = 0; k < 8; k++) {
                    int X = row + deltaXY[k][0];
                    int Y = column + deltaXY[k][1];
                    if (0 <= X && X < GRID_SIZE / COLUMN_COUNT && 0 <= Y && Y < COLUMN_COUNT && hasFlag[X][Y])
                        flagsCount++;
                }
            }

            int newRevealedCount = 0;
            if (neighbors[row][column] == flagsCount) {
                for (int k = 0; k < 8; k++) {
                    int X = row + deltaXY[k][0];
                    int Y = column + deltaXY[k][1];
                    if (0 <= X && X < GRID_SIZE / COLUMN_COUNT && 0 <= Y && Y < COLUMN_COUNT && !hasFlag[X][Y] && !isRevealed[X][Y]) {
                        reveal(X, Y);
                        newRevealedCount++;
                    }
                }
                if (newRevealedCount != 0)
                    return true;
            }
        }
        return false;
    }

    void reveal(int row, int column) {
        GridView grid = (GridView) findViewById(R.id.grid);
        ImageView v = (ImageView) (grid.getChildAt(row * COLUMN_COUNT + column));


        if (!isActive || isRevealed[row][column] || hasFlag[row][column] || hasQuestionMark[row][column]) {
            if (!mute) {
                final MediaPlayer clickSound = MediaPlayer.create(getBaseContext(), R.raw.error);
                clickSound.start();
                clickSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        clickSound.release();
                    }
                });
            }
            return;
        }
        if (hasMine[row][column]) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(800);
            if (!mute) {
                final MediaPlayer boomSound = MediaPlayer.create(getBaseContext(), R.raw.boom);
                boomSound.start();
            }
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

            ((ImageView) (grid.getChildAt(row * COLUMN_COUNT + column))).setImageResource(R.drawable.opened_mine);

            changeSmiley(findViewById(R.id.smiley));
        } else {
            if (!mute) {
                final MediaPlayer clickSound = MediaPlayer.create(getBaseContext(), R.raw.button_sound);
                clickSound.start();
                clickSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        clickSound.release();
                    }
                });
            }

            floodFill(row, column);
        }

        /*check if the board is completed */
        if (revealedCounter == GRID_SIZE - minesCount) {
            if (!mute) {
                final MediaPlayer boomSound = MediaPlayer.create(getBaseContext(), R.raw.win);
                boomSound.start();
            }
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
        }
    }

    void flag(int row, int column) {
        GridView grid = (GridView) findViewById(R.id.grid);
        ImageView v = (ImageView) (grid.getChildAt(row * COLUMN_COUNT + column));

        if (!isActive || isRevealed[row][column]) {
            if (!mute) {
                final MediaPlayer clickSound = MediaPlayer.create(getBaseContext(), R.raw.error);
                clickSound.start();
                clickSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        clickSound.release();
                    }
                });
            }
            return;
        }
        if (!mute) {
            final MediaPlayer wooshSound = MediaPlayer.create(getBaseContext(), R.raw.woosh);
            wooshSound.start();
            wooshSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    wooshSound.release();
                }
            });
        }

        if (hasFlag[row][column]) {
            v.setImageResource(R.drawable.question_mark);
            hasFlag[row][column] = false;
            hasQuestionMark[row][column] = true;
        } else if (hasQuestionMark[row][column]) {
            v.setImageResource(R.drawable.field);
            hasQuestionMark[row][column] = false;
        } else {
            v.setImageResource(R.drawable.flag);
            hasFlag[row][column] = true;
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

                if (revealNeighbors(row, column))
                    ;
                else if (revealOnClick)
                    reveal(row, column);
                else
                    flag(row, column);
            }
        });
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                int row = position / COLUMN_COUNT;
                int column = position % COLUMN_COUNT;

                if (revealNeighbors(row, column))
                    ;
                else if (revealOnClick)
                    flag(row, column);
                else
                    reveal(row, column);

                return true;
            }
        });


    }

    /* Hide the status bar on resume. */
    @Override
    protected void onResume() {
        super.onResume();
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
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

        final int screenWidth = this.getResources().getDisplayMetrics().widthPixels - 64; // padding 32

        /* Start the chronometer. */
        Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        timer.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (screenWidth / 8.5));

        /* Setting up the smiley face. */
        ImageView smiley = (ImageView) findViewById(R.id.smiley);
        smiley.getLayoutParams().width = screenWidth / 5;
        smiley.getLayoutParams().height = screenWidth / 5;
        changeSmiley(smiley);


        ImageButton clickIcon = (ImageButton) findViewById(R.id.clickIcon);
        clickIcon.getLayoutParams().width = screenWidth / 5;
        clickIcon.getLayoutParams().height = screenWidth / 5;

//        /* Set up the grid images and on click functions. */
        setupGrid();
//
//        /* Generate the grid mines and numbers. */
        generateGrid();

        ImageButton replay = (ImageButton) findViewById(R.id.replay);
        replay.getLayoutParams().width = screenWidth / 5;
        replay.getLayoutParams().height = screenWidth / 5;

        ImageButton audio = (ImageButton) findViewById(R.id.audio);
        audio.getLayoutParams().width = screenWidth / 5;
        audio.getLayoutParams().height = screenWidth / 5;

        ImageButton hint = (ImageButton) findViewById(R.id.hint);
        hint.getLayoutParams().width = screenWidth / 5;
        hint.getLayoutParams().height = screenWidth / 5;


        /* On click replay. */
        findViewById(R.id.replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                DifficultyDialog dialog = new DifficultyDialog();
                dialog.setCancelable(true);
                dialog.show(manager, "On Click");
            }
        });

        /* On click reveal icon. */
        findViewById(R.id.clickIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (revealOnClick) {
                    revealOnClick = false;
                    ((ImageButton) v).setImageResource(R.drawable.flag_icon);
                } else {
                    revealOnClick = true;
                    ((ImageButton) v).setImageResource(R.drawable.icon);
                }

            }
        });

        /* On click settings button. */
        findViewById(R.id.audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mute) {
                    ((ImageButton) v).setImageResource(R.drawable.audio);
                    mute = false;
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.mute);
                    mute = true;
                }
            }
        });

        /* On click hint_button button. */
        findViewById(R.id.hint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Agent myBot = new Agent(GRID_SIZE, COLUMN_COUNT, isRevealed, neighbors);
                int index = myBot.nextMove();

                Log.d("Omar", "onClick: " + index);

                if (index != -1) {
                    GridView grid = (GridView) findViewById(R.id.grid);
                    ImageView cell = (ImageView) grid.getChildAt(index);
                    cell.setImageResource(R.drawable.hint_field);
                } else {
                    Toast.makeText(getBaseContext(), "Help Yourself!!", Toast.LENGTH_LONG).show();
                }

            }
        });

        /* Pop up the start dialog. */
        FragmentManager manager = getSupportFragmentManager();
        DifficultyDialog dialog = new DifficultyDialog();
        dialog.setCancelable(false);
        dialog.show(manager, "Initial");
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

    /* On click smiley change smiley face depends on current state. */
    public void changeSmiley(View view) {
        if (isActive) {
            int index = new Random().nextInt(3);
            if (index == 0)
                ((ImageView) view).setImageResource(R.drawable.stoned);
            else if (index == 1)
                ((ImageView) view).setImageResource(R.drawable.salivating);
            else
                ((ImageView) view).setImageResource(R.drawable.mental);
        } else if (revealedCounter == GRID_SIZE - minesCount) {
            int index = new Random().nextInt(3);
            if (index == 0)
                ((ImageView) view).setImageResource(R.drawable.sunglasses);
            else if (index == 1)
                ((ImageView) view).setImageResource(R.drawable.happy);
            else
                ((ImageView) view).setImageResource(R.drawable.laughing);
        } else {
            int index = new Random().nextInt(4);
            if (index == 0)
                ((ImageView) view).setImageResource(R.drawable.vulnerable);
            else if (index == 1)
                ((ImageView) view).setImageResource(R.drawable.crying);
            else if (index == 2)
                ((ImageView) view).setImageResource(R.drawable.petrified);
            else
                ((ImageView) view).setImageResource(R.drawable.horrified);
        }

    }

    public void resetEverything() {
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

    @Override
    public void onDialogMessage(int message) {
        if (message == 0)
            minesCount = 15;
        if (message == 1)
            minesCount = 25;
        if (message == 2)
            minesCount = 35;
        if (message == 3)
            minesCount = 50;

        resetEverything();
    }
}

// TODO: 25/04/2016  Scoring and leaderboard
// TODO: 25/04/2016  Create a smart BOT and give it a remarkable name
