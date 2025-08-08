package main.java.com.example.hexgame.view;

import main.java.com.example.hexgame.event.HexMouseListener;


import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HexGridDisplay extends JPanel {
    static final int HEX_SIZE = 22; // Radius of each hex
    static final int WIDTH = 24;    // Number of columns
    static final int HEIGHT = 24;   // Number of rows
    static final double SQRT3 = Math.sqrt(3);

    public static class Hex {
        public int q;
        public int r;
        public Hex(int q, int r) { this.q = q; this.r = r; }
    }

    List<Hex> hexes;
    Hex centreHex;
    private Hex hoverHex; // Add this field

    public HexGridDisplay() {
        HexMouseListener mouseListener = new HexMouseListener(this);
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        hexes = generateGrid();
    }

    // Add this method to set the hover hex
    public void setHoverHex(Hex hex) {
        this.hoverHex = hex;
        repaint(); // Request a repaint when hover changes
    }

    public List<Hex> getHexes() {
        return this.hexes;
    }

    public void setCentreHex(Hex hex) {
        this.centreHex = hex;
    }

    public Point hexToPixel(Hex h) {
        double x = 22.0 * SQRT3 * (h.q + h.r / 2.0);
        double y = 33.0 * h.r;
        return new Point((int)x, (int)y);
    }

    private List<Hex> generateGrid() {
        List<Hex> list = new ArrayList<>();
        for (int r = 0; r < HexGridDisplay.HEIGHT; r++) {
            int r_offset = r >> 1;
            for (int q = -r_offset; q < HexGridDisplay.WIDTH - r_offset; q++) {
                list.add(new Hex(q, r));
            }
        }
        return list;
    }

    private Polygon hexPolygon(int x, int y) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 180 * (60 * i - 30); // pointy-topped
            int dx = (int) (x + HEX_SIZE * Math.cos(angle));
            int dy = (int) (y + HEX_SIZE * Math.sin(angle));
            hex.addPoint(dx, dy);
        }
        return hex;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.BLACK);

        int centerRow = HEIGHT / 2;
        int centerCol = WIDTH / 2 - (centerRow >> 1);
        centreHex = new Hex(centerCol, centerRow);

        for (Hex h : hexes) {
            Point p = hexToPixel(h);
            Polygon hex = hexPolygon(p.x, p.y);
            
            // Fill with different colors based on hex state
            if (h.q == centreHex.q && h.r == centreHex.r) {
                g2.setColor(Color.RED);
                g2.fillPolygon(hex);
            } else if (hoverHex != null && h.q == hoverHex.q && h.r == hoverHex.r) {
                // Add semi-transparent yellow highlight for hover
                g2.setColor(new Color(255, 255, 0, 100));
                g2.fillPolygon(hex);
            }
            
            // Draw the border
            g2.setColor(Color.BLACK);
            g2.drawPolygon(hex);
        }
    }
}