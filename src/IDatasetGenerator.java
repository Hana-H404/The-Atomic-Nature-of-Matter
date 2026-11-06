import java.io.IOException;
import java.util.List;

public interface IDatasetGenerator {

    List<String> generateRunDirectories() throws IOException;

    List<String> generateFramePaths(String runDirectory, int frameCount) throws IOException;
}