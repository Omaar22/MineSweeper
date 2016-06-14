package omar.Minesweeper;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Random;

public class Game {
    final int GRID_SIZE = 300;
    final int COLUMN_COUNT = 15;
    final int ROW_COUNT = GRID_SIZE / COLUMN_COUNT;
    boolean revealOnClick = true;
    int minesCount;

    boolean hasMine[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    boolean isRevealed[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    int neighbors[][] = new int[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    boolean hasFlag[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];
    boolean hasQuestionMark[][] = new boolean[GRID_SIZE / COLUMN_COUNT][COLUMN_COUNT];

    int revealedCounter = 0;

    boolean isActive = true;
    int[][] deltaXY = new int[][]{{1, 0}, {0, 1}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    boolean mute = false;

    Activity activity;
    Context baseContext;

    Game(Activity activity, Context baseContext) {
        this.activity = activity;
        this.baseContext = baseContext;
    }

    void reveal(int row, int column) {
        GridView grid = (GridView) activity.findViewById(R.id.grid);

        if (!isActive || isRevealed[row][column] || hasFlag[row][column] || hasQuestionMark[row][column]) {
            playSound(R.raw.error);
            return;
        }
        if (hasMine[row][column]) {
            ((Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(800);
            playSound(R.raw.boom);
            isActive = false;
            ((Chronometer) activity.findViewById(R.id.chronometer)).stop();

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

        } else {
            playSound(R.raw.button_sound);
            floodFill(row, column);
        }

        /*check if the board is completed */
        if (revealedCounter == GRID_SIZE - minesCount) {
            playSound(R.raw.tada);
            ((Chronometer) activity.findViewById(R.id.chronometer)).stop();
            isActive = false;

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

        GridView grid = (GridView) activity.findViewById(R.id.grid);
        ImageView v = (ImageView) (grid.getChildAt(row * COLUMN_COUNT + column));

        if (!isActive || isRevealed[row][column]) {
            playSound(R.raw.error);
            return;
        }
        playSound(R.raw.woosh);

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

    void generateGrid() {
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

    /* Reveal neighbors non-mine cells. */
    void floodFill(int row, int column) {
        GridView grid = (GridView) activity.findViewById(R.id.grid);
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


    void playSound(int id) {
        if (!mute) {
            final MediaPlayer clickSound = MediaPlayer.create(baseContext, id);
            clickSound.start();
            clickSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    clickSound.release();
                }
            });
        }
    }
}
