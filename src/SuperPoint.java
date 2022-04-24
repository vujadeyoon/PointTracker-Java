import struct.PTS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SuperPoint {
    public static ArrayList<PTS> get_pts_from_text(String _path_file) throws IOException {
        ArrayList<PTS> res = new ArrayList<>();

        List<String> listOfStrings = new ArrayList<String>();
        BufferedReader bf = new BufferedReader(new FileReader(_path_file));
        String line = bf.readLine();

        while (line != null) {
            listOfStrings.add(line);
            line = bf.readLine();
        }

        bf.close();

        String[] array = listOfStrings.toArray(new String[0]);
        int row = Integer.parseInt(array[0]);
        int col = Integer.parseInt(array[1]);

        float[][] tmp = MatrixOperator.get_float_2d_array(row, col, 0.0f);
        int cnt = 0;
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[0].length; j++) {
                tmp[i][j] = Float.parseFloat(array[2 + cnt]);
                cnt++;
            }
        }

        for (int i = 0; i < col; i++) {
            res.add(new PTS(tmp[2][i], (int) tmp[0][i], (int) tmp[1][i]));
        }

        return res;
    }

    public static float[][] get_desc_from_text(String _path_file) throws IOException {
        List<String> listOfStrings = new ArrayList<String>();
        BufferedReader bf = new BufferedReader(new FileReader(_path_file));
        String line = bf.readLine();

        while (line != null) {
            listOfStrings.add(line);
            line = bf.readLine();
        }

        bf.close();

        String[] array = listOfStrings.toArray(new String[0]);
        int row = Integer.parseInt(array[0]);
        int col = Integer.parseInt(array[1]);
        float[][] res = MatrixOperator.get_float_2d_array(row, col, 0.0f);

        int cnt = 0;
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[i][j] = Float.parseFloat(array[2 + cnt]);
                cnt++;
            }
        }

        return res;
    }

    public static void print_pts(ArrayList<PTS> _pts) {
        for (int i = 0; i < _pts.size(); i++) {
            System.out.printf("%d; %d; %f.", _pts.get(i).x, _pts.get(i).y, _pts.get(i).score);
        }
    }

    public static void print_desc(float[][] _desc) {
        for (int i = 0; i < _desc.length; i++) {
            for (int j = 0; j < _desc[0].length; j++) {
                System.out.printf("%f ", _desc[i][j]);
            }
            System.out.println();
        }
    }
}
