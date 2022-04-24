package struct;


public class ImageFloat {
    public float[][] r = null;
    public float[][] g = null;
    public float[][] b = null;

    public ImageFloat(int _row, int _col) {
        r = new float[_row][_col];
        g = new float[_row][_col];
        b = new float[_row][_col];
    }
}
