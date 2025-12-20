package DataCollection;

import Game.*;
import UI.HiveGame;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.SwingUtilities;
import java.util.List;

public class ReplayGames {
    private static final int REPLAY_DELAY_MS = 1000; // 1 second delay between moves
    private HiveGame game;

    public void replay(File saveFile) {
        game = new HiveGame(saveFile);
        game.setVisible(true);

        Thread replayThread = new Thread(() -> {
            try {
                // Give the UI time to initialize
                Thread.sleep(500);
                
                try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
                    String line;
                    // Skip header line
                    br.readLine();
                    
                    while ((line = br.readLine()) != null) {
                        final String currentMove = line;
                        SwingUtilities.invokeLater(() -> processMove(currentMove));
                        Thread.sleep(REPLAY_DELAY_MS);
                    }
                }
            } catch (IOException e) {
                handleReplayError("Error reading save file: " + e.getMessage());
            } catch (InterruptedException e) {
                handleReplayError("Replay interrupted: " + e.getMessage());
            }
        });
        
        replayThread.start();
    }

    private void processMove(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid move format: " + line);
            }

            String readPiece = parts[0];
            Piece piece = parsePiece(readPiece);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            HexCoord coord = new HexCoord(x, y);

            System.out.println("Replaying move: " + piece + " at (" + x + ", " + y + ")");
            
            GameBoard gameBoard = game.getGameBoard();
            GameState gameState = gameBoard.getGameState();
            
            // Place piece using the static method
            GameBoard.placeReplayPiece(coord, piece, gameState);
            
            // Remove from reserves if necessary
            List<Piece> reserve = gameState.getReserve(piece.getColor());
            if (reserve != null) {
                reserve.removeIf(p -> 
                    p.getType() == piece.getType() && 
                    p.getColor() == piece.getColor() && 
                    p.getInstanceNumber() == piece.getInstanceNumber()
                );
            }
            
            // Force a repaint of the game board
            gameBoard.repaint();
            
        } catch (Exception e) {
            handleReplayError("Error processing move: " + e.getMessage());
        }
    }

    private void handleReplayError(String message) {
        System.err.println(message);
        SwingUtilities.invokeLater(() -> {
            if (game != null) {
                game.repaint();
            }
        });
    }

    // Keep the existing parsePiece method unchanged
    private static Piece parsePiece(String pieceToken) {
        String t = pieceToken.trim();
        if (t.length() < 3) {
            throw new IllegalArgumentException("Invalid piece token: " + pieceToken);
        }

        char c = Character.toUpperCase(t.charAt(0));
        Color color = switch (c) {
            case 'W' -> Color.WHITE;
            case 'B' -> Color.BLACK;
            default -> throw new IllegalArgumentException("Unknown color prefix: " + c);
        };

        char p = Character.toUpperCase(t.charAt(1));
        PieceType type = switch (p) {
            case 'S' -> PieceType.SPIDER;
            case 'G' -> PieceType.GRASSHOPPER;
            case 'Q' -> PieceType.QUEEN;
            case 'A' -> PieceType.ANT;
            case 'B' -> PieceType.BEETLE;
            default -> throw new IllegalArgumentException("Unknown piece type: " + p);
        };

        int instance = Integer.parseInt(t.substring(2));
        return new Piece(type, color, instance);
    }
}