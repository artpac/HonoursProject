package Game;

public class PlacementValidator {
    private HiveBoard board;

    public PlacementValidator(HiveBoard board) {
        this.board = board;
    }

    public boolean canPlaceAt(HexCoord coord, Piece piece) {
        if (board.isEmpty()) {
            return coord.equals(new HexCoord(0, 0));
        }

        if (board.containsCoord(coord)) {
            return false;
        }

        if (board.size() == 1) {
            for (HexCoord neighbor : coord.getNeighbors()) {
                if (board.containsCoord(neighbor)) {
                    return true;
                }
            }
            return false;
        }

        boolean hasOwnNeighbor = false;
        for (HexCoord neighbor : coord.getNeighbors()) {
            if (board.containsCoord(neighbor)) {
                Piece topPiece = board.getTopPieceAt(neighbor);
                if (topPiece.getColor().equals(piece.getColor())) {
                    hasOwnNeighbor = true;
                } else {
                    return false;
                }
            }
        }

        return hasOwnNeighbor;
    }
}