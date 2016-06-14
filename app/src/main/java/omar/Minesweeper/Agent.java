package omar.Minesweeper;

import android.util.Log;

public class Agent {

    private final int GRID_SIZE;
    private final int COLUMN_COUNT;
    private final int ROW_COUNT;
    private boolean isRevealed[][];
    private int neighbors[][];
    private boolean isMine[][];
    int deltaXY[][] = new int[][]{{1, 0}, {0, 1}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    Agent(int GRID_SIZE, int COLUMN_COUNT, boolean isRevealed[][], int neighbors[][]) {
        this.GRID_SIZE = GRID_SIZE;
        this.COLUMN_COUNT = COLUMN_COUNT;
        ROW_COUNT = GRID_SIZE / COLUMN_COUNT;
        this.isRevealed = isRevealed.clone();
        this.neighbors = neighbors.clone();
        isMine = new boolean[ROW_COUNT][COLUMN_COUNT];
    }

    void setFlags() {
        boolean change = true;
        while (change) {
            change = false;
            for (int i = 0; i < ROW_COUNT; i++) {
                for (int j = 0; j < COLUMN_COUNT; j++) {
                    int unknown = 0;
                    int mines = 0;
                    if (isRevealed[i][j] && neighbors[i][j] != 0) {
                        for (int k = 0; k < 8; k++) {
                            int X = i + deltaXY[k][0];
                            int Y = j + deltaXY[k][1];
                            if (0 <= X && X < ROW_COUNT && 0 <= Y && Y < COLUMN_COUNT) {
                                if (isMine[X][Y])
                                    mines++;
                                else if (!isRevealed[X][Y])
                                    unknown++;
                            }
                        }

                        if (unknown + mines == neighbors[i][j]) {
                            for (int k = 0; k < 8; k++) {
                                int R = i + deltaXY[k][0];
                                int C = j + deltaXY[k][1];
                                if (0 <= R && R < ROW_COUNT && 0 <= C && C < COLUMN_COUNT) {
                                    if (!isRevealed[R][C] && !isMine[R][C]) {
                                        isMine[R][C] = true;
                                        change = true;
                                        Log.d("Omar", "straightforward: " + R + " " + C + "FROM: " + i + " " + j + " #" + neighbors[i][j]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    int straightforward() {
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                int unknown = 0;
                int total = 0;
                int mines = 0;
                if (isRevealed[i][j] && neighbors[i][j] != 0) {
                    for (int k = 0; k < 8; k++) {
                        int R = i + deltaXY[k][0];
                        int C = j + deltaXY[k][1];
                        if (0 <= R && R < ROW_COUNT && 0 <= C && C < COLUMN_COUNT) {
                            if (isMine[R][C])
                                mines++;
                            else if (!isRevealed[R][C])
                                unknown++;

                            total++;
                        }
                    }

                    if (mines != 0)
                        Log.d("Omar", "straightforward: (" + i + " " + j + ")");
                    if (unknown != 0 && mines == neighbors[i][j]) {
                        for (int k = 0; k < 8; k++) {
                            int R = i + deltaXY[k][0];
                            int C = j + deltaXY[k][1];
                            if (0 <= R && R < ROW_COUNT && 0 <= C && C < COLUMN_COUNT) {
                                if (!isRevealed[R][C] && !isMine[R][C]) {
                                    Log.d("Omar", "straightforward: Return: " + R + " " + C + " From: " + i + " " + j);
                                    return R * COLUMN_COUNT + C;
                                }
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    int probability() {

        return -1;
    }

    /* return index of available cell */
    int nextMove() {
        setFlags();

        int ret = straightforward();

        return ret;
    }
}

