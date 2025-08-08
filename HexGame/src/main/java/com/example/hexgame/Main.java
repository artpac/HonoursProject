package main.java.com.example.hexgame;

import main.java.com.example.hexgame.view.HexGridDisplay;

import javax.swing.*;

public final class Main {

    private static final String FRAME_TITLE = "Hex Grid";
    private static final int FRAME_WIDTH = 915;
    private static final int FRAME_HEIGHT = 810;

    private Main() {
        // Prevent instantiation
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }

    private static void createAndShowUI() {
        JFrame frame = createMainFrame();
        frame.setVisible(true);
    }

    private static JFrame createMainFrame() {
        JFrame frame = new JFrame(FRAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new HexGridDisplay());
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLocationRelativeTo(null);
        return frame;
    }
}