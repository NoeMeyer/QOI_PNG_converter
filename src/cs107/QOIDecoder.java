package cs107;

import java.lang.reflect.Array;

import static cs107.Helper.Image;

/**
 * "Quite Ok Image" Decoder
 * @apiNote Third task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder() {
    }

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     *
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header) {

        assert header != null;
        assert header.length == QOISpecification.HEADER_SIZE;
        byte[] headerBegining = ArrayUtils.extract(header, 0, 4);
        byte[] expectedBegining = {'q', 'o', 'i', 'f'};
        for (int i = 0; i < 4; i++) {
            assert headerBegining[i] == expectedBegining[i];
        }

        byte[] encoded1 = ArrayUtils.extract(header, 4, 4);
        byte[] encoded2 = ArrayUtils.extract(header, 8, 4);

        int[] decoded = new int[]{ArrayUtils.toInt(encoded1), ArrayUtils.toInt(encoded2),
                header[12], header[13]};

        assert header[12] == QOISpecification.RGB || header[12] == QOISpecification.RGBA;
        assert header[13] == QOISpecification.sRGB || header[13] == QOISpecification.ALL;

        return decoded;
    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param alpha    (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx) {

        assert buffer != null;
        assert input != null;
        assert idx < input.length;
        assert input.length >= 3;

        byte[] tab = ArrayUtils.extract(input, idx, 3);
        byte r = tab[0];
        byte g = tab[1];
        byte b = tab[2];

        buffer[position][0] = r;
        buffer[position][1] = g;
        buffer[position][2] = b;
        buffer[position][3] = alpha;

        return 3;

    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx) {

        assert buffer != null;
        assert input != null;
        assert idx < input.length;
        assert input.length >= 4;

        byte[] tab = ArrayUtils.extract(input, idx, 4);
        byte r = tab[0];
        byte g = tab[1];
        byte b = tab[2];
        byte a = tab[3];

        buffer[position][0] = r;
        buffer[position][1] = g;
        buffer[position][2] = b;
        buffer[position][3] = a;

        return 4;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     *
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk         (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk) {

        assert previousPixel != null;
        assert previousPixel.length == 4;
        assert (byte) (chunk >> 6) == (byte) (QOISpecification.QOI_OP_DIFF_TAG >> 6);

        chunk = (byte)(chunk & 0b00111111 );
        byte[] decodedDiff = new byte[4];

        decodedDiff[0] = (byte) (previousPixel[0] + ((chunk >> 4) & 0b11) -2);
        decodedDiff[1] = (byte) (previousPixel[1] + ((chunk >> 2) & 0b11) - 2);
        decodedDiff[2] = (byte) (previousPixel[2] + ((chunk) & 0b11) - 2);
        decodedDiff[3] = previousPixel[3];

        return decodedDiff;
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     * @param previousPixel (byte[]) - The previous pixel
     * @param data (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    /**
     * byte dr = diff[0];
     * byte dg = diff[1];
     * byte db = diff[2];
     * byte dr1 = (byte)(dr - dg);
     * byte db1 = (byte)(db - dg);
     * assert (dg > -33 && dg < 32);
     * assert(db1 > -9 && db1 < 8);
     * assert (dr1 > -9 && dr1 < 8);
     * assert (diff != null);
     * assert (diff.length == 3) ;
     * byte[] encoding = new byte[2];
     * byte[] nLuma = new byte[]{(byte) (dg + 32), (byte) (dr1 + 8), (byte) (db1 + 8)};
     * byte tag = QOISpecification.QOI_OP_LUMA_TAG;
     * encoding[0] = (byte) (tag | nLuma[0]);
     * encoding[1] = (byte) (nLuma[1] << 4 | nLuma[2]);
     * return encoding;
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data) {

        assert previousPixel != null;
        assert data != null;
        assert previousPixel.length == 4;
        assert (byte)(data[0] >> 6) == (byte)(QOISpecification.QOI_OP_LUMA_TAG >> 6);

        byte dr1 = (byte) ((data[1] >> 4) & 0b00001111);
        byte dg = (byte) ((data[0] & 0b00111111) - 32);
        byte db1 = (byte) (data[1] & 0b00001111);

        byte dr = (byte) (dr1 + dg - 8);
        byte db = (byte) (db1 + dg - 8);

        byte[] decodedLuma = new byte[4];
        decodedLuma[3] = previousPixel[3];
        decodedLuma[0] = (byte) (previousPixel[0] + dr);
        decodedLuma[1] = (byte) (previousPixel[1] + dg);
        decodedLuma[2] = (byte) (previousPixel[2] + db);

        return decodedLuma;
    }

    /**
     * Store the given pixel in the buffer multiple times
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param pixel    (byte[]) - The pixel to store
     * @param chunk    (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position) {
        assert pixel != null;
        assert buffer != null;

        byte count = (byte) (chunk & 0b00111111);
        for (int i = position; i < position + count + 1; i++) {
            buffer[i] = pixel;
        }
        return count;
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     *
     * @param data   (byte[]) - Data to decode
     * @param width  (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height) {

        assert data != null;

        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];
        byte[][] tab = new byte[height * width][4];
        int j = 0, i = 0, ecart = 0;
        byte index;

        byte RGBTag = QOISpecification.QOI_OP_RGB_TAG;
        byte RGBATag = QOISpecification.QOI_OP_RGBA_TAG;
        byte INDEXTag = QOISpecification.QOI_OP_INDEX_TAG;
        byte DIFFTag = QOISpecification.QOI_OP_DIFF_TAG;
        byte LUMATag = QOISpecification.QOI_OP_LUMA_TAG;
        byte RUNTag = QOISpecification.QOI_OP_RUN_TAG;


        while (i < tab.length) {

            if (data[j] == RGBATag) {
                ecart = 5;
                QOIDecoder.decodeQoiOpRGBA(tab, data, i, j + 1);
            }else {
                if (data[j] == RGBTag) {
                    ecart = 4;
                    QOIDecoder.decodeQoiOpRGB(tab, data, previousPixel[3], i, j + 1);
                }else{
                    if (((byte) (0b11000000 & data[j]) == (byte) (DIFFTag & 0b11000000))) {
                        ecart = 1;
                        tab[i] = QOIDecoder.decodeQoiOpDiff(previousPixel, data[j]);
                    }else {
                        if (((byte) (0b11000000 & data[j]) == (byte) (LUMATag & 0b11000000))) {
                            ecart = 2;
                            byte[] chunk = {data[j], data[j + 1]};
                            tab[i] = QOIDecoder.decodeQoiOpLuma(previousPixel, chunk);
                        } else {
                            if (((byte) (0b11000000 & data[j]) == (byte) (INDEXTag & 0b11000000))) {
                                ecart = 1;
                                index = (byte) (data[j] & 0b00111111);
                                tab[i] = hashTable[index];
                            }

                            if (((byte) (0b11000000 & data[j]) == (byte) (RUNTag & 0b11000000))) {
                                ecart = 1;
                                QOIDecoder.decodeQoiOpRun(tab, previousPixel, data[j], i);
                                i += (data[j] & 0b00111111);
                            }
                        }
                    }
                }
            }

            index = QOISpecification.hash(tab[i]);
            hashTable[index] = tab[i];
            previousPixel = tab[i];
            assert tab[i] != null;
            j += ecart;
            ecart = 0;
            i++;
        }
        return tab;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     *
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content) {

        assert content != null;

        byte tab[][] = ArrayUtils.partition(content,QOISpecification.HEADER_SIZE, (content.length - QOISpecification.HEADER_SIZE - QOISpecification.QOI_EOF.length), QOISpecification.QOI_EOF.length);
        assert ArrayUtils.equals(tab[2], QOISpecification.QOI_EOF);
        byte[] rContent = tab[1];
        int width = ArrayUtils.toInt(ArrayUtils.extract(tab[0], 4, 4));
        int height = ArrayUtils.toInt(ArrayUtils.extract(tab[0], 8, 4));

        byte nbChannels = ArrayUtils.extract(tab[0], 12, 1)[0];
        byte colorSpace = ArrayUtils.extract(tab[0], 13, 1)[0];
        byte[][] pixelValue = decodeData(rContent, width, height);

        int cord = 0;
        int[][] data = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byte[] argb = new byte[4];
                argb[0] = pixelValue[cord][QOISpecification.a];
                argb[1] = pixelValue[cord][QOISpecification.r];
                argb[2] = pixelValue[cord][QOISpecification.g];
                argb[3] = pixelValue[cord][QOISpecification.b];
                data[i][j] = ArrayUtils.toInt(argb);
                cord++;
            }
        }
        return Helper.generateImage(data, nbChannels, colorSpace);
    }
}
