package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;


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
        JButton rules = new JButton("Rules");
        JButton quit = new JButton("Quit");

        buttonPanel.add(startGame);
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
                    new HiveGame().setVisible(true);
                });
            });

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

        quit.addActionListener(e -> System.exit(0));
    }
}
