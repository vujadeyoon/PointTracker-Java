import struct.PTS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
// Android
// import android.util.Log;
// Java
import java.util.logging.Level;
import java.util.logging.Logger;


public class PointTracker extends MatrixOperator{
    private static int maxl = 0;
    private static float nn_thresh = 0.0f;
    private static ArrayList<ArrayList<PTS>> all_pts = new ArrayList<>();
    private static ArrayList<PTS> init_all_pts = new ArrayList<>();
    private static float[][] last_desc;
    private static float[][] tracks;
    private static int track_count;
    private static int max_score;
    private final static Logger Log = Logger.getGlobal();
    private static boolean is_debug;

    public PointTracker(int _max_length, float _nn_thresh, boolean _is_debug) throws IOException {
        // Java
        Log.setLevel(Level.WARNING);

        is_debug = _is_debug;

        if (_max_length < 2) {
            throw new IllegalArgumentException("max_length must be greater than or equal to 2.");
        }

        maxl = _max_length;
        nn_thresh = _nn_thresh;
        for(int i=0; i<maxl; i++) {
            init_all_pts.clear();
            init_all_pts.add(new PTS(-1.0f, -1, -1));
            all_pts.add(init_all_pts); // crucial
        }
        tracks = get_float_2d_array(0, maxl+2, 0.0f); // crucial
        track_count = 0;
        max_score = 9999;

        if (is_debug) {
            System.out.println("deb-0" + all_pts + all_pts.size());
            Util.pause();
        }
    }

    public static int get_num_all_pts_idx(int _idx) {
        int res = 0;
        if (0.0f <= all_pts.get(_idx).get(0).score) {
            res = all_pts.get(_idx).size();
        }
        return res;
    }

    public static int[] get_offsets() {
        /*
        Iterate through list of points and accumulate an offset value. Used to
        index the global point IDs into the list of points.
        Returns
            offsets - N length array with integer offset locations.
        */
        int n = all_pts.size();
        int[] offsets = get_int_1d_array(n, 0);
        int[] res = get_int_1d_array(n, 0);

        if (is_debug) {
            System.out.printf("go-1: %d\n", res.length);
        }

        for (int y = 0; y < (offsets.length - 1); y++) {
            if (is_debug) {
                System.out.printf("go-2: %d\n", get_num_all_pts_idx(y));
            }
            offsets[y + 1] = get_num_all_pts_idx(y);
        }

        res[0] = offsets[0];
        for (int y = 1; y < res.length; y++) {
            res[y] = (res[y - 1] + offsets[y]);
        }

        return res;
    }

    public static float[][] nn_match_two_way(float[][] desc1, float[][] desc2, float nn_thresh) throws IOException {
        /*
        Performs two-way nearest neighbor matching of two sets of descriptors, such
        that the NN match from descriptor A->B must equal the NN match from B->A.
        Inputs:
            desc1 - NxM numpy matrix of N corresponding M-dimensional descriptors.
            desc2 - NxM numpy matrix of N corresponding M-dimensional descriptors.
            nn_thresh - Optional descriptor distance below which is a good match.
        Returns:
            matches - 3xL numpy array, of L matches, where L <= N and each column i is
                      a match of two descriptors, d_i in image 1 and d_j' in image 2:
                      [d_i index, d_j' index, match_score]^T
        */

        if (desc1.length != desc2.length) {
            throw new IllegalArgumentException("The number of rows for desc1 and desc2 should be same.");
        }

        if ((desc1[0].length == 0) || (desc2[0].length == 0)) {
            return get_float_2d_array(3, 0, 0.0f); // crucial
        }

        if (nn_thresh < 0.0f) {
            throw new IllegalArgumentException("nn_thresh should be non-negative.");

        }

        if (is_debug) {
            System.out.println("[nn_match_two_way] START");
        }

        // Compute L2 distance. Easy since vectors are unit normalized.
        float[][] dmat = sqrt(elementwise_arithmetic_computation_2d(clip(multiply_float_2d_array(transpose_float_2d_array(desc1), desc2), -1.0f, 1.0f), -2, 2));

        if (is_debug) {
            System.out.printf("[nn_match_two_way] dmat: %dx%d\n", dmat.length, dmat[0].length);
            MatrixOperator.print_float_2d_array(dmat);
            Util.pause();
        }

        // Get NN indices and scores.
        int[] idx = argmin_axis_1(dmat);
        float[] scores = min_axis_1(dmat);

        // Threshold the NN matches.
        boolean[] keep = get_keep(scores, nn_thresh);

        if (is_debug) {
            System.out.printf("[nn_match_two_way] idx: %d\n", idx.length);
            MatrixOperator.print_int_1d_array(idx);
            Util.pause();

            System.out.printf("[nn_match_two_way] scores: %d\n", scores.length);
            MatrixOperator.print_float_1d_array(scores);
            Util.pause();

            System.out.printf("[nn_match_two_way] keep: %d\n", keep.length);
            MatrixOperator.print_boolean_1d_array(keep);
            Util.pause();
        }

        // Check if nearest neighbor goes both directions and keep those.
        int[] idx2 = argmin_axis_0(dmat);
        boolean[] keep_bi = get_keep_bi(idx, idx2);

        if (is_debug) {
            System.out.printf("[nn_match_two_way] idx2: %d\n", idx2.length);
            MatrixOperator.print_int_1d_array(idx2);
            Util.pause();

            System.out.printf("[nn_match_two_way] keep_bi: %d\n", keep_bi.length);
            MatrixOperator.print_boolean_1d_array(keep_bi);
            Util.pause();
        }

        keep = logical_and_for_two_arrays(keep, keep_bi);
        idx = make_int_array_hit_condition(idx, keep);
        scores = make_float_array_hit_condition(scores, keep);

        if (is_debug) {
            System.out.printf("[nn_match_two_way] idx: %d\n", idx.length);
            MatrixOperator.print_int_1d_array(idx);
            Util.pause();

            System.out.printf("[nn_match_two_way] scores: %d\n", scores.length);
            MatrixOperator.print_float_1d_array(scores);
            Util.pause();

            System.out.printf("[nn_match_two_way] keep: %d\n", keep.length);
            MatrixOperator.print_boolean_1d_array(keep);
            Util.pause();
        }



        // Get the surviving point indices.
        int[] m_idx1 = make_int_array_hit_condition(get_int_1d_arange(desc1[0].length), keep);
        int[] m_idx2 = idx; // shallow-copy; deep-copy: idx.clone();

        if (is_debug) {
            System.out.printf("[nn_match_two_way] m_idx1: %d\n", m_idx1.length);
            MatrixOperator.print_int_1d_array(m_idx1);
            Util.pause();

            System.out.printf("[nn_match_two_way] m_idx2: %d\n", m_idx2.length);
            MatrixOperator.print_int_1d_array(m_idx2);
            Util.pause();

            System.out.printf("[nn_match_two_way] sum_bool_1d_array(keep): %d\n", sum_bool_1d_array(keep));
            Util.pause();
        }

        // Populate the final 3xN match data structure.
        float[][] matches = get_float_2d_array(3, sum_bool_1d_array(keep), 0.0f);
        for (int x = 0; x < matches[0].length; x++) {
            matches[0][x] = m_idx1[x];
            matches[1][x] = m_idx2[x];
            matches[2][x] = scores[x];
        }

        return matches;
    }

    public static void update(ArrayList<PTS> _pts, float[][] _desc, boolean _is_first) throws IOException {
        /*
        Add a new set of point and descriptor observations to the tracker.

        Inputs
            _pts - 3xN numpy array of 2D point observations.
            _desc - DxN numpy array of corresponding D dimensional descriptors.
        */

        Queue<Integer> temp_queue = new LinkedList<>();
        int temp_cnt = 0;
        float track_len = 0.0f;

        if ((_pts == null) || (_desc == null)) {
            // Log.e("PointTracker", "Warning, no points were added to tracker.");
            Log.warning("Warning, no points were added to tracker.");
            throw new IllegalArgumentException("Warning, no points were added to tracker.");
            // reuturn;
        }

        if (_pts.size() != _desc[0].length) {
            // Log.e("PointTracker", "assert pts.shape[1] == _desc.shape[1]");
            Log.warning("assert pts.shape[1] == _desc.shape[1]");
            throw new IllegalArgumentException("assert pts.shape[1] == _desc.shape[1]");
            // return;
        }

        // Initialize last_desc.
        if (last_desc == null) {
            Log.warning("last_desc should not be null.");
            // throw new IllegalArgumentException("last_desc should not be null.");
            last_desc = get_float_2d_array(_desc.length, 0, 0.0f); // crucial
        }

        //  Remove oldest points, store its size to update ids later.
        int remove_size = get_num_all_pts_idx(0);

        if (is_debug) {
            System.out.println("deb; " + all_pts);
        }

        all_pts.remove(0);
        all_pts.add(_pts);

        if (is_debug) {
            System.out.printf("deb-1; %d %d %d %d %f", remove_size, all_pts.size(), all_pts.get(0).get(0).x, all_pts.get(0).get(0).y, all_pts.get(0).get(0).score);
            Util.pause();

            System.out.println("tracks-1");
            MatrixOperator.print_float_2d_array(tracks);
            Util.pause();
        }

        if (_is_first) {
            int[] offsets = get_offsets();
            if (is_debug) {
                System.out.printf("deb-4-1; %d\n", offsets.length);
                MatrixOperator.print_int_1d_array(offsets);
            }


            boolean[] matched = get_bool_1d_array(_pts.size(), false);

            // Add unmatched tracks.
            // new_ids = np.arange(pts.shape[1]) + offsets[-1]
            int[] temp_new_ids = get_int_1d_array(_pts.size(), 0);
            for (int i = 0; i < temp_new_ids.length; i++) {
                temp_new_ids[i] = i + offsets[offsets.length - 1];
            }

            // new_ids = new_ids[~matched]
            temp_queue.clear();
            for (int i = 0; i < matched.length; i++) {
                if (!matched[i]) { // (matched[i] == false)
                    temp_queue.add(i);
                }
            }

            int[] new_ids = get_int_1d_array(temp_queue.size(), 0);
            temp_cnt = 0;
            while (!temp_queue.isEmpty()) {
                new_ids[temp_cnt] = temp_queue.poll();
                temp_cnt++;
            }

            // new_tracks = -1*np.ones((new_ids.shape[0], self.maxl + 2))
            // new_tracks[:, -1] = new_ids
            float[][] new_tracks = get_float_2d_array(new_ids.length, maxl + 2, -1.0f);

            if (is_debug) {
                System.out.println(new_ids.length); // 79
                System.out.println(maxl + 2); // 7
                System.out.println(new_tracks.length - 1); // 78
                System.out.println(new_tracks[0].length - 1); // 6
            }

            for (int i = 0; i < new_tracks.length; i++) {
                new_tracks[i][new_tracks[0].length - 1] = (float) new_ids[i];
            }

            int new_num = new_ids.length;

            float[] new_trackids = get_float_1d_array(new_num, 0.0f);
            for (int i = 0; i < new_trackids.length; i++) {
                new_trackids[i] = ((float) i + (float) track_count);
            }

            for (int i = 0; i < new_tracks.length; i++) {
                new_tracks[i][0] = new_trackids[i];
                new_tracks[i][1] = max_score;
            }

            tracks = new_tracks;
            track_count += new_num; // Update the track count.

            // Remove empty tracks.
            // keep_rows = np.any(self.tracks[:, 2:] >= 0, axis=1);
            boolean[] keep_rows = get_bool_1d_array(tracks.length, false);
            for (int i = 0; i < tracks.length; i++) {
                for (int j = 2; j < tracks[0].length; j++) {
                    if (tracks[i][j] >= 0.0f) {
                        keep_rows[i] = true;
                        continue;
                    }
                }
            }

            // self.tracks = self.tracks[keep_rows, :]
            float[][] temp_tracks = tracks.clone();
            int num_hit = get_num_true_from_1d_boolean_array(keep_rows);
            tracks = get_float_2d_array(num_hit, temp_tracks[0].length, 0.0f);
            temp_cnt = 0;
            for (int i = 0; i < keep_rows.length; i++) {
                if (keep_rows[i]) {
                    for (int j = 0; j < tracks[0].length; j++) {
                        tracks[temp_cnt][j] = temp_tracks[i][j];
                    }
                    temp_cnt++;
                }
            }

            // Store the last descriptors.
            last_desc = _desc.clone();

            if (is_debug) {
                System.out.printf("deb-7-1: %d %d\n", tracks.length, tracks[0].length);
                System.out.printf("deb-7-2: %d %d\n", last_desc.length, last_desc[0].length);
                Util.pause();
                MatrixOperator.print_float_2d_array(tracks);
                MatrixOperator.print_float_2d_array(last_desc);
                Util.pause();
            }
        }
        else {
            // Remove oldest point in track.
            tracks = get_float_2d_sub_array(tracks, 2, 1);

            if (is_debug) {
                System.out.printf("tracks-2: %dx%d\n", tracks.length, tracks[0].length);
                MatrixOperator.print_float_2d_array(tracks);
                Util.pause();
            }

            // Update track offsets.
            for (int i = 2; i < tracks[0].length; i++) {
                for (int j = 0; j < tracks.length; j++) {
                    tracks[j][i] -= (float) remove_size;
                }
            }

            // self.tracks[:, 2:][self.tracks[:, 2:] < -1] = -1
            for (int i = 0; i < tracks.length; i++) {
                for (int j = 2; j < tracks[0].length; j++) {
                    if (tracks[i][j] < -1) {
                        tracks[i][j] = -1;
                    }
                }
            }

            if (is_debug) {
                System.out.printf("tracks-3: %dx%d\n", tracks.length, tracks[0].length);
                MatrixOperator.print_float_2d_array(tracks);
                Util.pause();
            }

            int[] offsets = get_offsets();

            if (is_debug) {
                System.out.printf("deb-4-1: %d\n", offsets.length);
                MatrixOperator.print_int_1d_array(offsets);
                Util.pause();
            }

            // Add a new -1 column.
            tracks = hstack_2d_float_array(tracks, get_float_2d_array(tracks.length, 1, -1.0f));

            if (is_debug) {
                System.out.printf("deb-4-2: %dx%d\n", tracks.length, tracks[0].length);
                MatrixOperator.print_float_2d_array(tracks);
                Util.pause();
            }

            // Try to append to existing tracks.
            boolean[] matched = get_bool_1d_array(_pts.size(), false);
            float[][] matches = nn_match_two_way(last_desc, _desc, nn_thresh);

            float[][] matches_T = transpose_float_2d_array(matches);

            if (is_debug) {
                System.out.printf("matched: %d\n", matched.length);
                MatrixOperator.print_boolean_1d_array(matched);
                Util.pause();

                System.out.printf("matches: %dx%d\n", matches.length, matches[0].length);
                MatrixOperator.print_float_2d_array(matches);
                Util.pause();

                System.out.printf("matches_T: %dx%d\n", matches_T.length, matches_T[0].length);
                MatrixOperator.print_float_2d_array(matches_T);
                Util.pause();
            }

            for (int i = 0; i < matches_T.length; i++) {
                int id1 = (int) matches_T[i][0] + offsets[offsets.length - 2];
                int id2 = (int) matches_T[i][1] + offsets[offsets.length - 1];

                // found = np.argwhere(self.tracks[:, -2] == id1)
                temp_cnt = 0;
                for (int j = 0; j < tracks.length; j++) {
                    if (tracks[j][tracks[0].length - 2] == (float) id1) { //@sjyoon
                        temp_cnt++;
                    }
                }

                int[] found = get_int_1d_array(temp_cnt, 0);
                temp_cnt = 0;
                for (int j = 0; j < tracks.length; j++) {
                    if (tracks[j][tracks[0].length - 2] == (float) id1) { //@sjyoon
                        found[temp_cnt] = j;
                    }
                }

                if (found.length == 1) {
                    matched[(int) matches_T[i][1]] = true;

                    int row = found[0]; // row = int(found)
                    tracks[row][tracks[0].length - 1] = (float) id2; //@sjyoon

                    if (tracks[row][1] == max_score) {
                        // Initialize track score.
                        tracks[row][1] = matches_T[i][2];
                    } else {
                    /*
                    Update track score with running average.
                    NOTE(dd): this running average can contain scores from old matches
                    not contained in last max_length track points.
                    */

                        // track_len = (self.tracks[row, 2:] != -1).sum() - 1.
                        track_len = -1.0f;
                        for (int j = 2; j < tracks[0].length; j++) {
                            if (tracks[row][j] != -1.0) {
                                track_len += tracks[row][j];
                            }
                        }
                        float frac = 1.0f / track_len;
                        tracks[row][1] = (1.0f - frac) * tracks[row][1] + frac * matches_T[i][2];
                    }
                }
            }

            if (is_debug) {
                System.out.printf("tracks-4: %dx%d\n", tracks.length, tracks[0].length);
                MatrixOperator.print_float_2d_array(tracks);
                Util.pause();
            }

            // Add unmatched tracks.
            // new_ids = np.arange(pts.shape[1]) + offsets[-1]
            int[] temp_new_ids = get_int_1d_array(_pts.size(), 0);
            for (int i = 0; i < temp_new_ids.length; i++) {
                temp_new_ids[i] = i + offsets[offsets.length - 1];
            }

            // new_ids = new_ids[~matched]
            temp_queue.clear();
            for (int i = 0; i < matched.length; i++) {
                if (!matched[i]) { // (matched[i] == false)
                    temp_queue.add(i);
                }
            }

            int[] new_ids = get_int_1d_array(temp_queue.size(), 0);
            temp_cnt = 0;
            while (!temp_queue.isEmpty()) {
                new_ids[temp_cnt] = temp_queue.poll();
                temp_cnt++;
            }

            // new_tracks = -1*np.ones((new_ids.shape[0], self.maxl + 2))
            float[][] new_tracks = get_float_2d_array(new_ids.length, maxl + 2, -1.0f);

            if (is_debug) {
                System.out.printf("new_ids.length: %d\n", new_ids.length);
                System.out.printf("maxl: %d\n", maxl);
                System.out.printf("new_tracks-1: %dx%d\n", new_tracks.length, new_tracks[0].length);
            }

            for (int i = 0; i < new_tracks.length; i++) {
                new_tracks[i][new_tracks[0].length - 1] = (float) new_ids[i]; //@sjyoon
            }

            int new_num = new_ids.length;

            float[] new_trackids = get_float_1d_array(new_num, 0.0f);
            for (int i = 0; i < new_trackids.length; i++) {
                new_trackids[i] = ((float) i + (float) track_count);
            }

            for (int i = 0; i < new_tracks.length; i++) {
                new_tracks[i][0] = new_trackids[i];
                new_tracks[i][1] = max_score;
            }

            if (new_tracks.length != 0) {
                tracks = vstack_2d_float_array(tracks, new_tracks);
            }

            track_count += new_num; // Update the track count.

            // Remove empty tracks.
            // keep_rows = np.any(self.tracks[:, 2:] >= 0, axis=1);
            boolean[] keep_rows = get_bool_1d_array(tracks.length, false);
            for (int i = 0; i < tracks.length; i++) {
                for (int j = 2; j < tracks[0].length; j++) {
                    if (tracks[i][j] >= 0.0f) {
                        keep_rows[i] = true;
                        continue;
                    }
                }
            }

            // self.tracks = self.tracks[keep_rows, :]
            float[][] temp_tracks = tracks.clone();
            int num_hit = get_num_true_from_1d_boolean_array(keep_rows);
            tracks = get_float_2d_array(num_hit, temp_tracks[0].length, 0.0f);
            temp_cnt = 0;
            for (int i = 0; i < keep_rows.length; i++) {
                if (keep_rows[i]) {
                    for (int j = 0; j < tracks[0].length; j++) {
                        tracks[temp_cnt][j] = temp_tracks[i][j];
                    }
                    temp_cnt++;
                }
            }

            // Store the last descriptors.
            last_desc = _desc.clone();

            if (is_debug) {
                Util.pause();
                System.out.printf("[Final] tracks: %dx%d\n", tracks.length, tracks[0].length);
                MatrixOperator.print_float_2d_array(tracks);
                Util.pause();

                System.out.printf("[Final] last_desc: %dx%d\n", last_desc.length, last_desc[0].length);
                MatrixOperator.print_float_2d_array(last_desc);
                Util.pause();
            }
        }
    }

    public float[][] get_tracks(int _min_length, boolean _is_first) throws IOException {
        /*
        Retrieve point tracks of a given minimum length.
        Input
            min_length - integer >= 1 with minimum track length
        Output
            returned_tracks - M x (2+L) sized matrix storing track indices, where
            M is the number of tracks and L is the maximum track length.
        */

        int temp_cnt = 0;

        if (_min_length < 1) {
            // Log.e("PointTracker", "min_length is too small.");
            Log.warning("min_length is too small.");
            throw new IllegalArgumentException("min_length is too small.");
        }

        boolean[] valid = get_bool_1d_array(tracks.length, true);

        // good_len = np.sum(self.tracks[:, 2:] != -1, axis=1) >= min_length;
        boolean[] good_len = get_bool_1d_array(tracks.length, false);
        for (int i = 0; i < tracks.length; i++) {
            temp_cnt = 0;
            for (int j = 2; j < tracks[0].length; j++) {
                if (tracks[i][j] != -1) {
                    temp_cnt++;
                }
            }
            if (temp_cnt >= _min_length) {
                good_len[i] = true;
            }
        }

        if (is_debug) {
            System.out.printf("[get_tracks] good_len: %d.\n", good_len.length);
            MatrixOperator.print_boolean_1d_array(good_len);
            Util.pause();
        }

        // Remove tracks which do not have an observation in most recent frame.
        // not_headless = (self.tracks[:, -1] != -1)
        boolean[] not_headless = get_bool_1d_array(tracks.length, false);
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i][tracks[0].length - 1] != -1) {
                not_headless[i] = true;
            }
        }

        if (is_debug) {
            System.out.printf("[get_tracks] not_headless: %d.\n", not_headless.length);
            MatrixOperator.print_boolean_1d_array(not_headless);
            Util.pause();
        }

        // keepers = np.logical_and.reduce((valid, good_len, not_headless))
        boolean[] keepers = logical_and_for_three_arrays(valid, good_len, not_headless);

        if (is_debug) {
            System.out.printf("[get_tracks] keepers: %d.\n", keepers.length);
            MatrixOperator.print_boolean_1d_array(keepers);
            Util.pause();

            System.out.printf("[get_tracks] get_num_true_from_1d_boolean_array(keepers): %d.\n", get_num_true_from_1d_boolean_array(keepers)); // 79
            System.out.printf("[get_tracks] tracks[0].length: %d.\n", tracks[0].length); // 7
            System.out.printf("[get_tracks] tracks: %dx%d.\n", tracks.length, tracks[0].length); // 79x7
        }

        // returned_tracks = self.tracks[keepers, :].copy()
        float[][] returned_tracks = get_float_2d_array(get_num_true_from_1d_boolean_array(keepers), tracks[0].length, 0.0f); // 79x7
        temp_cnt = 0;
        for (int i = 0; i < keepers.length; i++) {
            if (keepers[i]) { // 79
                for (int j = 0; j < tracks[0].length; j++) {
                    returned_tracks[temp_cnt][j] = tracks[i][j];
                }
                temp_cnt++;
            }
        }

        if (is_debug) {
            if (_is_first) {
                System.out.printf("[get_tracks] returned_tracks: %dx%d.\n", get_num_true_from_1d_boolean_array(keepers), tracks[0].length);
            }
            else {
                System.out.printf("[get_tracks] returned_tracks: %dx%d.\n", returned_tracks.length, returned_tracks[0].length);
                MatrixOperator.print_float_2d_array(returned_tracks);
            }
            Util.pause();
        }

        return returned_tracks;
    }
}

