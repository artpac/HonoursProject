package UI;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import Game.GameBoard;

public class HiveGame extends JFrame {

    private GameBoard gameBoard;
    private File saveFile;

    public HiveGame(File saveGame) {
        this.saveFile = saveGame;
        setTitle("Hive Board Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gameBoard = new GameBoard(saveFile);
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

    public GameBoard getGameBoard() {
        return gameBoard;
    }
}