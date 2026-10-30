public interface IBlob {

    public void add(int x, int y);

    public double distanceTo(IBlob other);

    public String toString();

    // ---Getter_Methods------------------------------
    public int getMass();

    public double getCenterX();

    public double getCenterY();
}
