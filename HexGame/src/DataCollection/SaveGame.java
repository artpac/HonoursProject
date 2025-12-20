package DataCollection;

import Game.HexCoord;
import Game.Piece;

import java.io.*;

public class SaveGame {
    public SaveGame(File saveFile, Piece selectedPiece, HexCoord coord) {
        String moveText = selectedPiece.toString() + selectedPiece.getInstanceNumber() + "," + coord.getQ() + "," + coord.getR() + "\n";
        try (FileWriter fw = new FileWriter(saveFile, true)) {
            fw.write(moveText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
