import java.util.ArrayList;
import java.util.Stack;
import java.awt.Color;

public class BeadFinder implements IBeadFinder {
    private final IPicture picture;

    private final int width;
    private final int height;

    private final double tau;

    private final boolean[][] isVisited;
    private final boolean[][] isForeground;

    private final ArrayList<IBlob> blobs;

    private static final int[] dx = { 0, 1, 0, -1 };
    private static final int[] dy = { -1, 0, 1, 0 };

    // ---Constructor---------------------------------
    public BeadFinder(IPicture picture, double tau) {
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
    @Override
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

    @Override
    public int totalBlobSize() {
        return blobs.size();
    }

    // -------------------------------------------------
    private void findBlobs() {
        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {

                if (isForeground[i][j] && !isVisited[i][j]) {
                    IBlob blob = BeadFactory.createBlob();
                    dfsSearch(blob, i, j);
                    blobs.add(blob);
                }
            }
        }

    }

    private void dfsSearch(IBlob blob, int startX, int startY) {
        if (((startX < 0) || (startX >= width) ||
                (startY < 0) || (startY >= height)) ||
                isVisited[startX][startY])
            return;

        isVisited[startX][startY] = true;
        blob.add(startX, startY);

        java.util.Stack<int[]> stack = new Stack<>();
        stack.push(new int[] { startX, startY });

        int[] current;
        int x, y;

        while (!stack.isEmpty()) {
            current = stack.pop();

            for (int i = 0; i < 4; i++) {
                x = current[0] + dx[i];
                y = current[1] + dy[i];

                if (((x < 0) || (x >= width) || (y < 0) || (y >= height)) || isVisited[x][y])
                    continue;

                isVisited[x][y] = true;

                if (!isForeground[x][y])
                    continue;

                stack.push(new int[] { x, y });
                blob.add(x, y);
            }
        }
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

}