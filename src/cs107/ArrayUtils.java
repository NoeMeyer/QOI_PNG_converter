package cs107;

/**
 * Utility class to manipulate arrays.
 * @apiNote First Task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils() {
    }

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2) {
        boolean test = true;
        if (a1.length != a2.length) {
            return false;
        }else{
            for (int i = 0; i < a1.length; i++) {
                if (a1[i] != a2[i]) {
                    test = false;
                }
            }
        }
        return test;
    }

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2) {
        boolean test = true;
        if (a1.length != a2.length || a1[0].length != a2[0].length){
            return false;
        }else {

            for (int i = 0; i < a1.length; i++) {
                for (int j = 0; j < a1[i].length; j++) {
                    for (int n = 0; n < a2[n].length; n++) {
                        if (a1[i][j] != a2[i][j]) {
                            test = false;
                        }
                    }
                }
            }
        }
        if (a1 == null && a2 == null) {
            test = true;
        }
        return test;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     *
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value) {
        return new byte[]{value};
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes) {

        assert bytes != null;
        assert bytes.length == 4;

        int intValue = 0;
        for (byte b : bytes) {
            intValue = (intValue << 8) + (b & 0xFF);
        }
        return intValue;
    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     /* @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */

    public static byte[] fromInt(int value) {

        byte[] intValue = new byte[4];
        for (int i = 0; i < 4; i++) {
            intValue[i] = (byte)(value >>> 24 - i * 8);
        }
        return intValue;
    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     *
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte ... bytes){
        assert bytes != null;
        int nb = bytes.length;
        assert bytes != null;
        byte[] tab = new byte[nb];
        for (int i = 0; i < nb; i++) {
            tab[i] = bytes[i];
        }
        return tab;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     *
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[]... tabs) {
        assert tabs != null;
        int nb = tabs.length;
        int sum = 0;
        for (int i = 0; i < nb; i++) {
            assert tabs[i] != null;
            sum = sum + tabs[i].length;
        }
        byte[] tab = new byte[sum];
        int k= 0;
        for (int i = 0; i < nb; i++) {
            byte[] temp = tabs[i];
            for (int j = 0; j < temp.length; j ++) {
                tab[k] = temp[j];
                k++;
            }
        }
        return tab;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     *
     * @param input  (byte[]) - Array to extract from
     * @param start  (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     *                        start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length) {
        assert input != null;
        byte[] extracted = new byte[length];
        for(int i = 0; i < extracted.length; i++){
            extracted[i] = input[start + i];
        }
        return extracted;
    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     *
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     *                        or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int... sizes) {

        assert input != null;
        assert sizes != null;
        int longueur = 0;
        int nb = sizes.length;
        for (int i = 0; i < nb; i++) {
            longueur += sizes[i];
        }
        assert longueur == input.length;

        int debut = 0;
        byte[][] tab = new byte[nb][];
        for (int i = 0; i < nb; i++) {
            tab[i] = new byte[sizes[i]];
            for (int j = 0; j < sizes[i]; j++) {
                tab[i][j] = input[j + debut];
            }
            debut += sizes[i];
        }
        return tab;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     *
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input) {

        assert (input != null);
        int height = input.length;
        int rowWidth = input[0].length;
        int size = 0;
        for (int i = 0; i < height; i++) {
            assert input[i].length == rowWidth;
            assert (input[i] != null);
            size += rowWidth;
        }

        assert size == height * rowWidth;
        byte[][] output = new byte[size][4];
        int cord = 0;
        for (int y = 0; y < height; y ++){
            for (int x = 0; x < rowWidth; x++) {
                output[cord][0] = fromInt(input[y][x])[1];
                output[cord][1] = fromInt(input[y][x])[2];
                output[cord][2] = fromInt(input[y][x])[3];
                output[cord][3] = fromInt(input[y][x])[0];
                cord++;
            }
        }
        return output;
    }


    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     *
     * @param input  (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width  (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     *                        or input's length differs from width * height
     *                        or height is invalid
     *                        or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width) {

        assert input.length == height * width;
        assert (input != null);
        for (int i = 0; i < input.length; i++) {
            assert (input[i].length == 4);
        }

        int[][] output = new int[height][width];
        for (int cord = 0; cord < input.length; cord++) {
            int x = cord % width;
            int y = (cord - x) / width;
            byte[] temp = new byte[4];
            temp[0] = input[cord][3];
            temp[1] = input[cord][0];
            temp[2] = input[cord][1];
            temp[3] = input[cord][2];
            output[y][x] = toInt(temp);
        }
        return output;
    }
}