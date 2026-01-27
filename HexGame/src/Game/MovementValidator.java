package Game;

import java.util.*;

public class MovementValidator {
    private HiveBoard board;

    public MovementValidator(HiveBoard board) {
        this.board = board;
    }

    public boolean breaksHive(HexCoord from, HexCoord to) {
        List<Piece> fromStack = board.getStackAt(from);
        if (fromStack.size() > 1) {
            return false;
        }

        List<Piece> temp = new ArrayList<>(fromStack);
        board.getBoard().remove(from);
        boolean connected = board.isHiveConnected();
        board.getBoard().put(from, temp);

        return !connected;
    }

    public boolean canSlideToBasic(HexCoord from, HexCoord to) {
        if (board.containsCoord(to) && board.getStackAt(to).size() == 1) {
            return false;
        }

        List<HexCoord> commonNeighbors = new ArrayList<>(from.getNeighbors());
        commonNeighbors.retainAll(to.getNeighbors());

        boolean hasGate = false;
        for (HexCoord common : commonNeighbors) {
            if (board.containsCoord(common)) {
                hasGate = true;
                break;
            }
        }

        return hasGate && board.hasAdjacentPiece(to);
    }

    public boolean canSlideTo(HexCoord start, HexCoord from, HexCoord to) {
        if (to.equals(start)) return false;
        if (board.containsCoord(to)) return false;

        List<HexCoord> commonNeighbors = new ArrayList<>(from.getNeighbors());
        commonNeighbors.retainAll(to.getNeighbors());

        int blocked = 0;
        for (HexCoord common : commonNeighbors) {
            if (board.containsCoord(common) && !common.equals(start)) {
                blocked++;
            }
        }

        return blocked > 0 && blocked < 2 && board.hasAdjacentPieceExcluding(to, start);
    }
}