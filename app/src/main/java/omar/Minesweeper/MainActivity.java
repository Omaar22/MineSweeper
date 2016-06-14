package omar.Minesweeper;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
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

    Game myGame = null;
    Agent myBot = null;

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

        if (myGame == null)
            myGame = new Game(this, getBaseContext());

        if (myBot == null)
            myBot = new Agent(myGame.GRID_SIZE, myGame.COLUMN_COUNT, myGame.isRevealed, myGame.neighbors);

        final int screenWidth = this.getResources().getDisplayMetrics().widthPixels - 64; // padding 32

        /* Start the chronometer. */
        Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        timer.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (screenWidth / 8.5));

        /* Setting up the smiley face. */
        ImageView smiley = (ImageView) findViewById(R.id.smiley);
        smiley.getLayoutParams().width = screenWidth / 5;
        smiley.getLayoutParams().height = screenWidth / 5;
        changeSmiley();

        ImageButton clickIcon = (ImageButton) findViewById(R.id.clickIcon);
        clickIcon.getLayoutParams().width = screenWidth / 5;
        clickIcon.getLayoutParams().height = screenWidth / 5;

//        /* Set up the grid images and on click functions. */
        setupGrid();
//
//        /* Generate the grid mines and numbers. */
        myGame.generateGrid();

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
                if (myGame.revealOnClick) {
                    myGame.revealOnClick = false;
                    ((ImageButton) v).setImageResource(R.drawable.flag_icon);
                } else {
                    myGame.revealOnClick = true;
                    ((ImageButton) v).setImageResource(R.drawable.icon);
                }

            }
        });

        /* On click settings button. */
        findViewById(R.id.audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myGame.mute) {
                    ((ImageButton) v).setImageResource(R.drawable.audio);
                    myGame.mute = false;
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.mute);
                    myGame.mute = true;
                }
            }
        });

        /* On click hint_button button. */
        findViewById(R.id.hint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myGame.isActive) {
                    final int index = myBot.nextMove();

                    if (index != -1) {
                        GridView grid = (GridView) findViewById(R.id.grid);
                        final ImageView cell = (ImageView) grid.getChildAt(index);
                        Handler mHandler = new Handler();
                        cell.setImageResource(R.drawable.hint_field);

                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                // Actions to do after 1 second
                                if (!myGame.isRevealed[index / myGame.COLUMN_COUNT][index % myGame.COLUMN_COUNT])
                                    cell.setImageResource(R.drawable.field);
                            }
                        }, 2000);
                    } else {
                        Toast.makeText(getBaseContext(), "Help Yourself!!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        /* Pop up the start dialog. */
        FragmentManager manager = getSupportFragmentManager();
        DifficultyDialog dialog = new DifficultyDialog();
        dialog.setCancelable(false);
        dialog.show(manager, "Initial");
    }


    /* On click smiley change smiley face depends on current state. */
    public void changeSmiley() {
        ImageView smiley = (ImageView) findViewById(R.id.smiley);
        if (myGame.isActive) {
            int index = new Random().nextInt(3);
            if (index == 0)
                smiley.setImageResource(R.drawable.stoned);
            else if (index == 1)
                smiley.setImageResource(R.drawable.salivating);
            else
                smiley.setImageResource(R.drawable.mental);
        } else if (myGame.revealedCounter == myGame.GRID_SIZE - myGame.minesCount) {
            int index = new Random().nextInt(3);
            if (index == 0)
                smiley.setImageResource(R.drawable.sunglasses);
            else if (index == 1)
                smiley.setImageResource(R.drawable.happy);
            else
                smiley.setImageResource(R.drawable.laughing);
        } else {
            int index = new Random().nextInt(4);
            if (index == 0)
                smiley.setImageResource(R.drawable.vulnerable);
            else if (index == 1)
                smiley.setImageResource(R.drawable.crying);
            else if (index == 2)
                smiley.setImageResource(R.drawable.petrified);
            else
                smiley.setImageResource(R.drawable.horrified);
        }

    }

    void setupGrid() {
        final GridView grid = (GridView) findViewById(R.id.grid);
        grid.setNumColumns(myGame.COLUMN_COUNT);
        grid.setAdapter(new ImageAdapter(this, myGame));

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int row = position / myGame.COLUMN_COUNT;
                int column = position % myGame.COLUMN_COUNT;

                if (myGame.revealNeighbors(row, column))
                    ;
                else if (myGame.revealOnClick) {
                    myGame.reveal(row, column);
                    if (myGame.hasMine[row][column]) {
                        changeSmiley();
                    } else if (myGame.revealedCounter == myGame.GRID_SIZE - myGame.minesCount) {
                        changeSmiley();
                    }
                } else
                    myGame.flag(row, column);
            }
        });
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                int row = position / myGame.COLUMN_COUNT;
                int column = position % myGame.COLUMN_COUNT;

                if (myGame.revealNeighbors(row, column))
                    ;
                else if (myGame.revealOnClick)
                    myGame.flag(row, column);
                else {
                    myGame.reveal(row, column);
                    if (myGame.hasMine[row][column]) {
                        changeSmiley();
                    } else if (myGame.revealedCounter == myGame.GRID_SIZE - myGame.minesCount) {
                        changeSmiley();
                    }
                }
                return true;
            }
        });


    }

    public void resetGame() {
        myGame.isActive = true;
        myGame.revealedCounter = 0;
        for (int i = 0; i < myGame.ROW_COUNT; i++)
            for (int j = 0; j < myGame.COLUMN_COUNT; j++)
                myGame.hasMine[i][j] = myGame.isRevealed[i][j] = myGame.hasQuestionMark[i][j] = myGame.hasFlag[i][j] = false;

        myBot = new Agent(myGame.GRID_SIZE, myGame.COLUMN_COUNT, myGame.isRevealed, myGame.neighbors);

        changeSmiley();

        myGame.generateGrid();
        ((GridView) findViewById(R.id.grid)).setAdapter(new ImageAdapter(getBaseContext(), myGame));

        Chronometer timer = (Chronometer) findViewById(R.id.chronometer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }


    @Override
    public void onDialogMessage(int message) {
        if (message == 0)
            myGame.minesCount = 15;
        if (message == 1)
            myGame.minesCount = 25;
        if (message == 2)
            myGame.minesCount = 35;
        if (message == 3)
            myGame.minesCount = 50;

        resetGame();
    }


}

// TODO: 25/04/2016  Scoring and leaderboard
