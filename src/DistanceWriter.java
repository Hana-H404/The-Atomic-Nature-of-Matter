import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class DistanceWriter {
    private final FileWriter writer;

    // ---Constructor------------------------------------
    public DistanceWriter() throws IOException {
        writer = new FileWriter("bead_displacements.txt");
    }

    // ---------------------------------------------------
    public void saveDistance(double distance) throws IOException {
        writer.write(String.format(Locale.US, "%.4f%n", distance));
    }

    public void close() throws IOException {
        writer.close();
    }
}