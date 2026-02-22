package com.example.gamezone;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

public class chess_game extends AppCompatActivity {

    // Game constants


    private static final int BOARD_SIZE = 8;
    private static final int WHITE = 1;
    private static final int BLACK = 2;
    private static final long TIMER_DURATION = 600000; // 10 minutes
    private static final long TIMER_INTERVAL = 1000;

    // Game state
    private ImageView[][] boardViews = new ImageView[BOARD_SIZE][BOARD_SIZE];
    private FrameLayout[][] cellContainers = new FrameLayout[BOARD_SIZE][BOARD_SIZE];
    private Piece[][] pieces = new Piece[BOARD_SIZE][BOARD_SIZE];
    private int currentPlayer = WHITE;
    private int selectedRow = -1, selectedCol = -1;
    private boolean isComputerMode = false;
    private String difficultyLevel = "Medium";
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private int player1Score = 0;
    private int player2Score = 0;
    private int moveCount = 0;
    private boolean gameActive = true;
    private List<int[]> possibleMoves = new ArrayList<>();
    private List<View> highlightViews = new ArrayList<>(); // Was List<ImageView>
    // Themes
    private int currentBoardTheme = 0;
    private int currentPieceTheme = 0; // 0: Original, 1: Red, 2: Blue, 3: Green, 4: Purple, 5: Gold, 6: Silver
    private int[][] boardColors = {
            // 1. Classic Brown (Original - perfect for black/white pieces)
            {Color.parseColor("#F0D9B5"), Color.parseColor("#B58863")},

            // 2. Gray Theme (modern look)
            {Color.parseColor("#E8E8E8"), Color.parseColor("#A0A0A0")},

            // 3. Blue Theme (soft contrast)
            {Color.parseColor("#B8D4E3"), Color.parseColor("#7092BE")},

            // 4. Green Theme (natural look)
            {Color.parseColor("#D5E8D4"), Color.parseColor("#82B366")},

            // 5. Purple Theme (elegant)
            {Color.parseColor("#E1D5E7"), Color.parseColor("#9673A6")},

            // 6. Sand Theme (warm)
            {Color.parseColor("#F5E8D0"), Color.parseColor("#D7B899")},

            // 7. Ocean Theme (cool)
            {Color.parseColor("#D4F1F9"), Color.parseColor("#86C5DA")}
    };
    // Piece themes - colors for both white and black pieces


    // UI components
    private GridLayout chessBoard;
    private TextView player1TimeText, player2TimeText;
    private TextView player1ScoreText, player2ScoreText;
    private TextView turnIndicator, moveCountText;
    private TextView player1NameText, player2NameText;
    private LinearLayout player1Captured, player2Captured;
    private CardView gameOverDialog;
    private AppCompatButton btnNewGame, btnMenu, btnBoardTheme;
    private FrameLayout moveHighlight;

    // Timers
    private CountDownTimer player1Timer, player2Timer;
    private long player1TimeLeft = TIMER_DURATION;
    private long player2TimeLeft = TIMER_DURATION;
    private boolean whiteTimerRunning = false;
    private boolean blackTimerRunning = false;

    // History
    private Stack<GameState> gameHistory = new Stack<>();
    private Stack<Piece> capturedPiecesWhite = new Stack<>();
    private Stack<Piece> capturedPiecesBlack = new Stack<>();

    // Piece values
    private Map<String, Integer> pieceValues = new HashMap<String, Integer>() {{
        put("PAWN", 1);
        put("KNIGHT", 3);
        put("BISHOP", 3);
        put("ROOK", 5);
        put("QUEEN", 9);
        put("KING", 1000);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_game);


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        initializeViews();
        loadGameSettings();
        setupChessBoard();
        initializeGame();
        setupButtonListeners();
        startTimers();


        chessBoard.post(() -> {
            new Handler().postDelayed(() -> {
                resizeAllPieces();
            }, 100);
        });
    }

    private void initializeViews() {
        chessBoard = findViewById(R.id.chessBoard);
        player1TimeText = findViewById(R.id.player1Time);
        player2TimeText = findViewById(R.id.player2Time);
        player1ScoreText = findViewById(R.id.player1Score);
        player2ScoreText = findViewById(R.id.player2Score);
        turnIndicator = findViewById(R.id.turnIndicator);
        moveCountText = findViewById(R.id.moveCount);
        player1NameText = findViewById(R.id.player1Name);
        player2NameText = findViewById(R.id.player2Name);

        // Find the new captured containers
        player1Captured = findViewById(R.id.player1CapturedRow1); // Changed to row1
        player2Captured = findViewById(R.id.player2CapturedRow1); // Changed to row1

        gameOverDialog = findViewById(R.id.gameOverDialog);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnMenu = findViewById(R.id.btnMenu);
        btnBoardTheme = findViewById(R.id.btnBoardTheme);
        moveHighlight = findViewById(R.id.moveHighlight);
    }

    private void loadGameSettings() {
        Intent intent = getIntent();
        if (intent != null) {
            player1Name = intent.getStringExtra("player1Name") != null ?
                    intent.getStringExtra("player1Name") : "Player 1";
            player2Name = intent.getStringExtra("player2Name") != null ?
                    intent.getStringExtra("player2Name") : "Player 2";
            isComputerMode = intent.getBooleanExtra("isComputerMode", false);
            difficultyLevel = intent.getStringExtra("difficultyLevel") != null ?
                    intent.getStringExtra("difficultyLevel") : "Medium";

            player1NameText.setText(player1Name);
            player2NameText.setText(isComputerMode ? "Computer (" + difficultyLevel + ")" : player2Name);
        }
    }

    private void setupChessBoard() {
        chessBoard.removeAllViews();
        chessBoard.setColumnCount(BOARD_SIZE);
        chessBoard.setRowCount(BOARD_SIZE);

        int cellSize = Math.min(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels - 300) / BOARD_SIZE;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Create cell container
                FrameLayout cellContainer = new FrameLayout(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                cellContainer.setLayoutParams(params);

                // Create background
                ImageView square = new ImageView(this);
                square.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));
                square.setBackgroundColor(getSquareColor(row, col));

                // Create piece view
                ImageView pieceView = new ImageView(this);
                pieceView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));
                pieceView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                pieceView.setPadding(5, 5, 5, 5);

                // Add views
                cellContainer.addView(square);
                cellContainer.addView(pieceView);

                // Store references
                cellContainers[row][col] = cellContainer;
                boardViews[row][col] = pieceView;

                // Click listener
                final int finalRow = row;
                final int finalCol = col;
                cellContainer.setOnClickListener(v -> onSquareClicked(finalRow, finalCol));

                chessBoard.addView(cellContainer);
            }
        }
    }

    private int getSquareColor(int row, int col) {
        return (row + col) % 2 == 0 ?
                boardColors[currentBoardTheme][0] :
                boardColors[currentBoardTheme][1];
    }

    private void updateBoardTheme() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ImageView square = (ImageView) cellContainers[row][col].getChildAt(0);
                square.setBackgroundColor(getSquareColor(row, col));
            }
        }
    }

    private void initializeGame() {
        // Clear board
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                pieces[i][j] = null;
                boardViews[i][j].setImageResource(0);
                boardViews[i][j].setTag(null);
            }
        }

        // Setup pieces
        setupInitialPieces();

        // Reset scores
        player1Score = 0;
        player2Score = 0;
        moveCount = 0;
        currentPlayer = WHITE;

        // Clear captured pieces from ALL rows
        clearCapturedPieces();

        // Clear other data
        gameHistory.clear();
        capturedPiecesWhite.clear();
        capturedPiecesBlack.clear();
        possibleMoves.clear();
        clearHighlights();

        // Update UI
        updateScores();
        updateTurnIndicator();
        updateMoveCount();
        gameActive = true;
    }

    private void clearCapturedPieces() {
        // Clear all captured piece rows
        LinearLayout[] rows = {
                findViewById(R.id.player1CapturedRow1),
                findViewById(R.id.player1CapturedRow2),
                findViewById(R.id.player2CapturedRow1),
                findViewById(R.id.player2CapturedRow2)
        };

        for (LinearLayout row : rows) {
            if (row != null) {
                row.removeAllViews();
                if (row.getId() == R.id.player1CapturedRow2 || row.getId() == R.id.player2CapturedRow2) {
                    row.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setupInitialPieces() {
        // Black pieces
        pieces[0][0] = new Piece("ROOK", BLACK, 0, 0);
        pieces[0][1] = new Piece("KNIGHT", BLACK, 0, 1);
        pieces[0][2] = new Piece("BISHOP", BLACK, 0, 2);
        pieces[0][3] = new Piece("QUEEN", BLACK, 0, 3);
        pieces[0][4] = new Piece("KING", BLACK, 0, 4);
        pieces[0][5] = new Piece("BISHOP", BLACK, 0, 5);
        pieces[0][6] = new Piece("KNIGHT", BLACK, 0, 6);
        pieces[0][7] = new Piece("ROOK", BLACK, 0, 7);

        for (int i = 0; i < BOARD_SIZE; i++) {
            pieces[1][i] = new Piece("PAWN", BLACK, 1, i);
        }

        // White pieces
        pieces[7][0] = new Piece("ROOK", WHITE, 7, 0);
        pieces[7][1] = new Piece("KNIGHT", WHITE, 7, 1);
        pieces[7][2] = new Piece("BISHOP", WHITE, 7, 2);
        pieces[7][3] = new Piece("QUEEN", WHITE, 7, 3);
        pieces[7][4] = new Piece("KING", WHITE, 7, 4);
        pieces[7][5] = new Piece("BISHOP", WHITE, 7, 5);
        pieces[7][6] = new Piece("KNIGHT", WHITE, 7, 6);
        pieces[7][7] = new Piece("ROOK", WHITE, 7, 7);

        for (int i = 0; i < BOARD_SIZE; i++) {
            pieces[6][i] = new Piece("PAWN", WHITE, 6, i);
        }

        updateBoardDisplay();
    }

    private void updateBoardDisplay() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = pieces[i][j];
                if (piece != null) {
                    int drawableId = getPieceDrawableId(piece.type, piece.color);
                    boardViews[i][j].setImageResource(drawableId);



                    boardViews[i][j].setTag(piece);
                    boardViews[i][j].setElevation(8f);
                } else {
                    boardViews[i][j].setImageResource(0);
                    boardViews[i][j].setColorFilter(null);
                    boardViews[i][j].setTag(null);
                }
            }
        }

        // ======== ADD THIS ========
        // Resize pieces after layout is ready
        new Handler().postDelayed(this::resizeAllPieces, 50);
    }

    private void resizeAllPieces() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                ImageView pieceView = boardViews[i][j];
                FrameLayout cell = cellContainers[i][j];

                // Check if views are ready
                if (pieceView != null && cell != null && cell.getWidth() > 0) {
                    Piece piece = pieces[i][j];
                    if (piece != null) {
                        fixPieceSize(pieceView, cell, piece.type);
                    }
                }
            }
        }
    }

    private void fixPieceSize(ImageView pieceView, FrameLayout cell, String pieceType) {
        // Get cell size
        int cellSize = cell.getWidth();
        if (cellSize <= 0) return; // Cell not measured yet

        int pieceSize;

        switch (pieceType) {
            case "BISHOP":
                pieceSize = (int) (cellSize * 0.8); // 80% for knight/bishop
                break;
            case "KING":
            case "QUEEN":
                pieceSize = (int) (cellSize * 0.92); // 85% for king/queen
                break;
            case "ROOK":
                pieceSize = (int) (cellSize * 0.73); // 75% for rook
                break;
            case "PAWN":
                pieceSize = (int) (cellSize * 0.75); // 70% for pawn
                break;
            case "KNIGHT":
                pieceSize = (int) (cellSize * 0.75); // 70% for pawn
                break;
            default:
                pieceSize = (int) (cellSize * 0.75); // default 75%
        }

        // Update layout params
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pieceSize, pieceSize);
        params.gravity = android.view.Gravity.CENTER;
        pieceView.setLayoutParams(params);

        // Set scale type
        pieceView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Force redraw
        pieceView.requestLayout();
        pieceView.invalidate();
    }

    private int getPieceDrawableId(String type, int color) {
        switch (type) {
            case "PAWN":
                return (color == WHITE) ? R.drawable.vector_chess_pawn : R.drawable.vector_chess_pawn_op;
            case "KNIGHT":
                return (color == WHITE) ? R.drawable.vector_chess_knight : R.drawable.vector_chess_knight_op;
            case "BISHOP":
                return (color == WHITE) ? R.drawable.vector_chess_bishop : R.drawable.vector_chess_bishop_op;
            case "ROOK":
                return (color == WHITE) ? R.drawable.vector_chess_rook : R.drawable.vector_chess_rook_op;
            case "QUEEN":
                return (color == WHITE) ? R.drawable.vector_chess_queen : R.drawable.vector_chess_queen_op;
            case "KING":
                return (color == WHITE) ? R.drawable.vector_chess_king : R.drawable.vector_chess_king_op;
            default:
                return 0;
        }
    }

    private void onSquareClicked(int row, int col) {
        if (!gameActive) return;

        // Check if computer's turn
        if (isComputerMode && currentPlayer == BLACK) {
            Toast.makeText(this, "Computer's turn!", Toast.LENGTH_SHORT).show();
            return;
        }

        // If piece selected, try to move
        if (selectedRow != -1 && selectedCol != -1) {
            for (int[] move : possibleMoves) {
                if (move[0] == selectedRow && move[1] == selectedCol &&
                        move[2] == row && move[3] == col) {
                    movePiece(selectedRow, selectedCol, row, col);
                    clearSelection();
                    return;
                }
            }
        }

        // Select piece
        Piece piece = pieces[row][col];
        if (piece != null && piece.color == currentPlayer) {
            if (selectedRow == row && selectedCol == col) {
                clearSelection();
            } else {
                selectPiece(row, col);
            }
        } else {
            clearSelection();
        }
    }

    private void selectPiece(int row, int col) {
        clearSelection();
        selectedRow = row;
        selectedCol = col;

        // Get the cell size
        FrameLayout cell = cellContainers[row][col];
        int cellSize = cell.getWidth();

        // Create a custom selection view that fits the cell
        View selectionView = new View(this);
        selectionView.setBackgroundResource(R.drawable.selected_square);

        // Make selection view fill the entire cell
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                cellSize,  // Match cell width
                cellSize   // Match cell height
        );
        selectionView.setLayoutParams(params);

        // Add to cell container
        cell.addView(selectionView);

        // Store reference to remove later
        selectionView.setTag("selection");

        // Show possible moves
        showPossibleMoves(row, col);
    }

    private void clearSelection() {
        if (selectedRow != -1 && selectedCol != -1) {
            FrameLayout cell = cellContainers[selectedRow][selectedCol];
            if (cell != null) {
                // Remove selection view by tag
                View selectionView = cell.findViewWithTag("selection");
                if (selectionView != null) {
                    cell.removeView(selectionView);
                }
            }
        }
        selectedRow = -1;
        selectedCol = -1;
        clearHighlights();
    }

    private void showPossibleMoves(int row, int col) {
        possibleMoves.clear();
        clearHighlights();

        Piece piece = pieces[row][col];
        if (piece == null) return;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isValidMove(row, col, i, j)) {
                    possibleMoves.add(new int[]{row, col, i, j});
                    addMoveHighlight(i, j, pieces[i][j] != null);
                }
            }
        }
    }

    private void addMoveHighlight(int row, int col, boolean isCapture) {
        View highlight = new View(this); // Change from ImageView to View

        // Remove: highlight.setImageResource(...)
        // Instead set background:
        highlight.setBackgroundResource(isCapture ?
                R.drawable.possible_capture_circle :
                R.drawable.possible_move_circle);

        // Make it fill the entire cell
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        highlight.setLayoutParams(params);

        cellContainers[row][col].addView(highlight);
        highlightViews.add(highlight);
    }
    private void clearHighlights() {
        for (View highlight : highlightViews) {
            View parent = (View) highlight.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(highlight);
            }
        }
        highlightViews.clear();
        possibleMoves.clear();
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = pieces[fromRow][fromCol];
        if (piece == null) return false;

        Piece destPiece = pieces[toRow][toCol];
        if (destPiece != null && destPiece.color == piece.color) {
            return false;
        }

        switch (piece.type) {
            case "PAWN":
                return isValidPawnMove(fromRow, fromCol, toRow, toCol);
            case "KNIGHT":
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case "BISHOP":
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case "ROOK":
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case "QUEEN":
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case "KING":
                return isValidKingMove(fromRow, fromCol, toRow, toCol);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = pieces[fromRow][fromCol];
        Piece destPiece = pieces[toRow][toCol];
        int direction = (piece.color == WHITE) ? -1 : 1;

        // Forward move
        if (fromCol == toCol && destPiece == null) {
            if (toRow == fromRow + direction) return true;
            if (!piece.hasMoved && toRow == fromRow + 2 * direction &&
                    pieces[fromRow + direction][fromCol] == null) {
                return true;
            }
        }

        // Capture
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            if (destPiece != null && destPiece.color != piece.color) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;

        int rowStep = (toRow > fromRow) ? 1 : -1;
        int colStep = (toCol > fromCol) ? 1 : -1;
        int row = fromRow + rowStep;
        int col = fromCol + colStep;

        while (row != toRow && col != toCol) {
            if (pieces[row][col] != null) return false;
            row += rowStep;
            col += colStep;
        }

        return true;
    }

    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false;

        if (fromRow == toRow) {
            int step = (toCol > fromCol) ? 1 : -1;
            for (int col = fromCol + step; col != toCol; col += step) {
                if (pieces[fromRow][col] != null) return false;
            }
        } else {
            int step = (toRow > fromRow) ? 1 : -1;
            for (int row = fromRow + step; row != toRow; row += step) {
                if (pieces[row][fromCol] != null) return false;
            }
        }

        return true;
    }

    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return isValidBishopMove(fromRow, fromCol, toRow, toCol) ||
                isValidRookMove(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return rowDiff <= 1 && colDiff <= 1;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = pieces[fromRow][fromCol];

        // 1. Check if piece is pinned (cannot move because it exposes king to check)
        if (isPiecePinned(piece, fromRow, fromCol, toRow, toCol)) {
            Toast.makeText(this, "Cannot move: King would be in danger!", Toast.LENGTH_SHORT).show();
            clearSelection();
            return;
        }

        // 2. Check if move leaves king in check
        if (moveLeavesKingInCheck(piece, fromRow, fromCol, toRow, toCol)) {
            Toast.makeText(this, "Invalid move: King would be in check!", Toast.LENGTH_SHORT).show();
            clearSelection();
            return;
        }

        // Save game state
        saveGameState();

        // Decrease player time
        if (currentPlayer == WHITE) {
            player1TimeLeft = Math.max(0, player1TimeLeft - 1000);
        } else {
            player2TimeLeft = Math.max(0, player2TimeLeft - 1000);
        }
        updateTimeDisplay();

        Piece capturedPiece = pieces[toRow][toCol];

        // Handle capture
        if (capturedPiece != null) {
            showCaptureAnimation(toRow, toCol, capturedPiece);
            handleCapture(capturedPiece);
        }

        // Show simple move animation
        showSimpleMoveAnimation(fromRow, fromCol, toRow, toCol, piece);

        // Move piece
        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;

        // Update piece position
        piece.row = toRow;
        piece.col = toCol;
        piece.hasMoved = true;

        // Check for pawn promotion
        checkPawnPromotion(toRow, toCol, piece);

        // Update display
        updateBoardDisplay();

        // Increment move count
        moveCount++;

        // Switch player
        switchPlayerAndTimers();

        // Update UI
        updateTurnIndicator();
        updateMoveCount();

        // Check for check/checkmate/stalemate
        checkGameStatus();

        // Computer move
        if (isComputerMode && currentPlayer == BLACK && gameActive) {
            new Handler().postDelayed(this::makeComputerMove, 800);
        }
    }

    private boolean isPiecePinned(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        // If not king's piece, check if moving it exposes king
        if (!piece.type.equals("KING")) {
            // Test move
            Piece tempDest = pieces[toRow][toCol];
            pieces[toRow][toCol] = piece;
            pieces[fromRow][fromCol] = null;
            int tempRow = piece.row;
            int tempCol = piece.col;
            piece.row = toRow;
            piece.col = toCol;

            // Check if king is in check after move
            boolean kingInCheck = isKingInCheck(piece.color);

            // Undo move
            pieces[fromRow][fromCol] = piece;
            pieces[toRow][toCol] = tempDest;
            piece.row = tempRow;
            piece.col = tempCol;

            return kingInCheck;
        }
        return false;
    }

    private boolean moveLeavesKingInCheck(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        // Test the move
        Piece tempDest = pieces[toRow][toCol];
        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;
        int tempRow = piece.row;
        int tempCol = piece.col;
        piece.row = toRow;
        piece.col = toCol;

        // Check if king is in check after move
        boolean kingInCheck = isKingInCheck(piece.color);

        // Undo move
        pieces[fromRow][fromCol] = piece;
        pieces[toRow][toCol] = tempDest;
        piece.row = tempRow;
        piece.col = tempCol;

        return kingInCheck;
    }

    private void showCaptureAnimation(int row, int col, Piece capturedPiece) {
        // Create a copy of the captured piece for animation
        ImageView capturedAnim = new ImageView(this);
        int drawableId = getPieceDrawableId(capturedPiece.type, capturedPiece.color);
        capturedAnim.setImageResource(drawableId);



        // Set layout params
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.CENTER;
        capturedAnim.setLayoutParams(params);

        // Add to the cell container
        cellContainers[row][col].addView(capturedAnim);

        // Create scale and fade animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(capturedAnim, "scaleX", 1f, 1.5f, 0.5f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(capturedAnim, "scaleY", 1f, 1.5f, 0.5f, 0f);
        ObjectAnimator fade = ObjectAnimator.ofFloat(capturedAnim, "alpha", 1f, 0.5f, 0f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(capturedAnim, "rotation", 0f, 45f, 90f, 180f);

        // Set duration
        scaleX.setDuration(800);
        scaleY.setDuration(800);
        fade.setDuration(800);
        rotate.setDuration(800);

        // Start animations together
        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, fade, rotate);
        animatorSet.start();

        // Remove view after animation
        new Handler().postDelayed(() -> {
            if (cellContainers[row][col].indexOfChild(capturedAnim) != -1) {
                cellContainers[row][col].removeView(capturedAnim);
            }
        }, 800);
    }

    private void makeComputerMove() {
        if (!gameActive || currentPlayer != BLACK) return;

        clearSelection();

        // Check if computer is in check
        boolean inCheck = isKingInCheck(BLACK);

        // Get only legal moves that don't leave king in check
        List<int[]> legalMoves = new ArrayList<>();
        for (int[] move : getAllPossibleMoves(BLACK)) {
            if (!moveLeavesKingInCheck(pieces[move[0]][move[1]], move[0], move[1], move[2], move[3])) {
                legalMoves.add(move);
            }
        }

        // If no legal moves available
        if (legalMoves.isEmpty()) {
            if (inCheck) {
                gameOverCheckmate(); // Computer lost
            } else {
                gameOverDraw("Stalemate!");
            }
            return;
        }

        int[] move = getComputerMove(legalMoves, inCheck);
        if (move != null) {
            executeComputerMove(move[0], move[1], move[2], move[3]);
        }
    }

    private void executeComputerMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Save game state
        saveGameState();

        // DECREASE COMPUTER'S TIME BY 1 SECOND
        if (player2TimeLeft >= 1000) {
            player2TimeLeft -= 1000; // Decrease by 1 second
        } else {
            player2TimeLeft = 0;
        }
        updateTimeDisplay(); // Update the display immediately

        Piece piece = pieces[fromRow][fromCol];
        Piece capturedPiece = pieces[toRow][toCol];

        // Handle capture with animation
        if (capturedPiece != null) {
            showCaptureAnimation(toRow, toCol, capturedPiece);
            handleCapture(capturedPiece);
        }

        // ======== ADD MOVE ANIMATION HERE ========
        // Show move animation for regular moves (non-captures)
        if (capturedPiece == null) {
            showMoveAnimation(fromRow, fromCol, toRow, toCol, piece);
        }
        // =========================================

        // Move piece
        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;

        // Update piece position
        piece.row = toRow;
        piece.col = toCol;
        piece.hasMoved = true;

        // Check for pawn promotion
        checkPawnPromotion(toRow, toCol, piece);

        // Update display
        updateBoardDisplay();

        // Increment move count
        moveCount++;

        // Switch player and timers
        switchPlayerAndTimers();

        // Update UI
        updateTurnIndicator();
        updateMoveCount();

        // Check for check/checkmate
        checkGameStatus();
    }

    private void showMoveAnimation(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        ImageView movingPiece = new ImageView(this);
        int drawableId = getPieceDrawableId(piece.type, piece.color);
        movingPiece.setImageResource(drawableId);



        FrameLayout startCell = cellContainers[fromRow][fromCol];
        int cellSize = startCell.getWidth();
        int pieceSize = (int) (cellSize * 0.7);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pieceSize, pieceSize);
        params.gravity = android.view.Gravity.CENTER;
        movingPiece.setLayoutParams(params);
        movingPiece.setScaleType(ImageView.ScaleType.FIT_CENTER);

        FrameLayout chessBoardContainer = findViewById(R.id.chessBoardContainer);
        chessBoardContainer.addView(movingPiece);

        int[] startLocation = new int[2];
        int[] endLocation = new int[2];
        startCell.getLocationOnScreen(startLocation);
        cellContainers[toRow][toCol].getLocationOnScreen(endLocation);

        int[] containerLocation = new int[2];
        chessBoardContainer.getLocationOnScreen(containerLocation);

        float startX = startLocation[0] - containerLocation[0];
        float startY = startLocation[1] - containerLocation[1];
        float endX = endLocation[0] - containerLocation[0];
        float endY = endLocation[1] - containerLocation[1];

        movingPiece.setX(startX);
        movingPiece.setY(startY);

        // ======== INCREASE DURATION FOR SLOWER MOVEMENT ========
        int moveDuration = 800; // Changed from 500 to 800 (slower)
        int scaleDuration = 800; // Changed from 500 to 800

        ObjectAnimator moveX = ObjectAnimator.ofFloat(movingPiece, "x", startX, endX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(movingPiece, "y", startY, endY);
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(movingPiece, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(movingPiece, "scaleY", 1f, 1.2f, 1f);

        moveX.setDuration(moveDuration);
        moveY.setDuration(moveDuration);
        scaleUp.setDuration(scaleDuration);
        scaleUpY.setDuration(scaleDuration);

        android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();
        animatorSet.playTogether(moveX, moveY, scaleUp, scaleUpY);
        animatorSet.start();

        new Handler().postDelayed(() -> {
            if (chessBoardContainer.indexOfChild(movingPiece) != -1) {
                chessBoardContainer.removeView(movingPiece);
            }
        }, moveDuration); // Match the delay with duration
    }

    private int[] getComputerMove(List<int[]> legalMoves, boolean inCheck) {
        if (legalMoves.isEmpty()) return null;

        Random random = new Random();

        // PRIORITY 1: If in check, prioritize king safety
        if (inCheck) {
            List<int[]> kingMoves = new ArrayList<>();
            List<int[]> captureAttackerMoves = new ArrayList<>();
            List<int[]> blockMoves = new ArrayList<>();

            // Find the attacking piece(s)
            List<int[]> attackers = new ArrayList<>();
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    Piece piece = pieces[i][j];
                    if (piece != null && piece.color == WHITE) {
                        int kingRow = -1, kingCol = -1;
                        for (int x = 0; x < BOARD_SIZE; x++) {
                            for (int y = 0; y < BOARD_SIZE; y++) {
                                Piece p = pieces[x][y];
                                if (p != null && p.type.equals("KING") && p.color == BLACK) {
                                    kingRow = x; kingCol = y; break;
                                }
                            }
                        }
                        if (isValidMove(i, j, kingRow, kingCol)) {
                            attackers.add(new int[]{i, j});
                        }
                    }
                }
            }

            // Categorize legal moves
            for (int[] move : legalMoves) {
                Piece movingPiece = pieces[move[0]][move[1]];

                // Move 1: Move king to safety
                if (movingPiece.type.equals("KING")) {
                    kingMoves.add(move);
                }

                // Move 2: Capture the attacking piece
                Piece target = pieces[move[2]][move[3]];
                if (target != null && target.color == WHITE) {
                    for (int[] attacker : attackers) {
                        if (move[2] == attacker[0] && move[3] == attacker[1]) {
                            captureAttackerMoves.add(move);
                            break;
                        }
                    }
                }

                // Move 3: Block the attack (for sliding pieces)
                if (!movingPiece.type.equals("KING") && attackers.size() == 1) {
                    int[] attacker = attackers.get(0);
                    int kingRow = -1, kingCol = -1;
                    for (int x = 0; x < BOARD_SIZE; x++) {
                        for (int y = 0; y < BOARD_SIZE; y++) {
                            Piece p = pieces[x][y];
                            if (p != null && p.type.equals("KING") && p.color == BLACK) {
                                kingRow = x; kingCol = y; break;
                            }
                        }
                    }

                    if (kingRow != -1) {
                        // Check if this is a sliding piece (queen, rook, bishop)
                        Piece attackerPiece = pieces[attacker[0]][attacker[1]];
                        if (attackerPiece != null &&
                                (attackerPiece.type.equals("QUEEN") ||
                                        attackerPiece.type.equals("ROOK") ||
                                        attackerPiece.type.equals("BISHOP"))) {

                            // Check if move lands between attacker and king
                            if (move[2] >= Math.min(attacker[0], kingRow) && move[2] <= Math.max(attacker[0], kingRow) &&
                                    move[3] >= Math.min(attacker[1], kingCol) && move[3] <= Math.max(attacker[1], kingCol)) {
                                blockMoves.add(move);
                            }
                        }
                    }
                }
            }

            // Priority order: King moves > Capture attacker > Block > Any legal move
            if (!kingMoves.isEmpty()) {
                return kingMoves.get(random.nextInt(kingMoves.size()));
            }
            if (!captureAttackerMoves.isEmpty()) {
                return captureAttackerMoves.get(random.nextInt(captureAttackerMoves.size()));
            }
            if (!blockMoves.isEmpty()) {
                return blockMoves.get(random.nextInt(blockMoves.size()));
            }
        }

        // If not in check, use existing difficulty logic with legal moves only
        switch (difficultyLevel) {
            case "Easy":
                return legalMoves.get(random.nextInt(legalMoves.size()));

            case "Medium":
                List<int[]> captureMoves = new ArrayList<>();
                for (int[] move : legalMoves) {
                    if (pieces[move[2]][move[3]] != null) {
                        captureMoves.add(move);
                    }
                }
                if (!captureMoves.isEmpty()) {
                    return captureMoves.get(random.nextInt(captureMoves.size()));
                }
                return legalMoves.get(random.nextInt(legalMoves.size()));

            case "Hard":
                int bestValue = -1;
                List<int[]> bestMoves = new ArrayList<>();

                for (int[] move : legalMoves) {
                    Piece target = pieces[move[2]][move[3]];
                    if (target != null) {
                        int value = pieceValues.get(target.type);
                        if (value > bestValue) {
                            bestValue = value;
                            bestMoves.clear();
                            bestMoves.add(move);
                        } else if (value == bestValue) {
                            bestMoves.add(move);
                        }
                    }
                }

                if (!bestMoves.isEmpty()) {
                    return bestMoves.get(random.nextInt(bestMoves.size()));
                }
                return legalMoves.get(random.nextInt(legalMoves.size()));

            default:
                return legalMoves.get(random.nextInt(legalMoves.size()));
        }
    }

    private void handleCapture(Piece capturedPiece) {
        int value = pieceValues.get(capturedPiece.type);

        if (currentPlayer == WHITE) {
            player1Score += value;
            capturedPiecesWhite.push(capturedPiece);
            addCapturedPieceToDisplay(capturedPiece, player1Captured);
        } else {
            player2Score += value;
            capturedPiecesBlack.push(capturedPiece);
            addCapturedPieceToDisplay(capturedPiece, player2Captured);
        }

        updateScores();
    }

    private void addCapturedPieceToDisplay(Piece piece, LinearLayout container) {
        runOnUiThread(() -> {
            try {
                // Get the appropriate rows based on which player's container
                LinearLayout row1, row2;

                // Check which player captured the piece
                if (piece.color == BLACK) { // White captured black piece
                    row1 = findViewById(R.id.player1CapturedRow1);
                    row2 = findViewById(R.id.player1CapturedRow2);
                } else { // Black captured white piece
                    row1 = findViewById(R.id.player2CapturedRow1);
                    row2 = findViewById(R.id.player2CapturedRow2);
                }

                // Check if rows exist
                if (row1 == null || row2 == null) {
                    Log.e("ChessGame", "Captured rows not found in layout!");
                    return;
                }

                ImageView capturedView = new ImageView(chess_game.this);

                // Smaller size for captured pieces
                int size = (int) (31 * getResources().getDisplayMetrics().density);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(4, 4, 4, 4);
                capturedView.setLayoutParams(params);

                int drawableId = getPieceDrawableId(piece.type, piece.color);
                capturedView.setImageResource(drawableId);

                capturedView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                // Add to row1 if less than 8 pieces, otherwise add to row2
                if (row1.getChildCount() < 8) {
                    row1.addView(capturedView);
                } else {
                    // Show row2 if not already visible
                    if (row2.getVisibility() != View.VISIBLE) {
                        row2.setVisibility(View.VISIBLE);
                    }
                    row2.addView(capturedView);
                }

                // Add fade animation
                Animation animation = AnimationUtils.loadAnimation(chess_game.this, R.anim.fade_in);
                capturedView.startAnimation(animation);

            } catch (Exception e) {
                Log.e("ChessGame", "Error adding captured piece: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void checkPawnPromotion(int row, int col, Piece piece) {
        if (piece.type.equals("PAWN") &&
                ((piece.color == WHITE && row == 0) ||
                        (piece.color == BLACK && row == BOARD_SIZE - 1))) {
            piece.type = "QUEEN";
            Toast.makeText(this, "Pawn promoted to Queen!", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchPlayerAndTimers() {
        currentPlayer = (currentPlayer == WHITE) ? BLACK : WHITE;

        if (currentPlayer == WHITE) {
            if (blackTimerRunning) {
                stopTimer(player2Timer);
                blackTimerRunning = false;
            }
            if (!whiteTimerRunning) {
                startPlayer1Timer();
                whiteTimerRunning = true;
            }
        } else {
            if (whiteTimerRunning) {
                stopTimer(player1Timer);
                whiteTimerRunning = false;
            }
            if (!blackTimerRunning) {
                startPlayer2Timer();
                blackTimerRunning = true;
            }
        }
    }

    private void checkGameStatus() {
        // Check for check
        if (isKingInCheck(currentPlayer)) {
            Toast.makeText(this, "Check!", Toast.LENGTH_SHORT).show();

            // Check if king has any legal moves
            if (!kingHasLegalMoves(currentPlayer)) {
                // No legal moves for king - checkmate or stalemate
                if (isKingInCheck(currentPlayer)) {
                    // Checkmate - don't kill king, just end game
                    gameOverCheckmate();
                } else {
                    // Stalemate
                    gameOverDraw("Stalemate! No legal moves.");
                }
                return;
            }
        }

        // Check for stalemate (no legal moves but not in check)
        if (isStalemate(currentPlayer)) {
            gameOverDraw("Stalemate! No legal moves.");
        }
    }

    private boolean kingHasLegalMoves(int playerColor) {
        // Find king
        int kingRow = -1, kingCol = -1;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = pieces[i][j];
                if (piece != null && piece.type.equals("KING") && piece.color == playerColor) {
                    kingRow = i;
                    kingCol = j;
                    break;
                }
            }
        }

        if (kingRow == -1) return false;

        // Check all 8 possible king moves
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] dir : directions) {
            int newRow = kingRow + dir[0];
            int newCol = kingCol + dir[1];

            if (newRow >= 0 && newRow < BOARD_SIZE && newCol >= 0 && newCol < BOARD_SIZE) {
                Piece target = pieces[newRow][newCol];

                // Check if square is empty or contains opponent piece
                if (target == null || target.color != playerColor) {
                    // Test if move is safe (doesn't move into check)
                    if (!moveLeavesKingInCheck(pieces[kingRow][kingCol], kingRow, kingCol, newRow, newCol)) {
                        return true; // King has at least one legal move
                    }
                }
            }
        }

        return false; // No legal moves for king
    }

    private void showSimpleMoveAnimation(int fromRow, int fromCol, int toRow, int toCol, Piece piece) {
        // Skip animation for fast gameplay
        if (isComputerMode) return;

        // Simple fade effect on destination cell
        ImageView destCell = boardViews[toRow][toCol];
        if (destCell != null) {
            ObjectAnimator fade = ObjectAnimator.ofFloat(destCell, "alpha", 0.3f, 1f);
            fade.setDuration(200);
            fade.start();
        }
    }

    private boolean isKingInCheck(int playerColor) {
        // Find king
        int kingRow = -1, kingCol = -1;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = pieces[i][j];
                if (piece != null && piece.type.equals("KING") && piece.color == playerColor) {
                    kingRow = i;
                    kingCol = j;
                    break;
                }
            }
        }

        if (kingRow == -1) return false;

        // Check if any opponent can attack king
        int opponent = playerColor == WHITE ? BLACK : WHITE;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = pieces[i][j];
                if (piece != null && piece.color == opponent) {
                    if (isValidMove(i, j, kingRow, kingCol)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isCheckmate(int playerColor) {
        // First, check if king is in check
        if (!isKingInCheck(playerColor)) {
            return false;
        }

        // Try all possible moves for all pieces
        for (int fromRow = 0; fromRow < BOARD_SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < BOARD_SIZE; fromCol++) {
                Piece piece = pieces[fromRow][fromCol];
                if (piece != null && piece.color == playerColor) {
                    // Check all possible destination squares
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                                // Make temporary move
                                Piece tempDest = pieces[toRow][toCol];
                                int tempFromRow = piece.row;
                                int tempFromCol = piece.col;
                                boolean tempHasMoved = piece.hasMoved;

                                // Move piece
                                pieces[toRow][toCol] = piece;
                                pieces[fromRow][fromCol] = null;
                                piece.row = toRow;
                                piece.col = toCol;

                                // Check if king is still in check
                                boolean stillInCheck = isKingInCheck(playerColor);

                                // Undo move
                                pieces[fromRow][fromCol] = piece;
                                pieces[toRow][toCol] = tempDest;
                                piece.row = tempFromRow;
                                piece.col = tempFromCol;
                                piece.hasMoved = tempHasMoved;

                                // If we found a move that gets out of check, not checkmate
                                if (!stillInCheck) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        // If no legal moves get out of check, it's checkmate
        return true;
    }

    private boolean isStalemate(int playerColor) {
        if (isKingInCheck(playerColor)) return false;

        // Check if any legal moves
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = pieces[i][j];
                if (piece != null && piece.color == playerColor) {
                    for (int x = 0; x < BOARD_SIZE; x++) {
                        for (int y = 0; y < BOARD_SIZE; y++) {
                            if (isValidMove(i, j, x, y)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private void gameOverCheckmate() {
        gameActive = false;
        stopAllTimers();

        // FIX: Computer loses when in checkmate (currentPlayer is BLACK when it's computer's turn)
        String winner = (currentPlayer == BLACK) ? player1Name : player2Name;
        showGameOverDialog("Checkmate! " + winner + " wins!");
    }




    private void gameOverDraw(String message) {
        gameActive = false;
        stopAllTimers();
        showGameOverDialog(message + " Game is drawn.");
    }
    private void saveGameState() {
        List<Piece> capWhite = new ArrayList<>(capturedPiecesWhite);
        List<Piece> capBlack = new ArrayList<>(capturedPiecesBlack);

        GameState state = new GameState(
                pieces,
                currentPlayer,
                player1Score,
                player2Score,
                player1TimeLeft,
                player2TimeLeft,
                capWhite,
                capBlack
        );

        gameHistory.push(state);
    }

    private void setupButtonListeners() {
        btnNewGame.setOnClickListener(v -> showNewGameDialog());
        btnMenu.setOnClickListener(v -> showMenuDialog());
        btnBoardTheme.setOnClickListener(v -> showBoardThemeDialog());

        // Game over dialog
        findViewById(R.id.btnPlayAgain).setOnClickListener(v -> {
            gameOverDialog.setVisibility(View.GONE);
            initializeGame();
            startTimers();
        });

        findViewById(R.id.btnMainMenu).setOnClickListener(v -> {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        });
    }




    private void showNewGameDialog() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.WHITE);
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title
        TextView title = new TextView(this);
        title.setText("New Game");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(Color.BLACK);

        // Message
        TextView message = new TextView(this);
        message.setText("Start a new game?");
        message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        message.setTextSize(16);
        message.setPadding(0, 10, 0, 0);
        message.setTextColor(Color.BLACK);

        // Add views to the layout
        dialogLayout.addView(title);
        dialogLayout.addView(message);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    initializeGame();
                    startTimers();
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

            Typeface customFont = ResourcesCompat.getFont(this, R.font.caudex);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(customFont, Typeface.BOLD);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(customFont, Typeface.BOLD);
        }
    }

    private void showMenuDialog() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.WHITE);
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title
        TextView title = new TextView(this);
        title.setText("Game Menu");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(Color.BLACK);

        // Message
        TextView message = new TextView(this);
        message.setText("Select an option:");
        message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        message.setTextSize(16);
        message.setPadding(0, 10, 0, 20);
        message.setTextColor(Color.BLACK);

        // Buttons container
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        buttonsLayout.setPadding(0, 10, 0, 0);

        String[] menuOptions = {"New Game", "Instructions", "Exit"};

        // DECLARE dialog variable here
        AlertDialog[] dialogHolder = new AlertDialog[1];

        for (int i = 0; i < menuOptions.length; i++) {
            Button btn = new Button(this);
            btn.setText(menuOptions[i]);
            btn.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
            btn.setTextSize(16);
            btn.setTextColor(Color.BLUE);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setPadding(0, 15, 20, 15);
            btn.setGravity(Gravity.START);

            int index = i;
            btn.setOnClickListener(v -> {
                switch (index) {
                    case 0: showNewGameDialog(); break;
                    case 1: showInstructionsDialog(); break;
                    case 2: finish(); break;
                }
                if (dialogHolder[0] != null) dialogHolder[0].dismiss();
            });

            buttonsLayout.addView(btn);
        }

        // Add views to the layout
        dialogLayout.addView(title);
        dialogLayout.addView(message);
        dialogLayout.addView(buttonsLayout);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setNegativeButton("Cancel", null)
                .create();

        dialogHolder[0] = dialog; // Store the dialog reference
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(
                    ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        }
    }
    private void showBoardThemeDialog() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.WHITE);
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title
        TextView title = new TextView(this);
        title.setText("Select Board Theme");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(Color.BLACK);

        // Message
        TextView message = new TextView(this);
        message.setText("Choose a board theme:");
        message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        message.setTextSize(16);
        message.setPadding(0, 10, 0, 20);
        message.setTextColor(Color.BLACK);

        // Themes container
        LinearLayout themesLayout = new LinearLayout(this);
        themesLayout.setOrientation(LinearLayout.VERTICAL);
        themesLayout.setPadding(0, 10, 0, 0);

        String[] themes = {
                "Classic Brown",
                "Modern Gray",
                "Soft Blue",
                "Natural Green",
                "Elegant Purple",
                "Warm Sand",
                "Cool Ocean"
        };

        // DECLARE dialog variable here
        AlertDialog[] dialogHolder = new AlertDialog[1];

        for (int i = 0; i < themes.length; i++) {
            Button btn = new Button(this);
            btn.setText(themes[i]);
            btn.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
            btn.setTextSize(16);
            btn.setTextColor(Color.BLUE);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setPadding(0, 15, 20, 10);
            btn.setGravity(Gravity.START);

            int index = i;
            btn.setOnClickListener(v -> {
                currentBoardTheme = index;
                updateBoardTheme();
                Toast.makeText(this, "Board Theme: " + themes[index], Toast.LENGTH_SHORT).show();
                if (dialogHolder[0] != null) dialogHolder[0].dismiss();
            });

            themesLayout.addView(btn);
        }

        // Add views to the layout
        dialogLayout.addView(title);
        dialogLayout.addView(message);
        dialogLayout.addView(themesLayout);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setNegativeButton("Cancel", null)
                .create();

        dialogHolder[0] = dialog; // Store the dialog reference
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(
                    ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        }
    }
    private void showInstructionsDialog() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.WHITE);
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title
        TextView title = new TextView(this);
        title.setText("How to Play");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(Color.BLACK);

        // Instructions text
        TextView instructions = new TextView(this);
        String instructionsText = "Chess Game Instructions:\n\n" +
                "• White moves first\n" +
                "• Click a piece to select it\n" +
                "• Green squares = possible moves\n" +
                "• Red squares = possible captures\n" +
                "• Pawn: Moves forward, captures diagonally\n" +
                "• Knight: L-shape moves\n" +
                "• Bishop: Diagonal moves\n" +
                "• Rook: Straight moves\n" +
                "• Queen: Any direction\n" +
                "• King: One square any direction\n" +
                "• Checkmate wins the game!";

        instructions.setText(instructionsText);
        instructions.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        instructions.setTextSize(16);
        instructions.setPadding(0, 10, 0, 0);
        instructions.setTextColor(Color.BLACK);
        instructions.setMovementMethod(new ScrollingMovementMethod());
        instructions.setMaxHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.5));

        // Add views to the layout
        dialogLayout.addView(title);
        dialogLayout.addView(instructions);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("OK", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(
                    ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        }
    }


    private void showGameOverDialog(String message) {
        TextView title = findViewById(R.id.gameOverTitle);
        TextView msg = findViewById(R.id.gameOverMessage);
        TextView score = findViewById(R.id.finalScore);

        title.setText("GAME OVER");
        msg.setText(message);
        score.setText("Final Score: " + player1Score + " - " + player2Score);

        gameOverDialog.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        gameOverDialog.startAnimation(animation);
    }

    private void startTimers() {
        player1TimeLeft = TIMER_DURATION;
        player2TimeLeft = TIMER_DURATION;

        // Update display immediately
        updateTimeDisplay();

        whiteTimerRunning = true;
        blackTimerRunning = false;

        startPlayer1Timer();
    }

    private void startPlayer1Timer() {
        if (player1Timer != null) player1Timer.cancel();

        player1Timer = new CountDownTimer(player1TimeLeft, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                player1TimeLeft = millisUntilFinished;
                updateTimeDisplay();
            }

            @Override
            public void onFinish() {
                player1TimeLeft = 0;
                updateTimeDisplay();
                handleTimeOut(WHITE);
            }
        };

        if (gameActive && currentPlayer == WHITE) {
            player1Timer.start();
        }
    }

    private void startPlayer2Timer() {
        if (player2Timer != null) player2Timer.cancel();

        player2Timer = new CountDownTimer(player2TimeLeft, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                player2TimeLeft = millisUntilFinished;
                updateTimeDisplay();
            }

            @Override
            public void onFinish() {
                player2TimeLeft = 0;
                updateTimeDisplay();
                handleTimeOut(BLACK);
            }
        };

        if (gameActive && currentPlayer == BLACK) {
            player2Timer.start();
        }
    }

    private void stopTimer(CountDownTimer timer) {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void stopAllTimers() {
        stopTimer(player1Timer);
        stopTimer(player2Timer);
        whiteTimerRunning = false;
        blackTimerRunning = false;
    }

    private void updateTimeDisplay() {
        runOnUiThread(() -> {
            player1TimeText.setText(formatTime(player1TimeLeft));
            player2TimeText.setText(formatTime(player2TimeLeft));
        });
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "00:00";
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void handleTimeOut(int player) {
        gameActive = false;
        stopAllTimers();

        String winner = (player == WHITE) ? player2Name : player1Name;
        showGameOverDialog("Time's up! " + winner + " wins!");
    }

    private void updateScores() {
        runOnUiThread(() -> {
            player1ScoreText.setText("Score: " + player1Score);
            player2ScoreText.setText("Score: " + player2Score);
        });
    }

    private void updateTurnIndicator() {
        runOnUiThread(() -> {
            if (currentPlayer == WHITE) {
                turnIndicator.setText("WHITE'S TURN");
                turnIndicator.setBackgroundColor(Color.parseColor("#F0D9B5"));
                turnIndicator.setTextColor(Color.BLACK);
            } else {
                turnIndicator.setText("BLACK'S TURN");
                turnIndicator.setBackgroundColor(Color.parseColor("#B58863"));
                turnIndicator.setTextColor(Color.WHITE);
            }
        });
    }

    private void updateMoveCount() {
        runOnUiThread(() -> moveCountText.setText("Move: " + moveCount));
    }

    private List<int[]> getAllPossibleMoves(int player) {
        List<int[]> moves = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = pieces[i][j];
                if (piece != null && piece.color == player) {
                    for (int x = 0; x < BOARD_SIZE; x++) {
                        for (int y = 0; y < BOARD_SIZE; y++) {
                            if (isValidMove(i, j, x, y)) {
                                moves.add(new int[]{i, j, x, y});
                            }
                        }
                    }
                }
            }
        }

        return moves;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAllTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameActive) {
            if (currentPlayer == WHITE) {
                startPlayer1Timer();
            } else {
                startPlayer2Timer();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllTimers();
    }



    @Override
    public void onBackPressed() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title
        TextView title = new TextView(this);
        title.setText("Leave Game");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(getResources().getColor(android.R.color.black));

        // Message
        TextView message = new TextView(this);
        message.setText("Do you want to leave the game?");
        message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        message.setTextSize(16);
        message.setPadding(0, 10, 0, 0);
        message.setTextColor(getResources().getColor(android.R.color.black));

        // Add views to the layout
        dialogLayout.addView(title);
        dialogLayout.addView(message);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("yes", (dialogInterface, which) -> {
                    Intent intent = new Intent(this, chess_setup.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85), // 90% of screen width
                    ViewGroup.LayoutParams.WRAP_CONTENT // Height: wrap content
            );

            // Set dialog background and button text colors
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

            Typeface customFont = ResourcesCompat.getFont(this, R.font.caudex);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(customFont, Typeface.BOLD);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(customFont, Typeface.BOLD);
        } else {
            super.onBackPressed();
        }

    }

    // Piece class
    private class Piece {
        String type;
        int color;
        int row, col;
        boolean hasMoved = false;

        Piece(String type, int color, int row, int col) {
            this.type = type;
            this.color = color;
            this.row = row;
            this.col = col;
        }

        Piece copy() {
            Piece p = new Piece(type, color, row, col);
            p.hasMoved = hasMoved;
            return p;
        }
    }

    // Game state for undo
    private class GameState {
        Piece[][] pieces;
        int currentPlayer;
        int player1Score, player2Score;
        long player1Time, player2Time;
        List<Piece> capturedWhite, capturedBlack;

        GameState(Piece[][] pieces, int player, int score1, int score2,
                  long time1, long time2, List<Piece> capWhite, List<Piece> capBlack) {
            this.pieces = new Piece[BOARD_SIZE][BOARD_SIZE];
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (pieces[i][j] != null) {
                        this.pieces[i][j] = pieces[i][j].copy();
                    }
                }
            }
            this.currentPlayer = player;
            this.player1Score = score1;
            this.player2Score = score2;
            this.player1Time = time1;
            this.player2Time = time2;
            this.capturedWhite = new ArrayList<>(capWhite);
            this.capturedBlack = new ArrayList<>(capBlack);
        }
    }
}