import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class DistanceWriter  implements IDistanceWriter{
    private final FileWriter writer;

    // ---Constructor------------------------------------

    public DistanceWriter() throws IOException {
        writer = new FileWriter("bead_displacements.txt");
    }

    // ---------------------------------------------------
    @Override
    public void saveDistance(double distance) throws IOException {
        writer.write(String.format(Locale.US, "%.4f%n", distance));
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}