import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
// Android
// import android.util.Log;
// Java
import java.util.logging.Level;
import java.util.logging.Logger;


public class PointTracker extends MatrixOperator{
    private int maxl = 0;
    private float nn_thresh = 0.0f;
    private ArrayList<int[][]> all_pts = new ArrayList<int[][]>();
    private float[][] last_desc;
    private float[][] tracks;
    private int track_count;
    private int max_score;
    private final static Logger Log = Logger.getGlobal();

    public PointTracker(int max_length, float nn_thresh) {
        // Java
        Log.setLevel(Level.WARNING);

        if (max_length < 2) {
            throw new IllegalArgumentException("max_length must be greater than or equal to 2.");
        }

        this.maxl = max_length;
        this.nn_thresh = nn_thresh;
        for(int i=0; i<this.maxl; i++) {
            this.all_pts.add(new int[2][0]); // crucial
        }
        this.tracks = get_float_2d_array(0, this.maxl+2, 0.0f); // crucial
        this.track_count = 0;
        this.max_score = 9999;
    }

    private int[] get_offsets() {
        /*
        Iterate through list of points and accumulate an offset value. Used to
        index the global point IDs into the list of points.
        Returns
            offsets - N length array with integer offset locations.
        */
        int n = this.all_pts.size();
        int[] offsets = get_int_1d_array(n, 0);
        int[] res = get_int_1d_array(n, 0);
        for (int y = 0; y < (offsets.length - 1); y++) {
            offsets[y] = this.all_pts.get(y)[0].length;
        }

        res[0] = offsets[0];
        for (int y = 1; y < res.length; y++) {
            res[y] = (res[y - 1] + offsets[y]);
        }

        return res;
    }

    private float[][] nn_match_two_way(float[][] desc1, float[][] desc2, float nn_thresh) {
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

        // Compute L2 distance. Easy since vectors are unit normalized.
        float[][] dmat = sqrt(elementwise_arithmetic_computation_2d(clip(multiply_float_2d_array(transpose_float_2d_array(desc1), desc2), -1.0f, 1.0f), -2, 2));

        // Get NN indices and scores.
        int[] idx = argmin_axis_1(dmat);
        float[] scores = min_axis_1(dmat);

        // Threshold the NN matches.
        boolean[] keep = get_keep(scores, nn_thresh);

        // Check if nearest neighbor goes both directions and keep those.
        int[] idx2 = argmin_axis_0(dmat);
        boolean[] keep_bi = get_keep_bi(idx, idx2);
        keep = logical_and_for_two_arrays(keep, keep_bi);
        idx = make_int_array_hit_condition(idx, keep);
        scores = make_float_array_hit_condition(scores, keep);

        // Get the surviving point indices.
        int[] m_idx1 = make_int_array_hit_condition(get_int_1d_arange(desc1[0].length), keep);
        int[] m_idx2 = idx; // shallow-copy; deep-copy: idx.clone();

        // Populate the final 3xN match data structure.
        float[][] matches = get_float_2d_array(3, sum_bool_1d_array(keep), 0.0f);
        for (int x = 0; x < matches[0].length; x++) {
            matches[0][x] = m_idx1[x];
            matches[1][x] = m_idx2[x];
            matches[2][x] = scores[x];
        }

        return matches;
    }

    private void update(int[][] pts, float[][] desc) {
        /*
        Add a new set of point and descriptor observations to the tracker.

        Inputs
            pts - 3xN numpy array of 2D point observations.
            desc - DxN numpy array of corresponding D dimensional descriptors.
        */

        Queue<Integer> temp_queue = new LinkedList<>();
        int temp_cnt = 0;
        float track_len = 0.0f;


        if ((pts == null) || (desc == null)) {
            // Log.e("PointTracker", "Warning, no points were added to tracker.");
            Log.warning("Warning, no points were added to tracker.");
            return;
        }

        if (pts[0].length != desc[0].length) {
            // Log.e("PointTracker", "assert pts.shape[1] == desc.shape[1]");
            Log.warning("assert pts.shape[1] == desc.shape[1]");
            return;
        }

        // Initialize last_desc.
        if (this.last_desc == null) {
            this.last_desc = get_float_2d_array(desc.length, 0, 0.0f); // crucial
        }

        //  Remove oldest points, store its size to update ids later.
        int remove_size = this.all_pts.get(0)[0].length;
        this.all_pts.remove(0);
        this.all_pts.add(pts);

        // Remove oldest point in track.
        this.tracks = get_float_2d_sub_array(this.tracks, 2, 1);

        // Update track offsets.
        for (int i = 2; i < this.tracks[0].length; i++) {
            for (int j = 0; j < this.tracks.length; j++) {
                this.tracks[j][i] -= (float) remove_size;
            }
        }

        // self.tracks[:, 2:][self.tracks[:, 2:] < -1] = -1
        for (int i = 0; i < tracks.length; i++) {
            for (int j = 2; j < tracks[0].length; j++) {
                if (this.tracks[i][j] < -1) {
                    this.tracks[i][j] = -1;
                }
            }
        }

        int[] offsets = this.get_offsets();

        // Add a new -1 column.
        this.tracks = hstack_2d_float_array(this.tracks, get_float_2d_array(this.tracks.length, 1, -1.0f));

        // Try to append to existing tracks.
        boolean[] matched = get_bool_1d_array(pts[0].length, false);
        float[][] matches = nn_match_two_way(this.last_desc, desc, this.nn_thresh);

        float[][] matches_T = transpose_float_2d_array(matches);

        for (int i = 0; i < matches_T.length; i++) {
            int id1 = (int) matches_T[i][0] + offsets[offsets.length - 2];
            int id2 = (int) matches_T[i][1] + offsets[offsets.length - 1];

            // found = np.argwhere(self.tracks[:, -2] == id1)
            temp_cnt = 0;
            for (int j = 0; j < this.tracks.length; j++) {
                if (this.tracks[j][this.tracks.length - 2] == (float) id1) {
                    temp_cnt++;
                }
            }

            int[] found = get_int_1d_array(temp_cnt, 0);
            temp_cnt = 0;
            for (int j = 0; j < this.tracks.length; j++) {
                if (this.tracks[j][this.tracks.length - 2] == (float) id1) {
                    found[temp_cnt] = j;
                }
            }

            if (found.length == 1) {
                matched[(int) matches_T[i][1]] = true;

                int row = found[0]; // row = int(found)
                this.tracks[row][this.tracks.length - 1] = (float) id2;

                if (this.tracks[row][1] == this.max_score) {
                    // Initialize track score.
                    this.tracks[row][1] = matches_T[i][2];
                } else {
                    /*
                    Update track score with running average.
                    NOTE(dd): this running average can contain scores from old matches
                    not contained in last max_length track points.
                    */

                    // track_len = (self.tracks[row, 2:] != -1).sum() - 1.
                    track_len = -1.0f;
                    for (int j = 2; j < this.tracks[0].length; j++) {
                        if (this.tracks[row][j] != -1.0) {
                            track_len += this.tracks[row][j];
                        }
                    }
                    float frac = 1.0f / track_len;
                    this.tracks[row][1] = (1.0f - frac) * this.tracks[row][1] + frac * matches_T[i][2];
                }
            }
        }

        // Add unmatched tracks.
        // new_ids = np.arange(pts.shape[1]) + offsets[-1];
        int[] temp_new_ids = get_int_1d_array(pts[0].length, 0);
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
        float[][] new_tracks = get_float_2d_array(new_ids.length, this.maxl + 2, -1.0f);
        for (int i = 0; i < new_tracks.length; i++) {
            new_tracks[i][new_tracks.length - 1] = (float) new_ids[i];
        }

        int new_num = new_ids.length;

        float[] new_trackids = get_float_1d_array(new_num, 0.0f);
        for (int i = 0; i < new_trackids.length; i++) {
            new_trackids[i] = ((float) i + (float) this.track_count);
        }

        for (int i = 0; i < new_tracks.length; i++) {
            new_tracks[i][0] = new_trackids[i];
            new_tracks[i][1] = this.max_score;
        }

        this.tracks = vstack_2d_float_array(this.tracks, new_tracks);
        this.track_count += new_num; // Update the track count.

        // Remove empty tracks.
        // keep_rows = np.any(self.tracks[:, 2:] >= 0, axis=1);
        boolean[] keep_rows = get_bool_1d_array(this.tracks.length, false);
        for (int i = 0; i < this.tracks.length; i++) {
            for (int j = 2; j < this.tracks[0].length; j++) {
                if (this.tracks[i][j] >= 0.0f) {
                    keep_rows[i] = true;
                    continue;
                }
            }
        }

        // self.tracks = self.tracks[keep_rows, :]
        float[][] temp_tracks = this.tracks.clone();
        int num_hit = get_num_true_from_1d_boolean_array(keep_rows);
        this.tracks = get_float_2d_array(num_hit, temp_tracks[0].length, 0.0f);
        temp_cnt = 0;
        for (int i = 0; i < keep_rows.length; i++) {
            if (keep_rows[i]) {
                for (int j = 0; j < this.tracks[0].length; j++) {
                    this.tracks[temp_cnt][j] = temp_tracks[i][j];
                }
                temp_cnt++;
            }
        }

        // Store the last descriptors.
        this.last_desc = desc.clone();
    }

    private float[][] get_tracks(int min_length) {
        /*
        Retrieve point tracks of a given minimum length.
        Input
            min_length - integer >= 1 with minimum track length
        Output
            returned_tracks - M x (2+L) sized matrix storing track indices, where
            M is the number of tracks and L is the maximum track length.
        */

        int temp_cnt = 0;

        if (min_length < 1) {
            // Log.e("PointTracker", "min_length is too small.");
            Log.warning("min_length is too small.");
            throw new IllegalArgumentException("min_length is too small.");
        }

        boolean[] valid = get_bool_1d_array(this.tracks.length, true);

        // good_len = np.sum(self.tracks[:, 2:] != -1, axis=1) >= min_length;
        boolean[] good_len = get_bool_1d_array(this.tracks.length, false);
        for (int i = 0; i < this.tracks.length; i++) {
            temp_cnt = 0;
            for (int j = 2; j < this.tracks[0].length; j++) {
                if (this.tracks[i][j] != -1) {
                    temp_cnt++;
                }
            }
            if (temp_cnt >= min_length) {
                good_len[i] = true;
            }
        }

        // Remove tracks which do not have an observation in most recent frame.
        // not_headless = (self.tracks[:, -1] != -1)
        boolean[] not_headless = get_bool_1d_array(this.tracks.length, false);
        for (int i = 0; i < this.tracks.length; i++) {
            if (this.tracks[i][this.tracks[0].length - 1] != -1) {
                not_headless[i] = true;
            }
        }

        // keepers = np.logical_and.reduce((valid, good_len, not_headless))
        boolean[] keepers = logical_and_for_three_arrays(valid, good_len, not_headless);

        // returned_tracks = self.tracks[keepers, :].copy()
        float[][] returned_tracks = get_float_2d_array(get_num_true_from_1d_boolean_array(keepers), this.tracks[0].length, 0.0f);
        temp_cnt = 0;
        for (int i = 0; i < keepers.length; i++) {
            if (keepers[i]) {
                for (int j = 0; j < this.tracks[0].length; j++) {
                    returned_tracks[temp_cnt][j] = this.tracks[i][j];
                    temp_cnt++;
                }
            }
        }

        return returned_tracks;
    }
}

