package Game;

import java.awt.Color;

public class Piece {
    private PieceType type;
    private Color color;
    private HexCoord position;
    private int stackLevel;
    private int instanceNumber;

    public Piece(PieceType type, Color color, int instanceNumber) {
        this.type = type;
        this.color = color;
        this.stackLevel = 0;
        this.instanceNumber = instanceNumber;
    }

    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public HexCoord getPosition() {
        return position;
    }

    public void setPosition(HexCoord position) {
        this.position = position;
    }

    public int getStackLevel() {
        return stackLevel;
    }

    public void setStackLevel(int level) {
        this.stackLevel = level;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    @Override
    public String toString() {
        String colorPrefix = color.equals(Color.WHITE) ? "W" : "B";
        return colorPrefix + type.getSymbol() + instanceNumber;
    }
}