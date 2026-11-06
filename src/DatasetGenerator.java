import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatasetGenerator implements IDatasetGenerator {

    private final Path datasetRoot;

    private static final int RUN_COUNT = 10;

    // ---Construcor------------------------------------------------
    public DatasetGenerator(Path datasetRoot) {
        this.datasetRoot = datasetRoot.toAbsolutePath().normalize();
    }
    // --------------------------------------------------------------

    @Override
    public List<String> generateRunDirectories() throws IOException {

        if (!Files.isDirectory(datasetRoot))
            throw new IOException("Dataset directory does not exist!");

        List<String> runDirectories = new ArrayList<String>();
        Path runDirectory;

        for (int i = 1; i <= RUN_COUNT; i++) {
            runDirectory = datasetRoot.resolve("run_" + i);

            if (!Files.isDirectory(runDirectory))
                throw new IOException("Missing derectory :" + runDirectory);

            runDirectories.add(runDirectory.toString());
        }

        return runDirectories;
    }

    @Override
    public List<String> generateFramePaths(String runDirectory, int frameCount) throws IOException {

        File directory = new File(runDirectory);

        if (!directory.exists() || !directory.isDirectory())
            throw new IOException("Run directory does not exist!!");

        if (frameCount <= 0)
            throw new IllegalArgumentException("Frame count must be greater than zero!!");

        return generateOrderedFramePaths(directory, frameCount);
    }

    // -----------------------------------------------------------------------
    private List<String> generateOrderedFramePaths(File runDirectory, int frameCount) throws IOException {

        List<String> picturePaths = new ArrayList<>();
        String fileName;
        File pictureFile;

        for (int i = 0; i < frameCount; i++) {

            fileName = String.format(Locale.US, "frame%05d.jpg", i);

            pictureFile = new File(runDirectory, fileName);

            if (!pictureFile.isFile())
                throw new IOException("Frame not found: " + pictureFile);

            picturePaths.add(pictureFile.getAbsolutePath());
        }

        return picturePaths;
    }

}
