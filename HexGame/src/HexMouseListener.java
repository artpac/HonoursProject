import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JLabel;

class HexMouseListener implements MouseListener, MouseMotionListener {
    private final HexGridDisplay hexGrid;
    private int mouseX = 0;
    private int mouseY = 0;
    private HexGridDisplay.Hex hoverHex = null;  // Fixed type
    private JLabel coordsLabel;

    public HexMouseListener(HexGridDisplay hexGrid) {
        this.hexGrid = hexGrid;
        this.setupCoordinatesLabel();
    }

    private void setupCoordinatesLabel() {
        this.coordsLabel = new JLabel("Mouse Position: (0, 0)");
        this.coordsLabel.setFont(new Font("Arial", Font.BOLD, 14));  // Using Font constant instead of magic number
        this.coordsLabel.setBounds(10, 10, 200, 20);
        this.coordsLabel.setForeground(Color.BLACK);
        this.hexGrid.add(this.coordsLabel);
    }

    public int getMouseX() {
        return this.mouseX;
    }

    public int getMouseY() {
        return this.mouseY;
    }

    public HexGridDisplay.Hex getHoverHex() {  // Fixed return type
        return this.hoverHex;
    }

    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        this.updateHoverHex(e);
        this.updateCoordinatesLabel();
        this.hexGrid.repaint();
    }

    private void updateCoordinatesLabel() {
        this.coordsLabel.setText(String.format("Mouse Position: (%d, %d)", this.mouseX, this.mouseY));
        if (this.hoverHex != null) {
            this.coordsLabel.setText(this.coordsLabel.getText() +
                String.format(" | Hex: (%d, %d)", this.hoverHex.q, this.hoverHex.r));
        }
    }

    public void mouseDragged(MouseEvent e) {
        this.mouseMoved(e);
    }

    public void mouseClicked(MouseEvent e) {
        if (this.hoverHex != null) {
            this.hexGrid.setCentreHex(new HexGridDisplay.Hex(this.hoverHex.q, this.hoverHex.r));
            System.out.println("Selected hex: (" + this.hoverHex.q + ", " + this.hoverHex.r + ")");
            this.hexGrid.repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    private void updateHoverHex(MouseEvent e) {
        HexGridDisplay.Hex closestHex = null;
        double minDistance = Double.MAX_VALUE;

        for (HexGridDisplay.Hex h : this.hexGrid.getHexes()) {
            Point p = this.hexGrid.hexToPixel(h);
            double distance = Math.sqrt(
                Math.pow(p.x - this.mouseX, 2) +
                Math.pow(p.y - this.mouseY, 2)
            );
            if (distance < minDistance) {
                minDistance = distance;
                closestHex = h;
            }
        }

        this.hoverHex = closestHex;
        this.hexGrid.setHoverHex(closestHex); // Add this line to update the hover effect
    }

    public void mouseExited(MouseEvent e) {
        this.mouseX = -1;
        this.mouseY = -1;
        this.hoverHex = null;
        this.hexGrid.setHoverHex(null); // Add this line to clear hover effect
        this.updateCoordinatesLabel();
        this.hexGrid.repaint();
    }
}