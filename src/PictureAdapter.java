import java.awt.Color;

public class PictureAdapter implements IPicture {

    private final Picture picture;

    public PictureAdapter(String path) {
        this.picture = new Picture(path);
    }

    @Override
    public int width() {
        return picture.width();
    }

    @Override
    public int height() {
        return picture.height();
    }

    @Override
    public Color get(int x, int y) {
        return picture.get(x, y);
    }
}
