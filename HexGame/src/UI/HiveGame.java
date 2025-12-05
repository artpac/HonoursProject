package UI;

import javax.swing.*;
import java.awt.*;
import Game.GameBoard;

public class HiveGame extends JFrame {

    private GameBoard gameBoard;

    public HiveGame() {
        setTitle("Hive Board Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gameBoard = new GameBoard();
        add(gameBoard, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.add(gameBoard.getStatusLabel());
        add(statusPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainScreen().setVisible(true);
        });
    }
}
