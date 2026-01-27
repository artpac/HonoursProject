package AI;

import Game.*;
import java.awt.Color;
import java.util.List;

/**
 * Represents an AI move decision
 */
public class AIMove {
    private Piece piece;
    private HexCoord from;
    private HexCoord to;
    private MoveType type;
    private double score;

    public AIMove(Piece piece, HexCoord from, HexCoord to, MoveType type) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.type = type;
        this.score = 0.0;
    }

    public Piece getPiece() { return piece; }
    public HexCoord getFrom() { return from; }
    public HexCoord getTo() { return to; }
    public MoveType getType() { return type; }
    public double getScore() { return score; }
    public void setScore(double s) { this.score = s; }

    /**
     * Find the actual piece in the game state's reserve that matches this move's piece
     */
    public Piece findActualPiece(GameState state) {
        if (type == MoveType.MOVE) {
            // For movement, get piece from board
            return state.getBoard().getTopPieceAt(from);
        }

        // For placement, find matching piece in reserve
        Color color = piece.getColor();
        PieceType type = piece.getType();
        int instanceNum = piece.getInstanceNumber();

        List<Piece> reserve = state.getReserve(color);
        for (Piece p : reserve) {
            if (p.getType() == type &&
                    p.getColor().equals(color) &&
                    p.getInstanceNumber() == instanceNum) {
                return p;
            }
        }

        // Fallback: find any piece of the same type and color
        for (Piece p : reserve) {
            if (p.getType() == type && p.getColor().equals(color)) {
                return p;
            }
        }

        return null;
    }

    public void execute(GameState state) {
        if (type == MoveType.PLACE) {
            // Find the actual piece from reserve
            Piece actualPiece = findActualPiece(state);
            if (actualPiece == null) {
                System.err.println("ERROR: Cannot find piece in reserve: " + piece);
                return;
            }

            state.getBoard().placePiece(actualPiece, to);
            state.removePieceFromReserve(actualPiece);
            if (actualPiece.getType() == PieceType.QUEEN) {
                state.setQueenPlaced(actualPiece.getColor());
            }
        } else if (type == MoveType.MOVE) {
            state.getBoard().movePiece(from, to);
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s from %s to (%d,%d)",
                piece.getColor().equals(Color.WHITE) ? "White" : "Black",
                piece.getType().name(),
                from != null ? "(" + from.getQ() + "," + from.getR() + ")" : "reserve",
                to.getQ(), to.getR());
    }
}