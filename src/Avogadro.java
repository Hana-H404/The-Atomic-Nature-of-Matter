import java.io.IOException;

public class Avogadro {

    private double sumSquaredDistances = 0;
    private int count = 0;
    private final double d_time;
    private double avogadro;

    // ---Constructor------------------------------------------------------------------
    public Avogadro(double d_time) throws IOException {

        this.d_time = d_time;

        Double distance;
        var reader = new DistanceReader();

        while ((distance = reader.readNextDistance()) != null) {

            distance = distance * 0.175 * Math.pow(10, -6);

            sumSquaredDistances += Math.pow(distance, 2);
            count++;
        }
    }

    // -----------------------------------------------------------------------------------
    public void showAvogadro() {
        avogadro = 8.31446 / calculateK();

        System.out.println("Avogadro: " + avogadro);
    }

    // -------------------------------------------------------------------------------------
    private double calculateVariancePerAxis() {

        if (count == 0)
            throw new IllegalStateException("No distance data");

        double variance = sumSquaredDistances / (2 * count);

        System.out.println("variance: " + variance);

        return variance;
    }

    private double calculateD() {

        double D = calculateVariancePerAxis() / (d_time * 2);

        System.out.println("D: " + D);

        return D;
    }

    private double calculateK() {

        double K = (calculateD() * 6 * Math.PI * 9.135
                * 0.5 * Math.pow(10, -10)) / 297;

        System.out.println("K: " + K);

        return K;
    }

    // ----------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
        var avo = new Avogadro(0.5);
        avo.showAvogadro();
    }
}
