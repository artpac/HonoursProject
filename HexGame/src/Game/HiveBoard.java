package Game;

import java.util.*;

class HiveBoard {
    private Map<HexCoord, List<Piece>> board;

    public HiveBoard() {
        this.board = new HashMap<>();
    }

    public Map<HexCoord, List<Piece>> getBoard() {
        return board;
    }

    public boolean isEmpty() {
        return board.isEmpty();
    }

    public int size() {
        return board.size();
    }

    public boolean containsCoord(HexCoord coord) {
        return board.containsKey(coord);
    }

    public List<Piece> getStackAt(HexCoord coord) {
        return board.get(coord);
    }

    public Piece getTopPieceAt(HexCoord coord) {
        List<Piece> stack = board.get(coord);
        if (stack == null || stack.isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public void placePiece(Piece piece, HexCoord coord) {
        piece.setPosition(coord);
        board.putIfAbsent(coord, new ArrayList<>());
        board.get(coord).add(piece);
    }

    public void movePiece(HexCoord from, HexCoord to) {
        List<Piece> fromStack = board.get(from);
        Piece piece = fromStack.remove(fromStack.size() - 1);

        if (fromStack.isEmpty()) {
            board.remove(from);
        }

        piece.setPosition(to);
        board.putIfAbsent(to, new ArrayList<>());
        board.get(to).add(piece);
    }

    public boolean isHiveConnected() {
        if (board.isEmpty()) return true;

        Set<HexCoord> visited = new HashSet<>();
        Queue<HexCoord> queue = new LinkedList<>();

        HexCoord start = board.keySet().iterator().next();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            HexCoord current = queue.poll();
            for (HexCoord neighbor : current.getNeighbors()) {
                if (board.containsKey(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() == board.size();
    }

    public boolean hasAdjacentPiece(HexCoord coord) {
        for (HexCoord neighbor : coord.getNeighbors()) {
            if (board.containsKey(neighbor)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAdjacentPieceExcluding(HexCoord coord, HexCoord exclude) {
        for (HexCoord neighbor : coord.getNeighbors()) {
            if (!neighbor.equals(exclude) && board.containsKey(neighbor)) {
                return true;
            }
        }
        return false;
    }

    public Set<HexCoord> getAllCoordinates() {
        return board.keySet();
    }
}