import struct.ImageFloat;
import struct.PTS;
import java.io.*;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) throws IOException {
        int params_num_frames = 30;
        int params_tracker_max_length = 5;
        float params_tracker_nn_thresh = 0.7f;
        int params_tracker_min_length = 2;

        PointTracker tracker = new PointTracker(params_tracker_max_length, params_tracker_nn_thresh);
        for (int _num_frame = 0; _num_frame < params_num_frames; _num_frame++) {
            ImageFloat arr_img_color = ImageProcessing.imread("./super_point_result/image_gray/img_" + _num_frame + ".bmp");
            float[][] arr_img_gray = ImageProcessing.imread_gray("./super_point_result/image_gray/img_" + _num_frame + ".bmp");
            ArrayList<PTS> pts = SuperPoint.get_pts_from_text("./super_point_result/pts/pts_" + _num_frame + ".txt");
            float[][] desc = SuperPoint.get_desc_from_text("./super_point_result/desc/desc_" + _num_frame + ".txt");

            // tracker.update(pts, desc);
            // float[][] tracks = tracker.get_tracks(params_tracker_min_length)

            /*
            MatrixOperator.print_float_2d_array(arr_img_color.r);
            MatrixOperator.print_float_2d_array(arr_img_color.g);
            MatrixOperator.print_float_2d_array(arr_img_color.b);
            MatrixOperator.print_float_2d_array(arr_img_gray);
            SuperPoint.print_pts(pts);
            SuperPoint.print_desc(desc);
            System.out.println(_num_frame);
            Util.pause();
            */

        }
    }
}