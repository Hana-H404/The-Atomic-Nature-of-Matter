import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DistanceReader {

    private final BufferedReader reader;

    public DistanceReader() throws IOException {
        reader = new BufferedReader(
                new FileReader("bead_displacements.txt"));
    }

    public Double readNextDistance() throws IOException {
        String line = reader.readLine();

        if (line == null)
            return null;

        return Double.parseDouble(line);
    }

    public void close() throws IOException {
        reader.close();
    }
}
