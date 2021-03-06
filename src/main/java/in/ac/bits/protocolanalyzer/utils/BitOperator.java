package in.ac.bits.protocolanalyzer.utils;

import java.util.Arrays;

/**
 * 
 * @author crygnus
 *
 */

public class BitOperator {

    /**
     * Returns the bit in byte b at the given index. Note that index must be
     * between 0-7
     * 
     * @param b
     * @param index
     * @return bit 0 or 1
     * @throws ArrayIndexOutOfBoundsException
     */
    public static int getBit(byte b, int index)
            throws ArrayIndexOutOfBoundsException {

        if (index < 0 || index > 7) {
            throw new ArrayIndexOutOfBoundsException(
                    "Provided bit location is beyond the range 0-7");
        }
        return (b >> index) & 1;
    }

    /**
     * Returns int array 'bit' of size 8 with bit[i] = bit at index 'i' in byte
     * b
     * 
     * @param b
     * @return array of int
     */
    public static int[] getBits(byte b) {
        int[] bits = new int[8];
        for (int i = 0; i < 8; i++) {
            bits[i] = getBit(b, i);
        }
        return bits;
    }

    /**
     * Returns int equivalent of bit array of specified start location and
     * length
     * 
     * @param b
     * @param startBit
     * @param numberOfBits
     * @return int val
     * @throws ArrayIndexOutOfBoundsException
     */
    public static int getValue(byte b, int startBit, int numberOfBits)
            throws ArrayIndexOutOfBoundsException {

        if (startBit + numberOfBits > 8) {
            throw new ArrayIndexOutOfBoundsException(
                    "Relevant bits are beyond this byte!");
        } else {
            int val = 0;
            for (int i = 0; i < numberOfBits; i++) {
                val = val | (getBit(b, i) << i + startBit);
            }
            return val;
        }
    }

    /**
     * Returns integer equivalent of the nibble in byte b with given nibble
     * index (either 0 or 1)
     * 
     * @param b
     * @param nibbleIndex
     * @return int nibble
     * @throws ArrayIndexOutOfBoundsException
     */
    public static int getNibble(byte b, int nibbleIndex)
            throws ArrayIndexOutOfBoundsException {
        if (nibbleIndex == 0) {
            return b & 0xF;
        } else if (nibbleIndex == 1) {
            return (b >> 4) & 0xF;
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Nibble index can only be 0 or 1");
        }
    }

    public static byte[] parse(byte[] header, int startBit, int endBit)
            throws IllegalArgumentException {

        // check if start and end bits are within the range of header
        int byteStartBit = 0;
        int byteEndBit = header.length * 8 - 1;

        if (!lieBetween(startBit, byteStartBit, byteEndBit)
                || !lieBetween(endBit, byteStartBit, byteEndBit)) {
            throw new IllegalArgumentException(
                    "Either startBit or endBit is out of range of header bytes provided!!");

        }
        // check if bit boundaries are perfect byte boundaries. If yes then use
        // arrays.copy util function
        boolean sbBoundary = startBit % 8 == 0;
        boolean ebBoundary = (endBit + 1) % 8 == 0;

        if (sbBoundary && ebBoundary) {
            return Arrays.copyOfRange(header, startBit / 8, (endBit + 1) / 8);
        } else if (sbBoundary && !ebBoundary) {
            int fullByteStart = startBit / 8;
            int fullByteEnd = ((endBit + 1) - ((endBit + 1) % 8)) / 8;
            byte[] returnBytes = new byte[fullByteEnd - fullByteStart + 1];
            byte[] fullBytes = Arrays.copyOfRange(header, fullByteStart,
                    fullByteEnd);
            for (int i = 0; i < fullBytes.length; i++) {
                returnBytes[i] = fullBytes[i];
            }
            returnBytes[returnBytes.length - 1] = bitToByte(header,
                    fullByteStart, fullByteEnd, (endBit + 1) % 8, false);
            return returnBytes;
        } else if (!sbBoundary && ebBoundary) {
            int fullByteEnd = (endBit + 1) / 8;
            int lowerBits = 8 - (startBit % 8);
            int fullByteStart = (startBit + lowerBits) / 8;
            byte[] returnBytes = new byte[fullByteEnd - fullByteStart + 1];
            byte[] fullBytes = Arrays.copyOfRange(header, fullByteStart,
                    fullByteEnd);
            for (int i = 0; i < fullBytes.length; i++) {
                returnBytes[i + 1] = fullBytes[i];
            }
            returnBytes[0] = bitToByte(header, fullByteStart, fullByteEnd,
                    startBit % 8, true);
            return returnBytes;
        } else {
            int lowerBits = 8 - (startBit % 8);
            int fullByteStart = (startBit + lowerBits) / 8;
            int fullByteEnd = ((endBit + 1) - ((endBit + 1) % 8)) / 8;
            byte[] returnBytes = new byte[fullByteEnd - fullByteStart + 2];
            byte[] fullBytes = Arrays.copyOfRange(header, fullByteStart,
                    fullByteEnd);
            for (int i = 0; i < fullBytes.length; i++) {
                returnBytes[i + 1] = fullBytes[i];
            }
            returnBytes[0] = bitToByte(header, fullByteStart, fullByteEnd,
                    startBit % 8, true);
            returnBytes[returnBytes.length - 1] = bitToByte(header,
                    fullByteStart, fullByteEnd, (endBit + 1) % 8, false);
            return returnBytes;
        }

    }

    private static byte bitToByte(byte[] original, int lower, int higher,
            int extraBits, boolean reverse) {
        if (!reverse) {
            // extra bits after higher boundary
            byte[] b = Arrays.copyOfRange(original, higher, higher + 1);
            int val = b[0];
            val = val >> (8 - extraBits);
            return (byte) val;
        } else {
            // extra bits before lower boundary
            byte[] b = Arrays.copyOfRange(original, lower - 1, lower);
            int val = b[0];
            val = val << (extraBits) & 0xFF;
            val = val >> (extraBits);
            return (byte) val;
        }
    }

    private static boolean lieBetween(int target, int lowerLimit,
            int upperLimit) {
        if (target < lowerLimit || target > upperLimit) {
            return false;
        }
        return true;
    }
}
