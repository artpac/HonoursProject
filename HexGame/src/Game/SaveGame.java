package Game;

import java.io.*;

public class SaveGame {
    public SaveGame(File saveFile, Piece selectedPiece, HexCoord coord) {
        String moveText = selectedPiece.toString() + "," + coord.getQ() + "," + coord.getR() + "\n";  // Append with newline for readability
        try (FileWriter fw = new FileWriter(saveFile, true)) {  // Set append mode to true
            fw.write(moveText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
