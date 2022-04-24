import struct.ImageFloat;
import java.io.*;


public class Main {
    public static void main(String[] args) throws IOException {
        ImageFloat arr_img_color = ImageProcessing.imread("./super_point_result/image_gray/img_0.bmp");
        float[][] arr_img_gray = ImageProcessing.imread_gray("./super_point_result/image_gray/img_0.bmp");
        float[][] pts = SuperPoint.get_float_2d_array_from_text("./super_point_result/pts/pts_0.txt");
        float[][] desc = SuperPoint.get_float_2d_array_from_text("./super_point_result/desc/desc_0.txt");
        MatrixOperator.print_float_2d_array(arr_img_color.r);
        MatrixOperator.print_float_2d_array(arr_img_color.g);
        MatrixOperator.print_float_2d_array(arr_img_color.b);
        MatrixOperator.print_float_2d_array(arr_img_gray);
        MatrixOperator.print_float_2d_array(pts);
        MatrixOperator.print_float_2d_array(desc);
    }
}