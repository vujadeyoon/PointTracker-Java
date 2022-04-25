import java.util.Arrays;
// Android
// import android.util.Log;


public class MatrixOperator {
    protected static boolean[] get_bool_1d_array(int row, boolean val) {
        boolean[] res = new boolean[row];
        Arrays.fill(res, val);

        return res;
    }

    public static int[] get_int_1d_array(int row, int val) {
        int[] res = new int[row];
        Arrays.fill(res, val);

        return res;
    }

    protected static int[][] get_int_2d_array(int row, int col, int val) {
        int[][] res = new int[row][col];

        for (int i = 0; i < res.length; i++) {
            Arrays.fill(res[i], val);
        }

        return res;
    }

    protected static int[] get_int_1d_arange(int row) {
        int[] res = get_int_1d_array(row, 0);
        for (int y = 0; y < res.length; y++) {
            res[y] = y;
        }

        return res;
    }

    protected static float[] get_float_1d_array(int row, float val) {
        float[] res = new float[row];
        Arrays.fill(res, val);

        return res;
    }

    protected static float[][] get_float_2d_array(int row, int col, float val) {
        float[][] res = new float[row][col];

        for (int i = 0; i < res.length; i++) {
            Arrays.fill(res[i], val);
        }

        return res;
    }

    protected static float[][] transpose_float_2d_array(float[][] arr) {
        int row = arr.length;
        int col = arr[0].length;

        float[][] res = get_float_2d_array(col, row, 0.0f);

        for (int y = 0; y < row; y++) {
            for (int x = 0; x < col; x++) {
                res[x][y] = arr[y][x];
            }
        }

        return res;
    }

    protected static float[][] multiply_float_2d_array(float[][] arr_1, float[][] arr_2) {
        int n = arr_1[0].length;
        int m = arr_1.length;
        int p = arr_2[0].length;
        float[][] res = new float[m][p];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                for (int k = 0; k < n; k++) {
                    res[i][j] += arr_1[i][k] * arr_2[k][j];
                }
            }
        }
        return res;
    }

    protected static float[][] clip(float[][] arr, float val_min, float val_max) {
        int row = arr.length;
        int col = arr[0].length;

        for (int y = 0; y < row; y++) {
            for (int x = 0; x < col; x++) {
                if (arr[y][x] < val_min) {
                    arr[y][x] = val_min;
                } else if (val_max < arr[y][x]) {
                    arr[y][x] = val_max;
                }
            }
        }
        return arr;
    }

    protected static float[][] elementwise_arithmetic_computation_2d(float[][] arr, float val_mul, float val_add) {
        int row = arr.length;
        int col = arr[0].length;

        for (int y = 0; y < row; y++) {
            for (int x = 0; x < col; x++) {
                arr[y][x] = val_mul * arr[y][x] + val_add;
            }
        }

        return arr;
    }

    protected static float[][] sqrt(float[][] arr) {
        int row = arr.length;
        int col = arr[0].length;

        for (int y = 0; y < row; y++) {
            for (int x = 0; x < col; x++) {
                arr[y][x] = (float) Math.sqrt((double) arr[y][x]);
            }
        }

        return arr;
    }

    protected static int[] argmin_axis_0(float[][] arr) {
        float val_min = 99999.999f;
        int idx_min = -1;
        int row = arr.length;
        int col = arr[0].length;
        int[] res = new int[col];

        for (int x = 0; x < col; x++) {
            val_min = 99999.999f;
            idx_min = -1;
            for (int y = 0; y < row; y++) {
                if (arr[y][x] < val_min) {
                    val_min = arr[y][x];
                    idx_min = y;
                }
            }
            res[x] = idx_min;
        }

        return res;
    }

    protected static int[] argmin_axis_1(float[][] arr) {
        float val_min = 99999.999f;
        int idx_min = -1;
        int row = arr.length;
        int col = arr[0].length;
        int[] res = new int[row];

        for (int y = 0; y < row; y++) {
            val_min = 99999.999f;
            idx_min = -1;
            for (int x = 0; x < col; x++) {
                if (arr[y][x] < val_min) {
                    val_min = arr[y][x];
                    idx_min = x;
                }
            }
            res[y] = idx_min;
        }

        return res;
    }

    protected static float[] min_axis_1(float[][] arr) {
        float val_min = 99999.999f;
        int row = arr.length;
        int col = arr[0].length;
        float[] res = new float[row];

        for (int y = 0; y < row; y++) {
            val_min = 99999.999f;
            for (int x = 0; x < col; x++) {
                if (arr[y][x] < val_min) {
                    val_min = arr[y][x];
                }
            }
            res[y] = val_min;
        }

        return res;
    }

    protected static int[] get_index_from_condition(float[] arr, float val, String condition) {
        int row = arr.length;
        int cnt = 0;

        for (int y = 0; y < row; y++) {
            if (condition.equals("less")) {
                if (arr[y] < val) {
                    cnt++;
                }
            }
        }

        int[] res = new int[cnt];
        cnt = 0;
        for (int y = 0; y < row; y++) {
            if (condition.equals("less")) {
                if (arr[y] < val) {
                    res[cnt] = y;
                    cnt++;
                }
            }
        }

        return res;
    }

    protected static boolean[] get_keep(float[] arr, float val) {
        int row = arr.length;
        boolean[] res = get_bool_1d_array(row, true);

        for (int y = 0; y < row; y++) {
            res[y] = (arr[y] < val);
        }

        return res;
    }

    protected static boolean[] get_keep_bi(int[] idx_1, int[] idx_2) {
        int idx_1_row = idx_1.length;
        boolean[] res = new boolean[idx_1_row];

        for (int y = 0; y < idx_1_row; y++) {
            res[y] = (y == idx_2[idx_1[y]]);
        }

        return res;
    }

    protected static boolean[] logical_and_for_two_arrays(boolean[] arr_1, boolean[] arr_2) {
        int arr_1_row = arr_1.length;
        boolean[] res = get_bool_1d_array(arr_1_row, true);

        for (int y = 0; y < arr_1_row; y++) {
            res[y] = (arr_1[y] && arr_2[y]);
        }

        return res;
    }

    protected static boolean[] logical_and_for_three_arrays(boolean[] arr_1, boolean[] arr_2, boolean[] arr_3) {
        int arr_1_row = arr_1.length;
        boolean[] res = get_bool_1d_array(arr_1_row, true);

        for (int y = 0; y < arr_1_row; y++) {
            res[y] = (arr_1[y] && arr_2[y] && arr_3[y]);
        }

        return res;
    }

    protected static int[] make_int_array_hit_condition(int[] arr, boolean[] idx) {
        int idx_row = idx.length;
        int cnt = 0;
        for (int y = 0; y < idx_row; y++) {
            if (idx[y]) {
                cnt++;
            }
        }

        int[] res = get_int_1d_array(cnt, 0);

        cnt = 0;
        for (int y = 0; y < idx_row; y++) {
            if (idx[y]) {
                res[cnt] = arr[y];
                cnt++;
            }
        }

        return res;
    }

    protected static float[] make_float_array_hit_condition(float[] arr, boolean[] idx) {
        int idx_row = idx.length;
        int cnt = 0;
        for (int y = 0; y < idx_row; y++) {
            if (idx[y]) {
                cnt++;
            }
        }

        float[] res = get_float_1d_array(cnt, 0.0f);

        cnt = 0;
        for (int y = 0; y < idx_row; y++) {
            if (idx[y]) {
                res[cnt] = arr[y];
                cnt++;
            }
        }

        return res;
    }

    protected static int sum_int_1d_array(int[] arr) {
        int row = arr.length;
        int cnt = 0;

        for (int y = 0; y < row; y++) {
            cnt += arr[y];
        }

        return cnt;
    }

    protected static int sum_bool_1d_array(boolean[] arr) {
        int row = arr.length;
        int cnt = 0;

        for (int y = 0; y < row; y++) {
            if (arr[y]) {
                cnt++;
            }
        }

        return cnt;
    }

    protected static float[][] get_float_2d_sub_array(float[][] arr, int obj, int axis) {
        int row = arr.length;
        int col = arr[0].length;


        int new_idx = 0;
        float[][] res = null;
        if (axis == 0) {
            res = get_float_2d_array(row - 1, col, 0.0f);
        }
        else if (axis == 1) {
            res = get_float_2d_array(row, col - 1, 0.0f);
        }
        else {
            // Log.e("PointTracker", "The given axis is not supported.");
            throw new IllegalArgumentException("The given axis is not supported.");
        }


        if (axis == 0) {
            for (int y = 0; y < row; y++) {
                if (y == obj) {
                    continue;
                }
                for (int x = 0; x < col; x++) {
                    res[new_idx][x] = arr[y][x];
                }
                new_idx++;
            }
        }
        else {
            // if (axis == 1)
            for (int x = 0; x < col; x++) {
                if (x == obj) {
                    continue;
                }
                for (int y = 0; y < row; y++) {
                    res[y][new_idx] = arr[y][x];
                }
                new_idx++;
            }
        }

        return res;
    }

    protected static int[][] get_int_2d_sub_array(int[][] arr, int obj, int axis) {
        int row = arr.length;
        int col = arr[0].length;
        int new_idx = 0;
        int[][] res = null;
        if (axis == 0) {
            res = get_int_2d_array(row - 1, col, 0);
        }
        else if (axis == 1) {
            res = get_int_2d_array(row, col - 1, 0);
        }
        else {
            // Log.e("PointTracker", "The given axis is not supported.");
            throw new IllegalArgumentException("The given axis is not supported.");
        }


        if (axis == 0) {
            for (int y = 0; y < row; y++) {
                if (y == obj) {
                    continue;
                }
                for (int x = 0; x < col; x++) {
                    res[new_idx][x] = arr[y][x];
                }
                new_idx++;
            }
        }
        else {
            // if (axis == 1)
            for (int x = 0; x < col; x++) {
                if (x == obj) {
                    continue;
                }
                for (int y = 0; y < row; y++) {
                    res[y][new_idx] = arr[y][x];
                }
                new_idx++;
            }
        }

        return res;
    }

    protected static float[][] hstack_2d_float_array(float[][] arr_1, float[][] arr_2) {
        int row_arr_1 = arr_1.length;
        int col_arr_1 = arr_1[0].length;
        int row_arr_2 = arr_2.length;
        int col_arr_2 = arr_2[0].length;
        int new_x = 0;

        if (row_arr_1 != row_arr_2) {
            // Log.e("PointTracker", "row_arr_1 != row_arr_2");
            throw new IllegalArgumentException("row_arr_1 != row_arr_2");
        }

        float[][] res = get_float_2d_array(row_arr_1, (col_arr_1 + col_arr_2), 0.0f);

        for (int y = 0; y < row_arr_1; y++) {
            new_x = 0;
            for (int x = 0; x < col_arr_1; x++) {
                res[y][new_x] = arr_1[y][x];
                new_x++;
            }
            for (int x = 0; x < col_arr_2; x++) {
                res[y][new_x] = arr_2[y][x];
                new_x++;
            }
        }

        return res;
    }

    protected static float[][] vstack_2d_float_array(float[][] arr_1, float[][] arr_2) {
        int row_arr_1 = arr_1.length;
        int col_arr_1 = arr_1[0].length;
        int row_arr_2 = arr_2.length;
        int col_arr_2 = arr_2[0].length;
        int new_y = 0;

        if (col_arr_1 != col_arr_2) {
            // Log.e("PointTracker", "col_arr_1 != col_arr_2");
            throw new IllegalArgumentException("col_arr_1 != col_arr_2");
        }

        float[][] res = get_float_2d_array((row_arr_1 + row_arr_2), col_arr_1, 0.0f);

        for (int x = 0; x < col_arr_1; x++) {
            new_y = 0;
            for (int y = 0; y < row_arr_1; y++) {
                res[new_y][x] = arr_1[y][x];
                new_y++;
            }
            for (int y = 0; y < row_arr_2; y++) {
                res[new_y][x] = arr_2[y][x];
                new_y++;
            }
        }

        return res;
    }

    protected static int get_num_true_from_1d_boolean_array(boolean[] arr) {
        int row = arr.length;
        int res = 0;

        for (int i = 0; i < row; i++) {
            if (arr[i]) {
                res++;
            }
        }

        return res;
    }

    public static void print_float_2d_array(float[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                System.out.printf("%f ", arr[i][j]);
            }
            System.out.println();
        }
    }

    public static void print_boolean_2d_array(boolean[][] _arr) {
        for (int i = 0; i < _arr.length; i++) {
            for (int j = 0; j < _arr[0].length; j++) {
                System.out.printf("%b ", _arr[i][j]);
            }
            System.out.println();
        }
    }

    public static void print_float_1d_array(float[] _arr) {
        for (int i = 0; i < _arr.length; i++) {
            System.out.printf("%f ", _arr[i]);
        }
        System.out.println();
    }

    public static void print_int_1d_array(int[] _arr) {
        for (int i = 0; i < _arr.length; i++) {
            System.out.printf("%d ", _arr[i]);
        }
        System.out.println();
    }

    public static void print_boolean_1d_array(boolean[] _arr) {
        for (int i = 0; i < _arr.length; i++) {
            System.out.printf("%b ", _arr[i]);
        }
        System.out.println();
    }
}

