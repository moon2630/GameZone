package com.example.gamezone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WordSearchView extends View {

    private ArrayList<Paint> foundPaints = new ArrayList<>();


    // Game state
    private boolean gameRunning = false;
    private String difficulty = "easy";

    // Grid properties
    private int gridSize = 8; // Easy: 8x8, Medium: 10x10, Hard: 12x12
    private char[][] grid;
    private float cellSize;
    private float gridStartX, gridStartY;

    // Words to find
    private ArrayList<String> wordsToFind = new ArrayList<>();
    private ArrayList<String> foundWords = new ArrayList<>();
    private ArrayList<WordPosition> wordPositions = new ArrayList<>();

    // Word categories based on difficulty
    private String[][] wordCategories = {
            // EASY - 8x8 grid (50+ meaningful 3-4 letter words)
            {
                    "CAT", "DOG", "SUN", "MOON", "STAR", "TREE", "BOOK", "PEN", "CAR", "BAG",
                    "HAT", "MAT", "BED", "CUP", "KEY", "MAP", "BOX", "TOY", "FOX", "ANT",
                    "BEE", "EGG", "ICE", "JAM", "KIT", "LIP", "MAN", "NET", "OWL", "PIG",
                    "RAT", "SEA", "TAP", "URN", "VAN", "WAX", "ZOO", "BUS", "CAN", "DOT",
                    "EAR", "FAN", "GUM", "HOP", "INK", "JAR", "LEG", "MUG", "NAV", "OAK",
                    "PAL", "RAY", "SAD", "TIN", "USE", "VIA", "WET", "YES", "ZIP", "ARM",
                    "BAT", "COW", "DAY", "EGO", "FROG", "GOD", "HEN", "INK", "JOB", "KID",
                    "LOT", "MOM", "NUT", "OIL", "PET", "QUEEN", "RUN", "SIT", "TEA", "UP",
                    "VOW", "WIN", "XRAY", "YAK", "ZEN", "AIR", "BOY", "CRY", "DAD", "EYE",
                    "FUN", "GAS", "HUG", "IRON", "JET", "KITE", "LION", "MUD", "NEW", "OLD"
            },

            // MEDIUM - 10x10 grid (50+ meaningful 5-6 letter words)
            {
                    "APPLE", "BEACH", "CLOUD", "DREAM", "EARTH", "FLOWER", "GARDEN", "HOUSE", "ISLAND", "JUNGLE",
                    "KETTLE", "LIGHT", "MONEY", "NIGHT", "OCEAN", "PAPER", "QUEEN", "RIVER", "SMILE", "TABLE",
                    "UMBRELLA", "VOICE", "WATER", "YELLOW", "ZEBRA", "ANGEL", "BRIDGE", "CANDLE", "DANCER", "EAGLE",
                    "FAMILY", "GINGER", "HAPPY", "INSECT", "JELLY", "KANGAROO", "LEMON", "MIRROR", "NOVEL", "ORANGE",
                    "PENCIL", "QUIET", "RABBIT", "SNAKE", "TIGER", "UNICORN", "VIOLIN", "WHALE", "XRAY", "YOGURT",
                    "ZIPPER", "ALARM", "BREAD", "CHAIR", "DOLPHIN", "ELEPHANT", "FRIEND", "GLOBE", "HONEY", "IGLOO",
                    "JACKET", "KINGDOM", "LADDER", "MONKEY", "NATURE", "ORCHID", "PIZZA", "QUILT", "ROCKET", "SCHOOL",
                    "TEMPLE", "UNIVERSE", "VOLUME", "WINDOW", "YOUTH", "ZODIAC", "BANANA", "CASTLE", "DESERT", "ENERGY",
                    "FOREST", "GARDEN", "HEART", "ISLAND", "JOURNEY", "KITCHEN", "LAGOON", "MUSIC", "NEST", "OCEAN"
            },

            // HARD - 12x12 grid (50+ meaningful 7+ letter words)
            {
                    "BUTTERFLY", "COMPUTER", "ELEPHANT", "FIREWORK", "MOUNTAIN", "NOTEBOOK", "RAINBOW", "STRAWBERRY", "TELEPHONE", "UNIVERSITY",
                    "VOLCANO", "WATERMELON", "ADVENTURE", "BICYCLE", "CALENDAR", "DINOSAUR", "ELEVATOR", "FOOTBALL", "GOLDFISH", "HOSPITAL",
                    "IMPORTANT", "JELLYFISH", "KILOMETER", "LANGUAGE", "MICROSCOPE", "NOTEBOOK", "OCTOPUS", "PAINTING", "QUESTION", "RESTAURANT",
                    "SANDWICH", "TELEVISION", "UMBRELLA", "VACATION", "WEDNESDAY", "XMAS", "YOGA", "ZODIAC", "AIRPLANE", "BALLOON",
                    "CATERPILLAR", "DICTIONARY", "ENVELOPE", "FESTIVAL", "GYMNASTICS", "HURRICANE", "ILLUSION", "JACKET", "KARAOKE", "LAPTOP",
                    "MAGICIAN", "NEPTUNE", "ORCHESTRA", "PINEAPPLE", "QUIZ", "RADIO", "SPAGHETTI", "TELESCOPE", "UNIFORM", "VAMPIRE",
                    "WINDOW", "XYLOPHONE", "YOUNGER", "ZEALOUS", "ALPHABET", "BIRTHDAY", "CRAYON", "DOLPHIN", "EXERCISE", "FIREFIGHTER",
                    "GARDENER", "HAMBURGER", "INSTRUMENT", "JOURNAL", "KEYBOARD", "LIBRARY", "MEDICINE", "NECKLACE", "ORIGINAL", "PENGUIN",
                    "QUALITY", "RAINCOAT", "SCISSORS", "TEACHER", "UNIVERSE", "VICTORY", "WONDERFUL", "XENOPHONE", "YELLOW", "ZIGZAG"
            }
    };

    // Selection
    private ArrayList<int[]> selectedCells = new ArrayList<>();
    private float startX, startY;
    private boolean isSelecting = false;
    private Path selectionPath = new Path();

    // Found word highlighting
    private ArrayList<ArrayList<int[]>> foundWordCells = new ArrayList<>();

    // Paints
    private Paint gridPaint;
    private Paint textPaint;
    private Paint selectedPaint;
    private Paint foundPaint;
    private Paint hintPaint;
    private Paint linePaint;

    // Colors
    private int gridColor = Color.parseColor("#444444");
    private int textColor = Color.parseColor("#FFFFFF");
    private int selectedColor = Color.parseColor("#2196F3");
    private int foundColor = Color.parseColor("#4CAF50");
    private int wrongColor = Color.parseColor("#F44336");
    private int hintColor = Color.parseColor("#FF9800");

    private Random random = new Random();
    private String hintWord = "";
    private long hintEndTime = 0;

    public WordSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize paints
        gridPaint = new Paint();
        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2);




        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        // Try to load caudex font, fallback to default if not available
        try {
            Typeface caudex = Typeface.createFromAsset(getContext().getAssets(), "fonts/caudex.ttf");
            textPaint.setTypeface(caudex);
        } catch (Exception e) {
            // Font not found, use default
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        selectedPaint = new Paint();
        selectedPaint.setColor(selectedColor);
        selectedPaint.setAlpha(100);
        selectedPaint.setStyle(Paint.Style.FILL);

        foundPaint = new Paint();
        foundPaint.setColor(foundColor);
        foundPaint.setAlpha(150);
        foundPaint.setStyle(Paint.Style.FILL);

        hintPaint = new Paint();
        hintPaint.setColor(hintColor);
        hintPaint.setAlpha(100);
        hintPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.YELLOW);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4);
        linePaint.setAntiAlias(true);


        int[] foundColors = {
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#FFEB3B"), // Yellow
                Color.parseColor("#795548"), // Brown
                Color.parseColor("#607D8B"), // Blue Grey
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#8BC34A"), // Light Green
                Color.parseColor("#FF5722")  // Deep Orange
        };

        for (int color : foundColors) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setAlpha(180); // Slightly transparent
            paint.setStyle(Paint.Style.FILL);
            foundPaints.add(paint);
        }

    }

// Update these methods in WordSearchView.java

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        switch (difficulty) {
            case "easy":
                gridSize = 8;
                break;
            case "medium":
                gridSize = 10;
                break;
            case "hard":
                gridSize = 12;
                break;
        }
        createNewPuzzle();
    }

    public void createNewPuzzle() {
        // Clear previous state
        wordsToFind.clear();
        foundWords.clear();
        wordPositions.clear();
        selectedCells.clear();
        foundWordCells.clear();
        hintWord = "";
        gameRunning = true;

        // Get words based on difficulty
        int categoryIndex = 0;
        switch (difficulty) {
            case "easy": categoryIndex = 0; break;
            case "medium": categoryIndex = 1; break;
            case "hard": categoryIndex = 2; break;
        }

        // Select random words from category
        String[] categoryWords = wordCategories[categoryIndex];
        List<String> wordList = Arrays.asList(categoryWords);
        Collections.shuffle(wordList);

        // UPDATED WORD COUNTS
        int wordCount = 0;
        switch (difficulty) {
            case "easy": wordCount = 6; break;
            case "medium": wordCount = 9; break;  // Changed from 8 to 9
            case "hard": wordCount = 12; break;   // Changed from 10 to 12
        }

        for (int i = 0; i < Math.min(wordCount, wordList.size()); i++) {
            wordsToFind.add(wordList.get(i));
        }

        // Initialize grid with spaces
        grid = new char[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = ' ';
            }
        }

        // Place words in grid
        placeWordsInGrid();

        // Fill empty spaces with random letters
        fillEmptySpaces();

        // Invalidate to redraw
        invalidate();
    }

    private void placeWordsInGrid() {
        wordPositions.clear();

        for (String word : wordsToFind) {
            word = word.toUpperCase();
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 500) {
                attempts++;

                // Choose random direction (0-7)
                int direction = random.nextInt(8);

                // Calculate max possible starting positions based on word length and direction
                int maxRow = gridSize;
                int maxCol = gridSize;

                switch (direction) {
                    case 0: // Horizontal right
                        maxCol = gridSize - word.length();
                        break;
                    case 1: // Horizontal left
                        maxCol = word.length() - 1;
                        break;
                    case 2: // Vertical down
                        maxRow = gridSize - word.length();
                        break;
                    case 3: // Vertical up
                        maxRow = word.length() - 1;
                        break;
                    case 4: // Diagonal down-right
                        maxRow = gridSize - word.length();
                        maxCol = gridSize - word.length();
                        break;
                    case 5: // Diagonal down-left
                        maxRow = gridSize - word.length();
                        maxCol = word.length() - 1;
                        break;
                    case 6: // Diagonal up-right
                        maxRow = word.length() - 1;
                        maxCol = gridSize - word.length();
                        break;
                    case 7: // Diagonal up-left
                        maxRow = word.length() - 1;
                        maxCol = word.length() - 1;
                        break;
                }

                if (maxRow < 0 || maxCol < 0) continue;

                int startRow = random.nextInt(maxRow + 1);
                int startCol = random.nextInt(maxCol + 1);

                // Adjust start position for left/up directions
                if (direction == 1 || direction == 3 || direction == 5 || direction == 7) {
                    startRow = random.nextInt(gridSize);
                    startCol = random.nextInt(gridSize);

                    // Ensure the word fits in reverse direction
                    if (direction == 1 && startCol < word.length() - 1) continue;
                    if (direction == 3 && startRow < word.length() - 1) continue;
                    if (direction == 5 && (startRow > gridSize - word.length() || startCol < word.length() - 1)) continue;
                    if (direction == 7 && (startRow < word.length() - 1 || startCol < word.length() - 1)) continue;
                }

                // Check if word fits
                if (canPlaceWord(word, startRow, startCol, direction)) {
                    // Place word
                    placeWord(word, startRow, startCol, direction);
                    placed = true;

                    // Save word position
                    wordPositions.add(new WordPosition(word, startRow, startCol, direction));
                }
            }

            if (!placed) {
                // Simple placement as fallback
                for (int i = 0; i < word.length(); i++) {
                    int row = random.nextInt(gridSize);
                    int col = random.nextInt(gridSize);
                    grid[row][col] = word.charAt(i);
                }
            }
        }
    }

    private boolean canPlaceWord(String word, int row, int col, int direction) {
        for (int i = 0; i < word.length(); i++) {
            int currentRow = row;
            int currentCol = col;

            switch (direction) {
                case 0: // Right
                    currentCol = col + i;
                    break;
                case 1: // Left
                    currentCol = col - i;
                    break;
                case 2: // Down
                    currentRow = row + i;
                    break;
                case 3: // Up
                    currentRow = row - i;
                    break;
                case 4: // Down-Right
                    currentRow = row + i;
                    currentCol = col + i;
                    break;
                case 5: // Down-Left
                    currentRow = row + i;
                    currentCol = col - i;
                    break;
                case 6: // Up-Right
                    currentRow = row - i;
                    currentCol = col + i;
                    break;
                case 7: // Up-Left
                    currentRow = row - i;
                    currentCol = col - i;
                    break;
            }

            // Check bounds
            if (currentRow < 0 || currentRow >= gridSize || currentCol < 0 || currentCol >= gridSize) {
                return false;
            }

            // Check if cell is empty or has same letter
            char existingChar = grid[currentRow][currentCol];
            if (existingChar != ' ' && existingChar != word.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private void placeWord(String word, int row, int col, int direction) {
        for (int i = 0; i < word.length(); i++) {
            int currentRow = row;
            int currentCol = col;

            switch (direction) {
                case 0: // Right
                    currentCol = col + i;
                    break;
                case 1: // Left
                    currentCol = col - i;
                    break;
                case 2: // Down
                    currentRow = row + i;
                    break;
                case 3: // Up
                    currentRow = row - i;
                    break;
                case 4: // Down-Right
                    currentRow = row + i;
                    currentCol = col + i;
                    break;
                case 5: // Down-Left
                    currentRow = row + i;
                    currentCol = col - i;
                    break;
                case 6: // Up-Right
                    currentRow = row - i;
                    currentCol = col + i;
                    break;
                case 7: // Up-Left
                    currentRow = row - i;
                    currentCol = col - i;
                    break;
            }

            grid[currentRow][currentCol] = word.charAt(i);
        }
    }

    private void fillEmptySpaces() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (grid[i][j] == ' ') {
                    grid[i][j] = getRandomLetter();
                }
            }
        }
    }

    private char getRandomLetter() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return letters.charAt(random.nextInt(letters.length()));
    }

    public ArrayList<String> getWordsToFind() {
        return wordsToFind;
    }

    public ArrayList<String> getFoundWords() {
        return foundWords;
    }

    public int getFoundCount() {
        return foundWords.size();
    }

    public int getTotalWords() {
        return wordsToFind.size();
    }

    public String getSelectedWord() {
        StringBuilder word = new StringBuilder();
        for (int[] cell : selectedCells) {
            word.append(grid[cell[0]][cell[1]]);
        }
        return word.toString();
    }

    public void clearSelection() {
        selectedCells.clear();
        invalidate();
    }

    public void showHint() {
        if (foundWords.size() >= wordsToFind.size()) return;

        // Find a word that hasn't been found yet
        for (String word : wordsToFind) {
            if (!foundWords.contains(word)) {
                hintWord = word;
                hintEndTime = System.currentTimeMillis() + 2000; // Show hint for 2 seconds
                invalidate();

                // Auto-clear hint after 2 seconds
                postDelayed(() -> {
                    hintWord = "";
                    invalidate();
                }, 2000);
                break;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Adjust grid size based on difficulty
        float gridScale;
        switch (difficulty) {
            case "easy":
                gridScale = 0.85f; // 85% of screen for 8x8
                break;
            case "medium":
                gridScale = 0.85f; // 80% of screen for 10x10
                break;
            case "hard":
                gridScale = 0.90f; // 75% of screen for 12x12
                break;
            default:
                gridScale = 0.90f;
                break;
        }

        // Calculate cell size based on available space
        float availableSize = Math.min(w, h) * gridScale;
        cellSize = availableSize / gridSize;

        // Center the grid
        gridStartX = (w - (cellSize * gridSize)) / 2;
        gridStartY = (h - (cellSize * gridSize)) / 2;

        // Adjust text size based on cell size
        textPaint.setTextSize(cellSize * 0.55f);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (grid == null) return;

        // Different colors for each found word
        int[] colors = {
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#FFEB3B"), // Yellow
                Color.parseColor("#795548"), // Brown
                Color.parseColor("#607D8B"), // Blue Grey
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#8BC34A"), // Light Green
                Color.parseColor("#FF5722")  // Deep Orange
        };

        // Draw found words background with different colors
        for (int wordIndex = 0; wordIndex < foundWordCells.size(); wordIndex++) {
            ArrayList<int[]> wordCells = foundWordCells.get(wordIndex);
            int colorIndex = wordIndex % colors.length;

            Paint wordPaint = new Paint();
            wordPaint.setColor(colors[colorIndex]);
            wordPaint.setAlpha(180); // Slightly transparent
            wordPaint.setStyle(Paint.Style.FILL);

            for (int[] cell : wordCells) {
                float left = gridStartX + cell[1] * cellSize;
                float top = gridStartY + cell[0] * cellSize;
                canvas.drawRect(left, top, left + cellSize, top + cellSize, wordPaint);
            }
        }

        // Draw hint if active
        if (System.currentTimeMillis() < hintEndTime && !hintWord.isEmpty()) {
            // Find the word position
            for (WordPosition wp : wordPositions) {
                if (wp.word.equals(hintWord)) {
                    // Highlight each cell of the hinted word
                    for (int i = 0; i < wp.word.length(); i++) {
                        int currentRow = wp.startRow;
                        int currentCol = wp.startCol;

                        switch (wp.direction) {
                            case 0: currentCol = wp.startCol + i; break;
                            case 1: currentCol = wp.startCol - i; break;
                            case 2: currentRow = wp.startRow + i; break;
                            case 3: currentRow = wp.startRow - i; break;
                            case 4:
                                currentRow = wp.startRow + i;
                                currentCol = wp.startCol + i;
                                break;
                            case 5:
                                currentRow = wp.startRow + i;
                                currentCol = wp.startCol - i;
                                break;
                            case 6:
                                currentRow = wp.startRow - i;
                                currentCol = wp.startCol + i;
                                break;
                            case 7:
                                currentRow = wp.startRow - i;
                                currentCol = wp.startCol - i;
                                break;
                        }

                        float left = gridStartX + currentCol * cellSize;
                        float top = gridStartY + currentRow * cellSize;
                        canvas.drawRect(left, top, left + cellSize, top + cellSize, hintPaint);
                    }
                    break;
                }
            }
        }

        // Draw grid
        for (int i = 0; i <= gridSize; i++) {
            // Vertical lines
            float x = gridStartX + i * cellSize;
            canvas.drawLine(x, gridStartY, x, gridStartY + gridSize * cellSize, gridPaint);

            // Horizontal lines
            float y = gridStartY + i * cellSize;
            canvas.drawLine(gridStartX, y, gridStartX + gridSize * cellSize, y, gridPaint);
        }

        // Draw selected cells
        for (int[] cell : selectedCells) {
            float left = gridStartX + cell[1] * cellSize;
            float top = gridStartY + cell[0] * cellSize;
            canvas.drawRect(left, top, left + cellSize, top + cellSize, selectedPaint);
        }

        // Draw letters
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                float x = gridStartX + j * cellSize + cellSize / 2;
                float y = gridStartY + i * cellSize + cellSize / 2 + (textPaint.getTextSize() / 3);

                // Check if this cell is in a found word
                boolean isFound = false;
                for (ArrayList<int[]> wordCells : foundWordCells) {
                    for (int[] cell : wordCells) {
                        if (cell[0] == i && cell[1] == j) {
                            isFound = true;
                            break;
                        }
                    }
                    if (isFound) break;
                }

                // Set text color
                if (isFound) {
                    textPaint.setColor(Color.WHITE);
                } else {
                    textPaint.setColor(textColor);
                }

                canvas.drawText(String.valueOf(grid[i][j]), x, y, textPaint);
            }
        }

        // Draw selection line if selecting
        if (isSelecting && selectedCells.size() > 1) {
            drawSelectionLine(canvas);
        }
    }

    private void drawSelectionLine(Canvas canvas) {
        if (selectedCells.size() < 2) return;

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#FFD700")); // Gold color
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(cellSize * 0.15f);
        linePaint.setAntiAlias(true);

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.parseColor("#FFD700"));
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        Path linePath = new Path();
        float offset = cellSize / 2;

        // Get first cell center
        int[] firstCell = selectedCells.get(0);
        float startX = gridStartX + firstCell[1] * cellSize + offset;
        float startY = gridStartY + firstCell[0] * cellSize + offset;
        linePath.moveTo(startX, startY);
        canvas.drawCircle(startX, startY, cellSize * 0.12f, dotPaint);

        // Draw line through all selected cells
        for (int i = 1; i < selectedCells.size(); i++) {
            int[] cell = selectedCells.get(i);
            float x = gridStartX + cell[1] * cellSize + offset;
            float y = gridStartY + cell[0] * cellSize + offset;
            linePath.lineTo(x, y);
            canvas.drawCircle(x, y, cellSize * 0.12f, dotPaint);
        }

        canvas.drawPath(linePath, linePaint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gameRunning) return true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startSelection(x, y);
                return true;

            case MotionEvent.ACTION_MOVE:
                updateSelection(x, y);
                return true;

            case MotionEvent.ACTION_UP:
                endSelection();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void startSelection(float x, float y) {
        int[] cell = getCellAtPosition(x, y);
        if (cell != null) {
            selectedCells.clear();
            selectedCells.add(cell);
            startX = x;
            startY = y;
            isSelecting = true;
            invalidate();
        }
    }

    private void updateSelection(float x, float y) {
        if (!isSelecting) return;

        int[] cell = getCellAtPosition(x, y);
        if (cell != null) {
            // If this is the first cell or if cell is not already selected
            if (selectedCells.size() == 0 || !containsCell(selectedCells, cell)) {
                // Check if selection is valid
                if (isValidSelection(cell)) {
                    selectedCells.add(cell);
                    invalidate();
                }
            }
        }
    }

    private void endSelection() {
        if (!isSelecting) return;

        isSelecting = false;

        if (selectedCells.size() >= 2) {
            checkSelectedWord();
        } else {
            selectedCells.clear();
        }

        invalidate();
    }

    private int[] getCellAtPosition(float x, float y) {
        if (x < gridStartX || x > gridStartX + gridSize * cellSize ||
                y < gridStartY || y > gridStartY + gridSize * cellSize) {
            return null;
        }

        int col = (int) ((x - gridStartX) / cellSize);
        int row = (int) ((y - gridStartY) / cellSize);

        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            return new int[]{row, col};
        }

        return null;
    }

    private boolean containsCell(ArrayList<int[]> cells, int[] cell) {
        for (int[] c : cells) {
            if (c[0] == cell[0] && c[1] == cell[1]) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidSelection(int[] newCell) {
        if (selectedCells.size() == 0) return true;

        int[] lastCell = selectedCells.get(selectedCells.size() - 1);

        // For the first selection (second cell), allow any of the 8 directions
        if (selectedCells.size() == 1) {
            int rowDiff = newCell[0] - lastCell[0];
            int colDiff = newCell[1] - lastCell[1];

            // Must be adjacent (one step away) and not the same cell
            return (Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1) &&
                    !(rowDiff == 0 && colDiff == 0);
        }

        // For subsequent selections, must continue in the same direction
        int[] firstCell = selectedCells.get(0);
        int[] secondCell = selectedCells.get(1);

        // Calculate the direction from first to second cell
        int rowDir = secondCell[0] - firstCell[0];
        int colDir = secondCell[1] - firstCell[1];

        // Normalize direction to -1, 0, or 1
        if (rowDir != 0) rowDir = rowDir / Math.abs(rowDir);
        if (colDir != 0) colDir = colDir / Math.abs(colDir);

        // Check if new cell continues in the same direction
        int expectedRow = lastCell[0] + rowDir;
        int expectedCol = lastCell[1] + colDir;

        return (newCell[0] == expectedRow && newCell[1] == expectedCol);
    }

    private int getDirection(int[] fromCell, int[] toCell) {
        int rowDiff = toCell[0] - fromCell[0];
        int colDiff = toCell[1] - fromCell[1];

        // Normalize to -1, 0, or 1
        if (rowDiff != 0) rowDiff = rowDiff / Math.abs(rowDiff);
        if (colDiff != 0) colDiff = colDiff / Math.abs(colDiff);

        // Map to direction index
        if (rowDiff == 0 && colDiff == 1) return 0;   // Right
        if (rowDiff == 0 && colDiff == -1) return 1;  // Left
        if (rowDiff == 1 && colDiff == 0) return 2;   // Down
        if (rowDiff == -1 && colDiff == 0) return 3;  // Up
        if (rowDiff == 1 && colDiff == 1) return 4;   // Down-Right
        if (rowDiff == 1 && colDiff == -1) return 5;  // Down-Left
        if (rowDiff == -1 && colDiff == 1) return 6;  // Up-Right
        if (rowDiff == -1 && colDiff == -1) return 7; // Up-Left

        return -1; // Invalid direction
    }

    private boolean continuesInDirection(int[] fromCell, int[] toCell, int direction) {
        int expectedRow = fromCell[0];
        int expectedCol = fromCell[1];

        // Calculate expected position based on direction
        switch (direction) {
            case 0: // Right
                expectedCol = fromCell[1] + 1;
                break;
            case 1: // Left
                expectedCol = fromCell[1] - 1;
                break;
            case 2: // Down
                expectedRow = fromCell[0] + 1;
                break;
            case 3: // Up
                expectedRow = fromCell[0] - 1;
                break;
            case 4: // Down-Right
                expectedRow = fromCell[0] + 1;
                expectedCol = fromCell[1] + 1;
                break;
            case 5: // Down-Left
                expectedRow = fromCell[0] + 1;
                expectedCol = fromCell[1] - 1;
                break;
            case 6: // Up-Right
                expectedRow = fromCell[0] - 1;
                expectedCol = fromCell[1] + 1;
                break;
            case 7: // Up-Left
                expectedRow = fromCell[0] - 1;
                expectedCol = fromCell[1] - 1;
                break;
        }

        return (toCell[0] == expectedRow && toCell[1] == expectedCol);
    }

    private boolean canPlaceWordInDirection(String word, int row, int col, int direction) {
        int rowDir = 0, colDir = 0;

        // Set direction vectors
        switch (direction) {
            case 0: rowDir = 0; colDir = 1; break;    // Right
            case 1: rowDir = 0; colDir = -1; break;   // Left
            case 2: rowDir = 1; colDir = 0; break;    // Down
            case 3: rowDir = -1; colDir = 0; break;   // Up
            case 4: rowDir = 1; colDir = 1; break;    // Down-Right
            case 5: rowDir = 1; colDir = -1; break;   // Down-Left
            case 6: rowDir = -1; colDir = 1; break;   // Up-Right
            case 7: rowDir = -1; colDir = -1; break;  // Up-Left
        }

        // Check if word fits in the grid
        int endRow = row + (word.length() - 1) * rowDir;
        int endCol = col + (word.length() - 1) * colDir;

        if (endRow < 0 || endRow >= gridSize || endCol < 0 || endCol >= gridSize) {
            return false;
        }

        // Check if cells are empty or have matching letters
        for (int i = 0; i < word.length(); i++) {
            int currentRow = row + i * rowDir;
            int currentCol = col + i * colDir;

            char existingChar = grid[currentRow][currentCol];
            char wordChar = word.charAt(i);

            if (existingChar != ' ' && existingChar != wordChar) {
                return false;
            }
        }

        return true;
    }
    private void checkSelectedWord() {
        if (selectedCells.size() < 2) {
            selectedCells.clear();
            invalidate();
            return;
        }

        String selectedWord = getSelectedWord();

        // Check if the selected word matches any word to find (forward direction)
        for (String word : wordsToFind) {
            if (word.equalsIgnoreCase(selectedWord) && !foundWords.contains(word)) {
                // Word found!
                foundWords.add(word);
                foundWordCells.add(new ArrayList<>(selectedCells));

                // Clear selection
                selectedCells.clear();
                invalidate();
                return;
            }
        }

        // Check if the reversed word matches any word to find
        String reversedWord = new StringBuilder(selectedWord).reverse().toString();
        for (String word : wordsToFind) {
            if (word.equalsIgnoreCase(reversedWord) && !foundWords.contains(word)) {
                // Word found in reverse direction!
                foundWords.add(word);

                // Reverse the cells for highlighting
                ArrayList<int[]> reversedCells = new ArrayList<>();
                for (int i = selectedCells.size() - 1; i >= 0; i--) {
                    reversedCells.add(selectedCells.get(i));
                }
                foundWordCells.add(reversedCells);

                // Clear selection
                selectedCells.clear();
                invalidate();
                return;
            }
        }



        // Word not found - clear selection
        selectedCells.clear();
        invalidate();
    }
    public boolean isPuzzleComplete() {
        return foundWords.size() == wordsToFind.size();
    }

    // Helper class to store word positions
    private class WordPosition {
        String word;
        int startRow;
        int startCol;
        int direction;

        WordPosition(String word, int startRow, int startCol, int direction) {
            this.word = word;
            this.startRow = startRow;
            this.startCol = startCol;
            this.direction = direction;
        }
    }
}