package UI;

import AI.HiveAI;
import DataCollection.ReplayGames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MainScreen extends JFrame {
    public MainScreen() {

        //Initialising Screen
        JFrame frame = new JFrame();

        setTitle("Hive Board Game");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Hive Board Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        //Initialising Buttons
        JButton startGame = new JButton("Start Game");
        JButton replayGame = new JButton("Replay Game");
        JButton trainAI = new JButton("Train AI");
        JButton rules = new JButton("Rules");
        JButton quit = new JButton("Quit");

        buttonPanel.add(startGame);
        buttonPanel.add(replayGame);
        buttonPanel.add(trainAI);
        buttonPanel.add(rules);
        buttonPanel.add(quit);

        add(buttonPanel, BorderLayout.CENTER);


        startGame.addActionListener(e -> {

            remove(title);
            JLabel opponentTitle = new JLabel("Select Opponent", SwingConstants.CENTER);
            opponentTitle.setFont(new Font("Arial", Font.BOLD, 22));
            add(opponentTitle, BorderLayout.NORTH);

            JPanel OpponentButtonPanel = new JPanel();
            OpponentButtonPanel.setLayout(new GridLayout(1, 2, 10, 10));
            OpponentButtonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

            JButton pvp = new JButton("Player Vs Player");
            JButton pvc = new JButton("Player Vs Computer");

            OpponentButtonPanel.add(pvp);
            OpponentButtonPanel.add(pvc);

            remove(buttonPanel);
            add(OpponentButtonPanel, BorderLayout.CENTER);

            revalidate();
            repaint();


            pvp.addActionListener(f -> {
                SwingUtilities.invokeLater(() -> {
                    String gameType = "PvP";
                    File saveGame = createSaveFile(gameType);
                    new HiveGame(saveGame).setVisible(true);
                });
            });

            pvc.addActionListener(f -> {
                SwingUtilities.invokeLater(() -> {
                    String gameType = "PvC";
                    File saveGame = createSaveFile(gameType);
                    HiveGame game = new HiveGame(saveGame);

                    // Add AI opponent
                    HiveAI aiOpponent = new HiveAI(true); // true = load trained weights
                    game.getGameBoard().setAIOpponent(aiOpponent, Color.BLACK);

                    game.setVisible(true);
                });
                dispose();
            });

        });

        trainAI.addActionListener(e -> {
            remove(title);
            JLabel trainingTitle = new JLabel("Select Training Method", SwingConstants.CENTER);
            trainingTitle.setFont(new Font("Arial", Font.BOLD, 22));
            add(trainingTitle, BorderLayout.NORTH);

            JPanel TrainingButtonPanel = new JPanel();
            TrainingButtonPanel.setLayout(new GridLayout(1, 2, 10, 10));
            TrainingButtonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

            JButton selfPlay = new JButton("Player Vs Player");
            JButton pvc = new JButton("Player Vs Computer");

        });

        rules.addActionListener(new ActionListener() {
            String url = "https://hivegame.com/download/rules.pdf";
            public void actionPerformed(ActionEvent ae) {
                try {
                    Desktop desktop = java.awt.Desktop.getDesktop();
                    URI oURL = new URI(url);
                    desktop.browse(oURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }});

        replayGame.addActionListener(e -> {
            File selectGame = new File("/Users/artur/Documents/GitHub/HonoursProject/HexGame/SavedGames/");
            JFileChooser fileChooser = new JFileChooser(selectGame);
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                new ReplayGames().replay(file);
                dispose();
            }
        });

        quit.addActionListener(e -> System.exit(0));
    }

    public static File createSaveFile(String gameType) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDateTime = LocalDateTime.now().format(dateFormat);
        String saveGameFile = "/Users/artur/Documents/GitHub/HonoursProject/HexGame/SavedGames/" + formattedDateTime + " - "+ gameType + " - Winner - WinnerColour.csv";
        File saveGame = new File(saveGameFile);
        try {
            if (saveGame.createNewFile()) {
                String testText = "Piece,X,Y\n";
                try (FileWriter fw = new FileWriter(saveGame)) {
                    fw.write(testText);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return saveGame;
    }

}
