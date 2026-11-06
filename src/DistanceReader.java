import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DistanceReader implements IDistanceReader {

    private final BufferedReader reader;

    public DistanceReader() throws IOException {
        reader = new BufferedReader(
                new FileReader("bead_displacements.txt"));
    }
    @Override
    public Double readNextDistance() throws IOException {
        String line = reader.readLine();

        if (line == null)
            return null;

        return Double.parseDouble(line);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
