import java.util.Locale;

public class Blob implements IBlob {
    private int mass;
    private long sumX;
    private long sumY;

    // ---Constructor------------------------------
    public Blob() {
        mass = 0;
    }

    // ---------------------------------------------

    @Override
    public void add(int x, int y) {
        sumX += x;
        sumY += y;
        mass++;
    }

    @Override
    public double distanceTo(IBlob other) {
        double d = Math.sqrt(Math.pow(getCenterX() - other.getCenterX(), 2) +
                Math.pow(getCenterY() - other.getCenterY(), 2));

        return d;
    }

    @Override
    public String toString() {
        // ---str : mass (center_x, center_y)
        return String.format(Locale.US, "%d (%.4f, %.4f)", mass, getCenterX(), getCenterY());
    }

    // ---Getter_Methods-------------------------------
    @Override
    public int getMass() {
        return mass;
    }

    @Override
    public double getCenterX() {
        if (mass == 0)
            throw new IllegalStateException("Bolb is empty and has no center!!");

        return sumX / (double) mass;
    }

    @Override
    public double getCenterY() {
        if (mass == 0)
            throw new IllegalStateException("Bolb is empty and has no center!!");

        return sumY / (double) mass;
    }

    // ---Testing the validity of this class-----------------------------
    public static void main(String[] args) {
        var b = new Blob();
        try {
            b.getCenterX();
        } catch (IllegalStateException e) {
            System.out.println("empty blob handled");
        }
        b.add(0, 0);
        b.add(1, 1);

        System.out.println(b.toString());

        var b2 = new Blob();
        b2.add(1, 0);
        b2.add(2, 1);
        System.out.println(b2.toString());

        System.out.println(b.distanceTo(b2));
    }
}
