package Game;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class HexCoord {
    private final int q;
    private final int r;

    public HexCoord(int q, int r) {
        this.q = q;
        this.r = r;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HexCoord)) return false;
        HexCoord h = (HexCoord) o;
        return q == h.q && r == h.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    public List<HexCoord> getNeighbors() {
        return Arrays.asList(
                new HexCoord(q + 1, r),
                new HexCoord(q - 1, r),
                new HexCoord(q, r + 1),
                new HexCoord(q, r - 1),
                new HexCoord(q + 1, r - 1),
                new HexCoord(q - 1, r + 1)
        );
    }

    public Point2D.Double toPixel(double size) {
        double x = size * 1.5 * q;
        double y = size * Math.sqrt(3) * (r + 0.5 * q);
        return new Point2D.Double(x, y);
    }

    public static HexCoord fromPixel(double x, double y, double hexSize) {
        double q = (2.0 / 3.0 * x) / hexSize;
        double r = (-1.0 / 3.0 * x + Math.sqrt(3) / 3.0 * y) / hexSize;
        return hexRound(q, r);
    }

    private static HexCoord hexRound(double q, double r) {
        double s = -q - r;
        int rq = (int) Math.round(q);
        int rr = (int) Math.round(r);
        int rs = (int) Math.round(s);

        double dq = Math.abs(rq - q);
        double dr = Math.abs(rr - r);
        double ds = Math.abs(rs - s);

        if (dq > dr && dq > ds) {
            rq = -rr - rs;
        } else if (dr > ds) {
            rr = -rq - rs;
        }

        return new HexCoord(rq, rr);
    }
}
