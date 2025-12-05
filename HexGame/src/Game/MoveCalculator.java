package Game;

import java.util.*;

class MoveCalculator {
    private HiveBoard board;
    private MovementValidator validator;

    public MoveCalculator(HiveBoard board, MovementValidator validator) {
        this.board = board;
        this.validator = validator;
    }

    public List<HexCoord> getValidMoves(Piece piece, HexCoord from) {
        List<HexCoord> moves = new ArrayList<>();

        switch (piece.getType()) {
            case QUEEN:
                moves = getQueenMoves(from);
                break;
            case ANT:
                moves = getAntMoves(from);
                break;
            case SPIDER:
                moves = getSpiderMoves(from);
                break;
            case GRASSHOPPER:
                moves = getGrasshopperMoves(from);
                break;
            case BEETLE:
                moves = getBeetleMoves(from);
                break;
        }

        moves.removeIf(coord -> validator.breaksHive(from, coord));
        return moves;
    }

    private List<HexCoord> getQueenMoves(HexCoord from) {
        List<HexCoord> moves = new ArrayList<>();
        for (HexCoord neighbor : from.getNeighbors()) {
            if (validator.canSlideToBasic(from, neighbor)) {
                moves.add(neighbor);
            }
        }
        return moves;
    }

    private List<HexCoord> getAntMoves(HexCoord from) {
        Set<HexCoord> visited = new HashSet<>();
        Queue<HexCoord> queue = new LinkedList<>();
        List<HexCoord> moves = new ArrayList<>();

        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            HexCoord current = queue.poll();
            for (HexCoord neighbor : current.getNeighbors()) {
                if (!visited.contains(neighbor) && validator.canSlideTo(from, current, neighbor)) {
                    visited.add(neighbor);
                    if (!neighbor.equals(from)) {
                        queue.add(neighbor);
                        moves.add(neighbor);
                    }
                }
            }
        }

        return moves;
    }

    private List<HexCoord> getSpiderMoves(HexCoord from) {
        List<HexCoord> moves = new ArrayList<>();
        Set<HexCoord> visited = new HashSet<>();
        spiderDFS(from, from, visited, 0, moves);
        return moves;
    }

    private void spiderDFS(HexCoord start, HexCoord current, Set<HexCoord> visited,
                           int depth, List<HexCoord> moves) {
        if (depth == 3) {
            if (!current.equals(start)) {
                moves.add(current);
            }
            return;
        }

        visited.add(current);
        for (HexCoord neighbor : current.getNeighbors()) {
            if (!visited.contains(neighbor) && validator.canSlideTo(start, current, neighbor)) {
                spiderDFS(start, neighbor, new HashSet<>(visited), depth + 1, moves);
            }
        }
    }

    private List<HexCoord> getGrasshopperMoves(HexCoord from) {
        List<HexCoord> moves = new ArrayList<>();

        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}, {1,-1}, {-1,1}};
        for (int[] dir : directions) {
            HexCoord current = new HexCoord(from.getQ() + dir[0], from.getR() + dir[1]);
            if (board.containsCoord(current)) {
                while (board.containsCoord(current)) {
                    current = new HexCoord(current.getQ() + dir[0], current.getR() + dir[1]);
                }
                moves.add(current);
            }
        }

        return moves;
    }

    private List<HexCoord> getBeetleMoves(HexCoord from) {
        List<HexCoord> moves = new ArrayList<>();
        for (HexCoord neighbor : from.getNeighbors()) {
            if (board.containsCoord(neighbor) || validator.canSlideToBasic(from, neighbor)) {
                moves.add(neighbor);
            }
        }
        return moves;
    }
}