import java.io.Closeable;
import java.io.IOException;

public interface IDistanceWriter extends Closeable {

    void saveDistance(double distance) throws IOException;

    @Override
    void close() throws IOException;
}
