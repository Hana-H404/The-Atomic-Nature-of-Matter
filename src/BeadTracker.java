import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class BeadTracker implements IBeadTracker {
    private double delta;
    private double tau;
    private int minPixels;

    // ---Constructor--------------------------------------
    public BeadTracker(double delta, double tau, int minPixels) {
        if (delta < 0)
            throw new IllegalArgumentException("The value of teh delta can not be less than 0!!");

        this.delta = delta;

        if (tau < 0 || tau > 255)
            throw new IllegalArgumentException("The value of the tau must be between 0 and 255!!");

        this.tau = tau;

        if (minPixels <= 0)
            throw new IllegalArgumentException("Minimum number of pixels must be greater than 0!!");

        this.minPixels = minPixels;
    }

    // -----------------------------------------------------
    // -----------------------------------------------------
    private IBlob[] getPictureBeads(String address) {
        IPicture picture = BeadFactory.createPicture(address);
        IBeadFinder finder = BeadFactory.createBeadFinder(picture, tau);

        return finder.getBeads(minPixels);
    }

    private double findClosestDistance(IBlob[] previousBeads, IBlob currentBead) {
        double minDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < previousBeads.length; i++)
            minDistance = Math.min(minDistance,
                    currentBead.distanceTo(previousBeads[i]));

        return minDistance;
    }

    private ArrayList<Double> processPairOfFrames(IBlob[] previousBeads, IBlob[] currentBeads) {
        ArrayList<Double> validDistances = new ArrayList<>();

        if (previousBeads.length == 0 || currentBeads.length == 0)
            return validDistances;

        double best;
        for (int i = 0; i < currentBeads.length; i++) {
            best = findClosestDistance(previousBeads, currentBeads[i]);
            if (best <= delta)
                validDistances.add(best);
        }

        return validDistances;
    }

    @Override
    public void trackFrames(String[] picturePaths) throws IOException {

        // no pair of frames to track!!
        if (picturePaths == null || picturePaths.length < 2)
            throw new IllegalArgumentException("Frames cannot be less than two ot empty!!");

        IBlob[] previousBeads, currentBeads;
        ArrayList<Double> distances;

        IDistanceWriter writer = BeadFactory.createDistanceWriter();

        previousBeads = getPictureBeads(picturePaths[0]);

        for (int i = 1; i < picturePaths.length; i++) {

            currentBeads = getPictureBeads(picturePaths[i]);
            distances = processPairOfFrames(previousBeads, currentBeads);

            for (double distance : distances)
                writer.saveDistance(distance);

            previousBeads = currentBeads;
        }

        writer.close();
    }
}