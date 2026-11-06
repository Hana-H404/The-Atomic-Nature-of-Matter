import java.io.IOException;

public interface IBeadTracker {

    void trackFrames(String[] picturePaths, String outputFilePath) throws IOException;
}
