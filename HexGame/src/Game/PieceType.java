package Game;

public enum PieceType {
    QUEEN(1, "Qu"),
    ANT(3, "An"),
    SPIDER(2, "Sp"),
    GRASSHOPPER(3, "Gr"),
    BEETLE(2, "Be");

    private final int count;
    private final String symbol;

    PieceType(int count, String symbol) {
        this.count = count;
        this.symbol = symbol;
    }

    public int getCount() {
        return count;
    }

    public String getSymbol() {
        return symbol;
    }
}