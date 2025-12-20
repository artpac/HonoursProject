package Game;

import DataCollection.ReplayGames;
import DataCollection.SaveGame;
import UI.HiveGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class GameBoard extends JPanel {
    private GameState gameState;
    private File saveFile;
    private PlacementValidator placementValidator;
    private MovementValidator movementValidator;
    private MoveCalculator moveCalculator;
    private WinConditionChecker winChecker;
    private GameRenderer renderer;

    private Piece selectedPiece;
    private HexCoord selectedCoord;
    private List<HexCoord> validMoves;
    private JLabel statusLabel;

    private Piece draggedPiece;
    private Point dragPoint;
    private boolean isDragging;


    public GameBoard(File saveGame) {
        //Set Game Size and Colour
        setPreferredSize(new Dimension(1200, 700));
        setBackground(new Color(240, 230, 210));

        gameState = new GameState();
        placementValidator = new PlacementValidator(gameState.getBoard());
        movementValidator = new MovementValidator(gameState.getBoard());
        moveCalculator = new MoveCalculator(gameState.getBoard(), movementValidator);
        winChecker = new WinConditionChecker(gameState.getBoard());
        renderer = new GameRenderer(gameState);

        selectedPiece = null;
        selectedCoord = null;
        validMoves = new ArrayList<>();
        statusLabel = new JLabel("White's turn - Place a piece");

        isDragging = false;
        draggedPiece = null;
        dragPoint = null;

        this.saveFile = saveGame;

        // Ensure UI updates after loading
        revalidate();
        repaint();

        setupMouseListeners();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e.getPoint());
            }

            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e.getPoint());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e.getPoint());
            }
        });
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    private void handleMousePressed(Point p) {
        // Check if clicking on reserve piece
        if (p.x < 200) {
            Piece piece = renderer.getPieceAtPoint(p, gameState.getReserve(gameState.getCurrentPlayer()));
            if (piece != null) {
                // Check if must place queen
                if (gameState.mustPlaceQueen() && piece.getType() != PieceType.QUEEN) {
                    statusLabel.setText("Must place Queen by turn 4!");
                    return;
                }

                selectedPiece = piece;
                draggedPiece = piece;
                dragPoint = p;
                isDragging = true;
                selectedCoord = null;
                validMoves.clear();
                repaint();
            }
        } else {
            // Check if clicking on board piece
            HexCoord coord = renderer.getHexAtPoint(p);
            if (gameState.getBoard().containsCoord(coord)) {
                Piece topPiece = gameState.getBoard().getTopPieceAt(coord);
                if (topPiece.getColor().equals(gameState.getCurrentPlayer()) &&
                        gameState.isQueenPlaced(gameState.getCurrentPlayer())) {
                    selectedPiece = topPiece;
                    selectedCoord = coord;
                    validMoves = moveCalculator.getValidMoves(topPiece, coord);
                    draggedPiece = null;
                    repaint();
                }
            }
        }
    }

    private void handleMouseDragged(Point p) {
        if (isDragging && draggedPiece != null) {
            dragPoint = p;
            repaint();
        }
    }

    private void handleMouseReleased(Point p) {
        if (isDragging && draggedPiece != null) {
            HexCoord coord = renderer.getHexAtPoint(p);

            if (placementValidator.canPlaceAt(coord, draggedPiece)) {
                gameState.getBoard().placePiece(draggedPiece, coord);
                gameState.removePieceFromReserve(draggedPiece);

                if (draggedPiece.getType() == PieceType.QUEEN) {
                    gameState.setQueenPlaced(draggedPiece.getColor());
                }
                saveGame(coord);

                selectedPiece = null;
                nextTurn();
            }

            isDragging = false;
            draggedPiece = null;
            dragPoint = null;
            repaint();
        } else if (selectedCoord != null && p.x > 200) {
            // Moving a piece from board
            HexCoord coord = renderer.getHexAtPoint(p);

            if (validMoves.contains(coord)) {
                gameState.getBoard().movePiece(selectedCoord, coord);
                saveGame(coord);
                selectedPiece = null;
                selectedCoord = null;
                validMoves.clear();

                nextTurn();

            } else {
                selectedPiece = null;
                selectedCoord = null;
                validMoves.clear();
            }
            repaint();
        }
    }

    private void nextTurn() {
        String winMessage = winChecker.checkWin(saveFile);
        if (winMessage != null) {
            statusLabel.setText(winMessage);
            return;
        }

        gameState.nextPlayer();

        String playerName = gameState.getCurrentPlayer().equals(Color.WHITE) ? "White" : "Black";
        String status = playerName + "'s turn - ";

        if (gameState.mustPlaceQueen()) {
            status += "Must place Queen!";
        } else if (!gameState.isQueenPlaced(gameState.getCurrentPlayer())) {
            status += "Place a piece";
        } else {
            status += "Place or move a piece";
        }

        statusLabel.setText(status);
    }

    public static void placeReplayPiece(HexCoord coord, Piece piece, GameState gameState) {
        if (coord != null && piece != null && gameState != null) {
            piece.setPosition(coord);
            gameState.getBoard().placePiece(piece, coord);
            
            // Update queen placement status if necessary
            if (piece.getType() == PieceType.QUEEN) {
                gameState.setQueenPlaced(piece.getColor());
            }
            
            // Update game state
            gameState.nextPlayer();
        }
    }

    public void saveGame(HexCoord coord) {
        if (selectedPiece != null && coord != null) {
            new SaveGame(saveFile, selectedPiece, coord);
            System.out.println("Saved selected piece and coordinates.");
        } else {
            System.out.println("No selected piece or coordinate to save.");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        renderer.render(g2, selectedPiece, selectedCoord, validMoves, draggedPiece, dragPoint);
    }

    public GameState getGameState() {
        return gameState;
    }
}