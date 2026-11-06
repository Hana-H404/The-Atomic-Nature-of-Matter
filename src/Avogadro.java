import java.io.IOException;

public class Avogadro implements IAvogadro{

    private double sumSquaredDistances = 0;
    private int count = 0;
    private final double d_time;
    private double avogadro;

    // ---Constructor------------------------------------------------------------------
    public Avogadro(double d_time) throws IOException {

        if (d_time <= 0)
            throw new IllegalArgumentException("Time must be positive");

        this.d_time = d_time;

        Double distance;
        IDistanceReader reader = BeadFactory.createDistanceReader();

        while ((distance = reader.readNextDistance()) != null) {

            distance = distance * 0.175 * Math.pow(10, -6);

            sumSquaredDistances += Math.pow(distance, 2);
            count++;
        }

        reader.close();
    }

    // -----------------------------------------------------------------------------------
    @Override
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
        IAvogadro avo = BeadFactory.createAvogadro(0.5);
        avo.showAvogadro();
    }
}
