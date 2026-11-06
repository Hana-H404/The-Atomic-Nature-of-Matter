import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// ---- frames -> BeadFinder -> BeadTracker -> bead_displacements.txt -> Avogadro

public class IntegrationTest {


    final static String runFolder = "C:\\Users\\HK-Bayrami\\Pictures\\The-Atomic-Nature-of-Matter-main\\Dataset\\run_1";
    final static double delta = 10.0;
    final static double tau = 180.0;
    final static int minPixels = 25;
    final static double dTime = 0.5; // seconds between two frames
    final static double lowestAvogadro = 4.0e23;
    final static double highestAvogadro = 9.0e24;

    static int passed = 0;
    static int failed = 0;

    static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }


    static File resultFile = new File("bead_displacements.txt");
    static File backupFile = new File("bead_displacements_backup.txt");

    static void setUp() {
        if (backupFile.exists())
            backupFile.delete();
        if (resultFile.exists())
            resultFile.renameTo(backupFile);
    }

    static void tearDown() {
        resultFile.delete();
        if (backupFile.exists())
            backupFile.renameTo(resultFile);
    }


    static void runTest() {

        //  find the frames---
        System.out.println("\n--- the frames ---");

        check("the run folder exists", new File(runFolder).isDirectory());

        ArrayList<String> frames = new ArrayList<>();
        File frame = new File(runFolder, String.format(Locale.US, "frame%05d.jpg", 0));
        while (frame.exists()) {
            frames.add(frame.getPath());
            frame = new File(runFolder, String.format(Locale.US, "frame%05d.jpg", frames.size()));
        }
        System.out.println("      frames found: " + frames.size());


        //  BeadFinder and BeadTracker---
        System.out.println("\n--- tracking the beads ---");

        boolean trackingWorked = true;
        try {
            IBeadTracker tracker = BeadFactory.createBeadTracker(delta, tau, minPixels);
            tracker.trackFrames(frames.toArray(new String[0]));
        } catch (Exception e) {
            trackingWorked = false;
            System.out.println("      " + e);
        }
        check("tracking finished without an error", trackingWorked);
        if (!trackingWorked)
            return;

        //  the result file---
        System.out.println("\n--- the result file ---");

        check("bead_displacements.txt was created", resultFile.exists());
        if (!resultFile.exists())
            return;

        List<String> lines = null;
        try {
            lines = Files.readAllLines(resultFile.toPath());
        } catch (IOException e) {
            System.out.println("      " + e);
        }
        check("the file can be read", lines != null);
        if (lines == null)
            return;

        System.out.println("      distances found: " + lines.size());
        check("the file has some distances", lines.size() > 0);

        boolean allAreNumbers = true;
        boolean allAreInRange = true;
        for (String line : lines) {
            try {
                double distance = Double.parseDouble(line);
                if (distance < 0 || distance > delta)
                    allAreInRange = false;
            } catch (NumberFormatException e) {
                allAreNumbers = false;
            }
        }
        check("every line is a number", allAreNumbers);
        check("every distance is between 0 and delta", allAreInRange);

        //  Avogadro---
        System.out.println("\n--- Avogadro's number ---");

        PrintStream screen = System.out;
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        System.setOut(new PrintStream(printed));

        boolean avogadroWorked = true;
        try {
            IAvogadro avogadro = BeadFactory.createAvogadro(dTime);
            avogadro.showAvogadro();
        } catch (Exception e) {
            avogadroWorked = false;
        }

        System.setOut(screen);

        check("Avogadro finished without an error", avogadroWorked);
        if (!avogadroWorked)
            return;

        double estimate = -1;
        for (String line : printed.toString().split("\\R")) {
            if (line.startsWith("Avogadro: "))
                estimate = Double.parseDouble(line.substring("Avogadro: ".length()).trim());
        }
        System.out.println("      Avogadro estimate: " + estimate);

        check("Avogadro printed an estimate", estimate > 0);
        check("the estimate has the right size (4e23 to 9e24)",
                estimate >= lowestAvogadro && estimate <= highestAvogadro);
    }

    public static void main(String[] args) {
        setUp();
        try {
            runTest();
        } finally {
            tearDown();
        }

        System.out.println("\nPassed: " + passed + "    Failed: " + failed);
    }
}
