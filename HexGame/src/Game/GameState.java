package Game;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class GameState {
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

    public GameState clone() {
        GameState cloned = new GameState();

        // Deep clone board
        HiveBoard clonedBoard = new HiveBoard();
        for (Map.Entry<HexCoord, List<Piece>> entry : this.board.getBoard().entrySet()) {
            HexCoord coord = new HexCoord(entry.getKey().getQ(), entry.getKey().getR());
            List<Piece> clonedStack = new ArrayList<>();
            for (Piece p : entry.getValue()) {
                Piece clonedPiece = new Piece(p.getType(), p.getColor(), p.getInstanceNumber());
                if (p.getPosition() != null) {
                    clonedPiece.setPosition(new HexCoord(p.getPosition().getQ(), p.getPosition().getR()));
                }
                clonedStack.add(clonedPiece);
            }
            clonedBoard.getBoard().put(coord, clonedStack);
        }

        // Use reflection to set private fields
        try {
            java.lang.reflect.Field boardField = GameState.class.getDeclaredField("board");
            boardField.setAccessible(true);
            boardField.set(cloned, clonedBoard);

            // Clone reserves
            Map<Color, List<Piece>> clonedReserves = new HashMap<>();
            for (Color color : new Color[]{Color.WHITE, Color.BLACK}) {
                List<Piece> clonedReserve = new ArrayList<>();
                for (Piece p : this.reserves.get(color)) {
                    clonedReserve.add(new Piece(p.getType(), p.getColor(), p.getInstanceNumber()));
                }
                clonedReserves.put(color, clonedReserve);
            }

            java.lang.reflect.Field reservesField = GameState.class.getDeclaredField("reserves");
            reservesField.setAccessible(true);
            reservesField.set(cloned, clonedReserves);

            // Copy other fields
            java.lang.reflect.Field currentPlayerField = GameState.class.getDeclaredField("currentPlayer");
            currentPlayerField.setAccessible(true);
            currentPlayerField.set(cloned, this.currentPlayer);

            java.lang.reflect.Field turnCountField = GameState.class.getDeclaredField("turnCount");
            turnCountField.setAccessible(true);
            turnCountField.set(cloned, this.turnCount);

            java.lang.reflect.Field queenPlacedField = GameState.class.getDeclaredField("queenPlaced");
            queenPlacedField.setAccessible(true);
            Map<Color, Boolean> clonedQueenPlaced = new HashMap<>(this.queenPlaced);
            queenPlacedField.set(cloned, clonedQueenPlaced);

        } catch (Exception e) {
            System.err.println("Error cloning GameState: " + e.getMessage());
            e.printStackTrace();
        }

        return cloned;
    }
}