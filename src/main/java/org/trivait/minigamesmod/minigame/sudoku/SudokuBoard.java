package org.trivait.minigamesmod.minigame.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuBoard {

    private final int[][] solution = new int[9][9];
    private final int[][] puzzle = new int[9][9];
    private final boolean[][] initial = new boolean[9][9];
    private final Random random = new Random();
    private final int cellsToRemove;

    public SudokuBoard(int cellsToRemove) {
        this.cellsToRemove = cellsToRemove;
        generateBase();
        createPuzzle();
    }

    private void generateBase() {
        fillDiagonal();
        fillRemaining(0, 3);
    }

    private void fillDiagonal() {
        for (int i = 0; i < 9; i += 3) {
            fillBox(i, i);
        }
    }

    private void fillBox(int row, int col) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, random);
        int index = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                solution[row + r][col + c] = numbers.get(index++);
            }
        }
    }

    private boolean fillRemaining(int r, int c) {
        if (c >= 9 && r < 8) {
            r = r + 1;
            c = 0;
        }
        if (r >= 9 && c >= 9) {
            return true;
        }
        if (r < 3) {
            if (c < 3) {
                c = 3;
            }
        } else if (r < 6) {
            if (c == (int) (r / 3) * 3) {
                c = c + 3;
            }
        } else {
            if (c == 6) {
                r = r + 1;
                c = 0;
                if (r >= 9) {
                    return true;
                }
            }
        }
        for (int num = 1; num <= 9; num++) {
            if (checkSafe(r, c, num)) {
                solution[r][c] = num;
                if (fillRemaining(r, c + 1)) {
                    return true;
                }
                solution[r][c] = 0;
            }
        }
        return false;
    }

    private boolean checkSafe(int r, int c, int num) {
        for (int col = 0; col < 9; col++) {
            if (solution[r][col] == num) {
                return false;
            }
        }
        for (int row = 0; row < 9; row++) {
            if (solution[row][c] == num) {
                return false;
            }
        }
        int boxRowStart = r - r % 3;
        int boxColStart = c - c % 3;
        for (int rIndex = 0; rIndex < 3; rIndex++) {
            for (int cIndex = 0; cIndex < 3; cIndex++) {
                if (solution[boxRowStart + rIndex][boxColStart + cIndex] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createPuzzle() {
        for (int r = 0; r < 9; r++) {
            System.arraycopy(solution[r], 0, puzzle[r], 0, 9);
        }
        int remaining = cellsToRemove;
        while (remaining > 0) {
            int cellId = random.nextInt(81);
            int r = cellId / 9;
            int c = cellId % 9;
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0;
                remaining--;
            }
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                initial[r][c] = (puzzle[r][c] != 0);
            }
        }
    }

    public int[][] getPuzzle() {
        return puzzle;
    }

    public boolean[][] getInitial() {
        return initial;
    }

    public int[][] getSolution() {
        return solution;
    }
}