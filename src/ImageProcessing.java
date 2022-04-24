import struct.ImageFloat;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class ImageProcessing {
    public static float[][] imread_gray(String path_file_name) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path_file_name));
        } catch (IOException e) {
            throw new IllegalArgumentException("ImageIO.read is failed.");
        }

        int height = img.getHeight();
        int width = img.getWidth();
        int rgb = 0;
        float[][] res = MatrixOperator.get_float_2d_array(height, width, 0.0f);


        for (int h = 0; h<height; h++) {
            for (int w = 0; w<width; w++) {
                rgb = img.getRGB(w, h);

                res[h][w] = (float) ((rgb >> 16) & 0x000000FF);
            }
        }
        return res;
    }

    public static ImageFloat imread(String path_file_name) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path_file_name));
        } catch (IOException e) {
            throw new IllegalArgumentException("ImageIO.read is failed.");
        }

        int height = img.getHeight();
        int width = img.getWidth();
        int rgb = 0;
        ImageFloat res = new ImageFloat(height, width);

        for (int h = 0; h<height; h++) {
            for (int w = 0; w<width; w++) {
                rgb = img.getRGB(w, h);

                res.r[h][w] = (float) ((rgb >> 16) & 0x000000FF);
                res.g[h][w] = (float) ((rgb >> 8) & 0x000000FF);
                res.b[h][w] = (float) ((rgb) & 0x000000FF);
            }
        }
        return res;
    }
}
