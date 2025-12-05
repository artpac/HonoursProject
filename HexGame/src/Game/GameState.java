package Game;

import java.awt.Color;
import java.util.*;

class GameState {
    private HiveBoard board;
    private Map<Color, List<Piece>> reserves;
    private Color currentPlayer;
    private Map<Color, Boolean> queenPlaced;
    private int turnCount;
    private int whiteTurnCount;
    private int blackTurnCount;

    public GameState() {
        this.board = new HiveBoard();
        this.reserves = new HashMap<>();
        this.currentPlayer = Color.WHITE;
        this.queenPlaced = new HashMap<>();
        this.turnCount = 0;

        initializeReserves();
    }

    private void initializeReserves() {
        reserves.put(Color.WHITE, createPieceSet(Color.WHITE));
        reserves.put(Color.BLACK, createPieceSet(Color.BLACK));
        queenPlaced.put(Color.WHITE, false);
        queenPlaced.put(Color.BLACK, false);
    }

    private List<Piece> createPieceSet(Color color) {
        List<Piece> pieces = new ArrayList<>();
        for (PieceType type : PieceType.values()) {
            for (int i = 0; i < type.getCount(); i++) {
                pieces.add(new Piece(type, color, i + 1));
            }
        }
        return pieces;
    }

    public HiveBoard getBoard() {
        return board;
    }

    public List<Piece> getReserve(Color color) {
        return reserves.get(color);
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public void nextPlayer() {
        currentPlayer = currentPlayer.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        turnCount++;
        if (currentPlayer.equals(Color.WHITE)) {
            whiteTurnCount++;
        } else {
            blackTurnCount++;
        }
    }

    public int getTurnCount() {
        return turnCount;
    }

    public boolean isQueenPlaced(Color color) {
        return queenPlaced.get(color);
    }

    public void setQueenPlaced(Color color) {
        queenPlaced.put(color, true);
    }

    public boolean mustPlaceQueen() {
//        return turnCount >= 3 && !queenPlaced.get(currentPlayer);
        return whiteTurnCount >= 3 && !queenPlaced.get(Color.WHITE) || blackTurnCount >= 4 && !queenPlaced.get(Color.BLACK);
    }

    public void removePieceFromReserve(Piece piece) {
        reserves.get(currentPlayer).remove(piece);
    }
}