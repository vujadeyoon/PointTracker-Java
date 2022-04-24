import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SuperPoint {
    public static float[][] get_float_2d_array_from_text(String path_file_name) throws IOException {
        List<String> listOfStrings = new ArrayList<String>();
        BufferedReader bf = new BufferedReader(new FileReader(path_file_name));
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
}
