import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

public class UnitTest {

    static int passed = 0;
    static int failed = 0;

    // prints PASS or FAIL for one check
    static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    // =====================================================================
    // Blob tests
    // =====================================================================
    static void testBlob() {
        System.out.println("\n--- Blob ---");

        // a new blob is empty and has no center
        IBlob blob = BeadFactory.createBlob();
        check("new blob has mass 0", blob.getMass() == 0);

        boolean thrown = false;
        try {
            blob.getCenterX();
        } catch (IllegalStateException e) {
            thrown = true;
        }
        check("empty blob has no center", thrown);

        blob.add(0, 0);
        blob.add(1, 1);
        blob.add(2, 5);
        check("mass is 3 after 3 pixels", blob.getMass() == 3);

        // center = average of the pixels
        check("center x is 1", blob.getCenterX() == 1.0);
        check("center y is 2", blob.getCenterY() == 2.0);

        IBlob a = BeadFactory.createBlob();
        a.add(0, 0);
        IBlob b =BeadFactory.createBlob();
        b.add(3, 4);
        check("distance is 5", a.distanceTo(b) == 5.0);


        IBlob c =  BeadFactory.createBlob();
        c.add(0, 0);
        c.add(1, 1);
        check("toString", c.toString().equals("2 (0.5000, 0.5000)"));
    }

    // =====================================================================
    // BeadFinder tests
    // =====================================================================

    // A fake picture made of text, so we don't need an image file
    // Every String is one row, every character is one pixel
    //   '#' = white   ,   '.' = black  ,  'x' = gray (brightness 100)
    static class FakePicture implements IPicture {
        String[] rows;

        FakePicture(String[] rows) {
            this.rows = rows;
        }

        public int width() {
            return rows[0].length();
        }

        public int height() {
            return rows.length;
        }

        public Color get(int x, int y) {
            char c = rows[y].charAt(x);

            if (c == '#')
                return new Color(255, 255, 255);
            if (c == 'x')
                return new Color(100, 100, 100);
            return new Color(0, 0, 0);
        }
    }

    static void testBeadFinder() {
        System.out.println("\n--- BeadFinder ---");

        // a black picture has no blobs
        IPicture picture = new FakePicture(new String[] {
                "...",
                "..."
        });
        IBeadFinder finder = BeadFactory.createBeadFinder(picture, 128);
        check("black picture has 0 blobs", finder.totalBlobSize() == 0);

        // a white picture is one blob with 12 pixels
        picture = new FakePicture(new String[] {
                "####",
                "####",
                "####"
        });
        finder = BeadFactory.createBeadFinder(picture, 128);
        check("white picture has 1 blob", finder.totalBlobSize() == 1);
        check("the blob has 12 pixels", finder.getBeads(1)[0].getMass() == 12);

        // two separate blobs
        picture = new FakePicture(new String[] {
                "#..#",
                "#..#"
        });
        finder = BeadFactory.createBeadFinder(picture, 128);
        check("two separate blobs", finder.totalBlobSize() == 2);

        // diagonal pixels don't become beads(we only look up, down, left, right)
        picture = new FakePicture(new String[] {
                "#.",
                ".#"
        });
        finder = BeadFactory.createBeadFinder(picture, 128);
        check("diagonal pixels are 2 blobs", finder.totalBlobSize() == 2);

        // getBeads keeps only the big enough blobs
        picture = new FakePicture(new String[] {
                "##..#",
                "##..#"
        });
        finder = BeadFactory.createBeadFinder(picture, 128);
        check("totalBlobSize counts all the blobs", finder.totalBlobSize() == 2);
        check("getBeads(2) gives 2 beads", finder.getBeads(2).length == 2);
        check("getBeads(3) gives 1 bead", finder.getBeads(3).length == 1);
        check("getBeads(4) gives 1 bead", finder.getBeads(4).length == 1);
        check("getBeads(5) gives 0 beads", finder.getBeads(5).length == 0);


        picture = new FakePicture(new String[] {
                ".#.",
                ".#.",
                "..."
        });
        finder = BeadFactory.createBeadFinder(picture, 128);
        IBlob bead = finder.getBeads(1)[0];
        check("center x is the column", bead.getCenterX() == 1.0);
        check("center y is the row", bead.getCenterY() == 0.5);


        picture = new FakePicture(new String[] { "xx", "xx" });
        check("tau 99: gray is white", BeadFactory.createBeadFinder(picture, 99).totalBlobSize() == 1);
        check("tau 100: gray is white", BeadFactory.createBeadFinder(picture, 100).totalBlobSize() == 1);
        check("tau 101: gray is not white", BeadFactory.createBeadFinder(picture, 101).totalBlobSize() == 0);

        // a big blob (300 x 300 white pixels)
        String[] rows = new String[300];
        for (int i = 0; i < rows.length; i++)
            rows[i] = "#".repeat(300);
        finder = BeadFactory.createBeadFinder(new FakePicture(rows), 128);
        check("big blob has 90000 pixels", finder.getBeads(1)[0].getMass() == 90000);
    }

    // =============================================================
    // BeadTracker tests
    // ==============================================================

    static File resultFile = new File("bead_displacements.txt");
    static File backupFile = new File("bead_displacements_backup.txt");
    static File frameFolder = new File("test_frames");


    static void setUp() {
        if (backupFile.exists())
            backupFile.delete();
        if (resultFile.exists())
            resultFile.renameTo(backupFile);

        frameFolder.mkdir();
    }


    static void deleteAll() {
        for (File file : frameFolder.listFiles())
            file.delete();
        frameFolder.delete();

        resultFile.delete();
        if (backupFile.exists())
            backupFile.renameTo(resultFile);
    }

    // Saves a 60 x 60 black picture with white 5 x 5 beads.
    // returns the path of the picture.
    static String makeFrame(String name, int[][] beads) throws IOException {
        BufferedImage image = new BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < beads.length; i++)
            for (int x = beads[i][0]; x < beads[i][0] + 5; x++)
                for (int y = beads[i][1]; y < beads[i][1] + 5; y++)
                    image.setRGB(x, y, Color.WHITE.getRGB());

        File file = new File(frameFolder, name);
        ImageIO.write(image, "png", file);
        return file.getAbsolutePath();
    }

    static void testBeadTracker() throws IOException {
        System.out.println("\n--- BeadTracker ---");

        // ---wrong values for the constructor---
        boolean thrown = false;
        try {
            BeadFactory.createBeadTracker(-1, 180, 25);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("negative delta is refused", thrown);

        thrown = false;
        try {
            BeadFactory.createBeadTracker(5, 256, 25);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("tau above 255 is refused", thrown);

        thrown = false;
        try {
            BeadFactory.createBeadTracker(5, 180, 0);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("minPixels 0 is refused", thrown);

        // ---wrong values for trackFrames---
        String still = makeFrame("still.png", new int[][] { { 10, 10 } });
        IBeadTracker tracker =BeadFactory.createBeadTracker(5, 180, 25);

        thrown = false;
        try {
            tracker.trackFrames(new String[] { still });
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("only one frame is refused", thrown);

        // ---a bead moves 3 pixels---
        String moved = makeFrame("moved.png", new int[][] { { 13, 10 } });
        tracker.trackFrames(new String[] { still, moved });
        List<String> lines = Files.readAllLines(resultFile.toPath());
        check("moving 3 pixels gives 1 line", lines.size() == 1);
        check("the distance is 3.0000", lines.get(0).equals("3.0000"));

        // ---a bead moves 15 pixels, but delta is 5---
        String far = makeFrame("far.png", new int[][] { { 25, 10 } });
        tracker.trackFrames(new String[] { still, far });
        lines = Files.readAllLines(resultFile.toPath());
        check("a move bigger than delta is ignored", lines.size() == 0);

        // ---a bead moves 3 pixels and delta is 3---
        IBeadTracker tracker3 =BeadFactory.createBeadTracker(3, 180, 25);
        tracker3.trackFrames(new String[] { still, moved });
        lines = Files.readAllLines(resultFile.toPath());
        check("a move equal to delta is kept", lines.size() == 1);

        // ---the beads have 25 pixels, but minPixels is 26---
        IBeadTracker tracker26 =BeadFactory.createBeadTracker(5, 180, 26);
        tracker26.trackFrames(new String[] { still, moved });
        lines = Files.readAllLines(resultFile.toPath());
        check("small blobs are ignored", lines.size() == 0);

        // ---a frame without beads---
        String empty = makeFrame("empty.png", new int[0][0]);
        tracker.trackFrames(new String[] { empty, still });
        lines = Files.readAllLines(resultFile.toPath());
        check("no beads in the first frame gives no lines", lines.size() == 0);

        // ---two beads: the first moves 1 pixel, the second moves 3 pixels---
        String twoBefore = makeFrame("two1.png", new int[][] { { 10, 10 }, { 40, 40 } });
        String twoAfter = makeFrame("two2.png", new int[][] { { 11, 10 }, { 40, 43 } });
        tracker.trackFrames(new String[] { twoBefore, twoAfter });
        lines = Files.readAllLines(resultFile.toPath());
        check("two beads give 2 lines", lines.size() == 2);
        check("first distance is 1.0000", lines.get(0).equals("1.0000"));
        check("second distance is 3.0000", lines.get(1).equals("3.0000"));

        // ---three frames: the bead moves 3 pixels, then 2 pixels---
        String moved2 = makeFrame("moved2.png", new int[][] { { 15, 10 } });
        tracker.trackFrames(new String[] { still, moved, moved2 });
        lines = Files.readAllLines(resultFile.toPath());
        check("three frames give 2 lines", lines.size() == 2);
        check("first distance is 3.0000", lines.get(0).equals("3.0000"));
        check("second distance is 2.0000", lines.get(1).equals("2.0000"));
    }


    public static void main(String[] args) throws IOException {
        testBlob();
        testBeadFinder();
        setUp();
        try {
            testBeadTracker();
        } finally {
            deleteAll();
        }

        System.out.println("\nPassed: " + passed + "    Failed: " + failed);
    }
}
