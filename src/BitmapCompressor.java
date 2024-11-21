/******************************************************************************
 *  Compilation:  javac BitmapCompressor.java
 *  Execution:    java BitmapCompressor - < input.bin   (compress)
 *  Execution:    java BitmapCompressor + < input.bin   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   q32x48.bin
 *                q64x96.bin
 *                mystery.bin
 *
 *  Compress or expand binary input from standard input.
 *
 *  % java DumpBinary 0 < mystery.bin
 *  8000 bits
 *
 *  % java BitmapCompressor - < mystery.bin | java DumpBinary 0
 *  1240 bits
 ******************************************************************************/

import java.util.ArrayList;

/**
 *  The {@code BitmapCompressor} class provides static methods for compressing
 *  and expanding a binary bitmap input.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *  @author Zach Blick
 *  @author Cody Kletter
 */
public class BitmapCompressor {
    // magic numbers for zero, one bits, max bit size for each run length and the max int value for that bit size
    public static boolean ONE = true;
    public static boolean ZERO = false;
    public static final int BIT_SIZE = 8;
    public static final int MAX_SIZE = 255;
    /**
     * Reads a sequence of bits from standard input, compresses them,
     * and writes the results to standard output.
     */
    public static void compress() {
        boolean nextBoolean = BinaryStdIn.readBoolean();
        boolean startBoolean = nextBoolean;
        // Array list of each length of consecutive bit runs
        ArrayList<Integer> bitConsecs = new ArrayList<Integer>();
        // Read each set of consecutive ints
        while (!BinaryStdIn.isEmpty()) {
            // Calculate the run length of our next chunk of a certain bit
            int num = nextConsec(nextBoolean);
            // Add consecutive repeat num to array list
            bitConsecs.add(num);
            // Alternate which bit we are looking at
            nextBoolean = !nextBoolean;
        }
        // if the bitmap starts with a one, pad the beginning of the file with zeros to start
        if (startBoolean == ONE) {
            BinaryStdOut.write(0, BIT_SIZE);
        }
        // loop through each of our run lengths
        for (int runLength : bitConsecs) {
            // while the length of the run is greater than max bit size
            while (runLength > MAX_SIZE) {
                // write the max size of the run we can (255), then subtract that from our run length
                BinaryStdOut.write(MAX_SIZE, BIT_SIZE);
                // write out a byte of zeros to represent no run length of the next bit
                BinaryStdOut.write(0, BIT_SIZE);
                runLength = runLength - MAX_SIZE;
            }
            // write out remaining run length in 8 bits
            BinaryStdOut.write(runLength, BIT_SIZE);
        }
        BinaryStdOut.close();
    }

    /**
     * Given a starting bit, determines the run length of that bit in our
     * current location in the file
     * @param startBit either a 1 or a 0, true or false
     * @return run length, as an int
     */
    public static int nextConsec(boolean startBit) {
        // look for the opposite bit of the starting bit (either 0 or 1
        boolean endBit = !startBit;
        int bitCount = 1;
        // Keep looping and counting bits until the file is empty or until the consecutive run ends
        while (!BinaryStdIn.isEmpty() && BinaryStdIn.readBoolean() != endBit) {
            bitCount++;
        }
        // return consecutive run length
        return bitCount;
    }

    /**
     * Reads a sequence of bits from standard input, decodes it,
     * and writes the results to standard output.
     */
    public static void expand() {
        // assume we start with zero
        boolean currentBit = ZERO;
        // keep looping until we've read entire compressed file
        while (!BinaryStdIn.isEmpty()) {
            // read the next 8 bits
            int runLength = BinaryStdIn.readInt(BIT_SIZE);
            // print each bit in the run to expanded file
            for (int i = 0; i < runLength; i++) {
                BinaryStdOut.write(currentBit);
            }
            // alternate our bit
            currentBit = !currentBit;
        }
        BinaryStdOut.close();
    }

    /**
     * When executed at the command-line, run {@code compress()} if the command-line
     * argument is "-" and {@code expand()} if it is "+".
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}