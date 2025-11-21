enum PieceType {
    QUEEN(1, "Q"),
    ANT(3, "A"),
    SPIDER(2, "S"),
    GRASSHOPPER(3, "G"),
    BEETLE(2, "B");

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