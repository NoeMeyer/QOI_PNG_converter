package cs107;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * "Quite Ok Image" Encoder
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder(){}

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *  (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image) {

        byte[] qoiHeader = new byte [QOISpecification.HEADER_SIZE];

        for (int i = 0; i < 4; i ++) {
            qoiHeader[i] = QOISpecification.QOI_MAGIC[i];
        }

        for (int i = 0; i < 4; i++) {
            qoiHeader[i + 4] = ArrayUtils.fromInt(image.data()[0].length)[i];
        }

        for (int i = 0; i < 4; i++) {
            qoiHeader[i + 8] = ArrayUtils.fromInt(image.data().length)[i];
        }
        qoiHeader[12] = image.channels();
        qoiHeader[13] = image.color_space();

        return qoiHeader;
    }
    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel) {

        assert (pixel.length == 4);
        return new byte[]{QOISpecification.QOI_OP_RGB_TAG, pixel[0], pixel[1], pixel[2] };
    }
    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel) {

        assert pixel.length == 4;
        byte[] rgbaValues = new byte[5];
        rgbaValues[0] = QOISpecification.QOI_OP_RGBA_TAG;
        for (int i = 1; i < 5; i++){
            rgbaValues[i] = pixel[i - 1];
        }
        return rgbaValues;
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index){

        assert index < 64;
        assert index >= 0;
        byte tag = QOISpecification.QOI_OP_INDEX_TAG;
        return new byte[]{(byte)(tag | index)};
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff){

        assert diff != null;

        for (int i = 0; i < 3; i++) {
            assert diff[i] > -3 && diff[i] < 2;
        }
        assert diff.length == 3;
        byte[] encoding;
        byte[] nDiff = new byte[] {(byte)(diff[0] + 2), (byte)(diff[1] + 2), (byte)(diff[2] + 2)};byte tag =  QOISpecification.QOI_OP_DIFF_TAG;
        nDiff[0] = (byte)(nDiff[0] << 4);
        nDiff[1] = (byte)(nDiff[1] << 2);
        byte temp = (byte)(tag |  nDiff[0] | nDiff[1] | nDiff[2]);
        encoding = ArrayUtils.wrap(temp);
        return encoding;
    }
    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     * or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff){

        byte dr = diff[0];
        byte dg = diff[1];
        byte db = diff[2];
        byte dr1 = (byte)(dr - dg);
        byte db1 = (byte)(db - dg);

        assert (dg > -33 && dg < 32);
        assert(db1 > -9 && db1 < 8);
        assert (dr1 > -9 && dr1 < 8);
        assert (diff != null);
        assert (diff.length == 3) ;

        byte[] encoding = new byte[2];
        byte[] nLuma = new byte[]{(byte) (dg + 32), (byte) (dr1 + 8), (byte) (db1 + 8)};
        byte tag = QOISpecification.QOI_OP_LUMA_TAG;

        encoding[0] = (byte) (tag | nLuma[0]);
        encoding[1] = (byte) (nLuma[1] << 4 | nLuma[2]);

        return encoding;
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count){

        assert (count >= 1 && count <= 62);

        byte [] run = new byte[1];
        byte cnt = (byte)(count - 1);
        byte tag = QOISpecification.QOI_OP_RUN_TAG;

        run[0] = (byte)(tag | cnt);

        return run;
    }
    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================
    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image) {

        assert image != null;

        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];
        int compteur = 0;
        ArrayList<byte[]> tab = new ArrayList<>();

        for (int i = 0; i < image.length; i++) {
            byte[] temp = image[i];
            if (ArrayUtils.equals(temp, previousPixel)) {
                compteur++;
                if (compteur == 62 || i == image.length - 1) {
                    tab.add(QOIEncoder.qoiOpRun((byte) compteur));
                    compteur = 0;
                }
            } else {
                if (compteur > 0) {
                    tab.add(QOIEncoder.qoiOpRun((byte) compteur));
                    compteur = 0;
                }
                if (temp == hashTable[QOISpecification.hash(temp)]) {
                    tab.add(QOIEncoder.qoiOpIndex(QOISpecification.hash(temp)));
                } else {
                    hashTable[QOISpecification.hash(temp)] = temp;
                    if (previousPixel[3] == temp[3]) {
                        byte dr = (byte) (temp[0] - previousPixel[0]);
                        byte dg = (byte) (temp[1] - previousPixel[1]);
                        byte db = (byte) (temp[2] - previousPixel[2]);
                        byte[] Diff = new byte[]{dr, dg, db};
                        byte[] Diff1 = new byte[]{dr, dg, db};
                        if (testDiff(dr, dg, db)) {
                            tab.add(QOIEncoder.qoiOpDiff(Diff));
                        } else if (testLuma(dr, dg, db)) {
                            tab.add(QOIEncoder.qoiOpLuma(Diff1));
                        } else {
                            tab.add(QOIEncoder.qoiOpRGB(temp));
                        }
                    }
                    if (previousPixel[3] != temp[3]) {
                        tab.add(QOIEncoder.qoiOpRGBA(temp));
                    }
                }
            }
            previousPixel = temp;
        }
        return ArrayUtils.concat(tab.toArray(new byte[0][]));
    }

        /**
         * Creates the representation in memory of the "Quite Ok Image" file.
         * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
         * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
         * @param image (Helper.Image) - Image to encode
         * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
         * @throws AssertionError if the image is null
         */
    public static byte[] qoiFile(Helper.Image image){

        byte[][] input = ArrayUtils.imageToChannels(image.data());
        return ArrayUtils.concat(qoiHeader(image), encodeData(input), QOISpecification.QOI_EOF);
    }
    public static boolean testDiff(byte dr, byte dg, byte db) {
        boolean test = false;

        if ((dr > -3 && dr < 2) && (dg > -3 && dg < 2) && (db > -3 && db < 2)) {
            test = true;
        }
        return test;
    }
    public static boolean testLuma(byte dr, byte dg, byte db) {
        boolean test = false;

        if ((dr - dg  > -9 && dr - dg < 8) && (dg > -33 && dg < 32 ) && (db - dg > -9 && db - dg< 8)) {
            test = true;
        }
        return test;
    }
}

