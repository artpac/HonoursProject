import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hex Grid");
            frame.setDefaultCloseOperation(3);
            frame.add(new HexGridDisplay());
            frame.pack();
            frame.setLocationRelativeTo((Component)null);
            frame.setVisible(true);
            frame.setResizable(false);
        });
    }
}