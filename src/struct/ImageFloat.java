package struct;


public class ImageFloat {
    public float[][] r = null;
    public float[][] g = null;
    public float[][] b = null;

    public ImageFloat(int row, int col) {
        r = new float[row][col];
        g = new float[row][col];
        b = new float[row][col];
    }
}
