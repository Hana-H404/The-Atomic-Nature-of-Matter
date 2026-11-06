import java.io.Closeable;
import java.io.IOException;

public interface IDistanceReader extends Closeable {


    Double readNextDistance() throws IOException;

    @Override
    void close() throws IOException;
}
