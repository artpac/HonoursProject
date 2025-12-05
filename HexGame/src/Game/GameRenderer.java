package Game;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

class GameRenderer {
    private static final int HEX_SIZE = 22;
    private static final int BOARD_CENTER_X = 700;
    private static final int BOARD_CENTER_Y = 350;
    private GameState gameState;

    public GameRenderer(GameState gameState) {
        this.gameState = gameState;
    }

    public void render(Graphics2D g2, Piece selectedPiece, HexCoord selectedCoord,
                       List<HexCoord> validMoves, Piece draggedPiece, Point dragPoint) {
        drawHexGrid(g2);
        drawBoard(g2, selectedCoord);
        drawValidMoves(g2, validMoves);
        drawReserve(g2, selectedPiece, draggedPiece);

        if (draggedPiece != null && dragPoint != null) {
            drawDraggedPiece(g2, draggedPiece, dragPoint);
        }
    }

    private void drawHexGrid(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 20));

        for (int q = -12; q <= 12; q++) {
            for (int r = -15; r <= 15; r++) {
                HexCoord coord = new HexCoord(q, r);
                Point2D.Double center = coord.toPixel(HEX_SIZE);
                center.x += BOARD_CENTER_X;
                center.y += BOARD_CENTER_Y;

                if (q == 0 && r == 0) {
                    g2.setColor(new Color(255, 100, 100, 100));
                    drawHexFilled(g2, center.x, center.y);
                }

                g2.setColor(new Color(0, 0, 0, 20));
                drawHexOutline(g2, center.x, center.y);
            }
        }
    }


    private void drawReserve(Graphics2D g2, Piece selectedPiece, Piece draggedPiece) {
        g2.setColor(Color.BLACK);
        String playerName = gameState.getCurrentPlayer().equals(Color.WHITE) ? "White" : "Black";
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString(playerName + " Reserve:", 20, 40);

        List<Piece> reserve = gameState.getReserve(gameState.getCurrentPlayer());

        int y = 70;
        for (Piece piece : reserve) {
            if (piece == draggedPiece) {
                continue; // Don't draw if being dragged
            }

            boolean isSelected = selectedPiece == piece;

            if (isSelected) {
                g2.setColor(new Color(255, 255, 150));
                g2.fillRoundRect(15, y - 25, 190, 50, 10, 10);
            }

            drawPieceAt(g2, piece, 50, y, HEX_SIZE - 5);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString(piece.getType().name(), 90, y + 5);

            y += 60;
        }
    }

    private void drawBoard(Graphics2D g2, HexCoord selectedCoord) {
        for (Map.Entry<HexCoord, List<Piece>> entry : gameState.getBoard().getBoard().entrySet()) {
            HexCoord coord = entry.getKey();
            List<Piece> stack = entry.getValue();

            Point2D.Double center = coord.toPixel(HEX_SIZE);
            center.x += BOARD_CENTER_X;
            center.y += BOARD_CENTER_Y;

            for (int i = 0; i < stack.size(); i++) {
                Piece piece = stack.get(i);
                double offsetX = i * 2;
                double offsetY = i * 2;

                drawPieceAt(g2, piece, center.x - offsetX, center.y - offsetY, HEX_SIZE);
            }

            if (selectedCoord != null && selectedCoord.equals(coord)) {
                g2.setColor(new Color(255, 255, 0, 150));
                g2.setStroke(new BasicStroke(3));
                drawHexOutline(g2, center.x, center.y);
                g2.setStroke(new BasicStroke(1));
            }
        }
    }

    private void drawValidMoves(Graphics2D g2, List<HexCoord> validMoves) {
        g2.setColor(new Color(0, 255, 0, 100));
        for (HexCoord coord : validMoves) {
            Point2D.Double center = coord.toPixel(HEX_SIZE);
            center.x += BOARD_CENTER_X;
            center.y += BOARD_CENTER_Y;
            drawHexFilled(g2, center.x, center.y);
        }
    }

    private void drawDraggedPiece(Graphics2D g2, Piece piece, Point p) {
        drawPieceAt(g2, piece, p.x, p.y, HEX_SIZE);
    }

    private void drawPieceAt(Graphics2D g2, Piece piece, double cx, double cy, double size) {
        // Draw hex shape
        Polygon hex = createHexagon(cx, cy, size);

        g2.setColor(piece.getColor());
        g2.fillPolygon(hex);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawPolygon(hex);
        g2.setStroke(new BasicStroke(1));

        // Draw piece symbol
        g2.setColor(piece.getColor().equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        String text = piece.getType().getSymbol();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2.drawString(text, (int)(cx - textWidth / 2), (int)(cy + textHeight / 3));
    }

    private void drawHexOutline(Graphics2D g2, double cx, double cy) {
        Polygon hex = createHexagon(cx, cy, HEX_SIZE);
        g2.drawPolygon(hex);
    }

    private void drawHexFilled(Graphics2D g2, double cx, double cy) {
        Polygon hex = createHexagon(cx, cy, HEX_SIZE);
        g2.fillPolygon(hex);
    }

    private Polygon createHexagon(double cx, double cy, double size) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i;
            int x = (int)(cx + size * Math.cos(angle));
            int y = (int)(cy + size * Math.sin(angle));
            hex.addPoint(x, y);
        }
        return hex;
    }

    public HexCoord getHexAtPoint(Point p) {
        return HexCoord.fromPixel(p.x - BOARD_CENTER_X, p.y - BOARD_CENTER_Y, HEX_SIZE);
    }

    public Piece getPieceAtPoint(Point p, List<Piece> reserve) {
        int y = 70;
        for (Piece piece : reserve) {
            if (p.x >= 15 && p.x <= 185 && p.y >= y - 25 && p.y <= y + 25) {
                return piece;
            }
            y += 60;
        }
        return null;
    }
}