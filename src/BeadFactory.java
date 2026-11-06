import java.io.IOException;

// The only place that knows which concrete classes are used.
// Everything else depends on interfaces (Dependency Inversion Principle).
public final class BeadFactory {

    private BeadFactory() {
    }

    public static IBeadFinder createBeadFinder(IPicture picture, double tau) {
        return new BeadFinder(picture, tau);
    }

    public static IDistanceReader createDistanceReader() throws IOException {
        return new DistanceReader();
    }

    public static IDistanceWriter createDistanceWriter() throws IOException {
        return new DistanceWriter();
    }

    public static IBeadTracker createBeadTracker(double delta, double tau, int minPixels)
            throws IOException {
        return new BeadTracker(delta, tau, minPixels);

    }

    public static IAvogadro createAvogadro(double dTime) throws IOException {
        return new Avogadro(dTime);
    }

    public static IBlob createBlob(){ return  new Blob();}


    public static IPicture createPicture(String path){return new PictureAdapter(path);}
}
