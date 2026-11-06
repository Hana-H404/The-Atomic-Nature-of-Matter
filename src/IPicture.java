import java.awt.Color;

public interface IPicture {

    int width();

    int height();

    Color get(int x, int y);
}
