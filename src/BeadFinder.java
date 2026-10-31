import java.util.ArrayList;
import java.awt.Color;

public class BeadFinder {
    private Picture picture;
    private int width;
    private int height;
    private double tau;
    private boolean[][] isVisited;
    private boolean[][] isForeground;
    private ArrayList<IBlob> blobs;

    // ---Cinstructor---------------------------------
    public BeadFinder(Picture picture, double tau) {
        this.picture = picture;
        this.tau = tau;

        width = picture.width();
        height = picture.height();

        isForeground = new boolean[width][height];
        findWhitePixels();

        isVisited = new boolean[width][height];

        blobs = new ArrayList<>();
        findBlobs();
    }

    // ------------------------------------------------
    public IBlob[] getBeads(int min) {

        ArrayList<IBlob> beads = new ArrayList<>();
        IBlob temp;
        for (int i = 0; i < blobs.size(); i++) {
            temp = blobs.get(i);
            if (temp.getMass() >= min)
                beads.add(temp);
        }

        return beads.toArray(new IBlob[0]);
    }

    // -------------------------------------------------
    private void findBlobs() {
        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {

                if (isForeground[i][j] && !isVisited[i][j]) {
                    IBlob blob = new Blob();
                    dfsSearch(blob, i, j);
                    blobs.add(blob);
                }
            }
        }

    }

    private void dfsSearch(IBlob blob, int x, int y) {
        if (((x < 0) || (x >= width) || (y < 0) || (y >= height))
                || isVisited[x][y])
            return;

        isVisited[x][y] = true;

        if (!isForeground[x][y])
            return;

        blob.add(x, y);
        // forward(y - 1 because Picture the fourth quadrant of the coordinate system)
        dfsSearch(blob, x, y - 1);
        // rightward
        dfsSearch(blob, x + 1, y);
        // backward
        dfsSearch(blob, x, y + 1);
        // leftward
        dfsSearch(blob, x - 1, y);
    }

    private void findWhitePixels() {

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                isForeground[i][j] = isWhite(picture.get(i, j));

    }

    private boolean isWhite(Color pixel) {
        double brightness = 0.299 * pixel.getRed() +
                0.587 * pixel.getGreen() +
                0.114 * pixel.getBlue();

        return brightness >= tau;
    }

    // --------------------------------------------------------------------
    public static void main(String[] args) {

        int min = 25;
        double tau = 180.0;
        String imagePath = "D:\\Uni\\Java_Projects\\The_Atomin_Nature_Of_Matter\\Atomic-Nature-of-Matter-master\\Atomic-Nature-of-Matter-master\\Dataset\\run_1\\frame00001.jpg";

        // Allow command-line arguments
        if (args.length != 0) {
            if (args.length != 3) {
                System.out.println(
                        "Usage: java BeadFinder <min> <tau> <imagePath>");
                return;
            }

            min = Integer.parseInt(args[0]);
            tau = Double.parseDouble(args[1]);
            imagePath = args[2];
        }

        // Read image
        Picture picture = new Picture(imagePath);

        // Create BeadFinder
        BeadFinder finder = new BeadFinder(picture, tau);

        // Find valid beads
        IBlob[] beads = finder.getBeads(min);

        // Test results
        int totalBlobs = finder.blobs.size();
        int totalBeads = beads.length;

        System.out.println("Image: " + imagePath);
        System.out.println("Tau: " + tau);
        System.out.println("Min pixels: " + min);

        System.out.println("\n--- Test Results ---");

        System.out.println(
                "Total Blobs: " + totalBlobs +
                        (totalBlobs == 15 ? " -> PASS" : " -> FAIL (Expected 15)"));

        System.out.println(
                "Valid Beads: " + totalBeads +
                        (totalBeads == 13 ? " -> PASS" : " -> FAIL (Expected 13)"));

        System.out.println("\n--- Detected Beads ---");

        for (IBlob bead : beads) {
            System.out.println(bead);
        }
    }
}
