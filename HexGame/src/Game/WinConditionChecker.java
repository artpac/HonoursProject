package Game;

import java.awt.Color;
import java.util.List;
import java.util.Map;

class WinConditionChecker {
    private HiveBoard board;

    public WinConditionChecker(HiveBoard board) {
        this.board = board;
    }

    public String checkWin() {
        boolean whiteQueenSurrounded = false;
        boolean blackQueenSurrounded = false;

        for (Map.Entry<HexCoord, List<Piece>> entry : board.getBoard().entrySet()) {
            Piece piece = entry.getValue().get(0);
            if (piece.getType() == PieceType.QUEEN) {
                int neighbors = 0;
                for (HexCoord neighbor : entry.getKey().getNeighbors()) {
                    if (board.containsCoord(neighbor)) {
                        neighbors++;
                    }
                }

                if (neighbors == 6) {
                    if (piece.getColor().equals(Color.WHITE)) {
                        whiteQueenSurrounded = true;
                    } else {
                        blackQueenSurrounded = true;
                    }
                }
            }
        }

        if (whiteQueenSurrounded && blackQueenSurrounded) {
            return "Draw! Both Queens surrounded!";
        } else if (whiteQueenSurrounded) {
            return "Black wins! White Queen surrounded!";
        } else if (blackQueenSurrounded) {
            return "White wins! Black Queen surrounded!";
        }

        return null;
    }
}
