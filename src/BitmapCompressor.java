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
 *  @author YOUR NAME HERE
 */
public class BitmapCompressor {
    public static boolean ONE = true;
    public static boolean ZERO = false;
    /**
     * Reads a sequence of bits from standard input, compresses them,
     * and writes the results to standard output.
     */
    public static void compress() {
        boolean nextBoolean = BinaryStdIn.readBoolean();
        boolean startBoolean = nextBoolean;
        // Keep track of the highest consecutive run of bits
        int longestConsec = Integer.MIN_VALUE;
        // Array list of each length of consecutive bit runs
        ArrayList<Integer> bitConsecs = new ArrayList<Integer>();
        // Read each set of consecutive ints
        while (!BinaryStdIn.isEmpty()) {
            // Calculate the run length of our next chunk of a certain bit
            int num = nextConsec(nextBoolean);
            longestConsec = Math.max(longestConsec, num);
            // Add consecutive repeat num to array list
            bitConsecs.add(num);
            // Alternate which bit we are looking at
            nextBoolean = !nextBoolean;
        }
        int length = bitConsecs.size();
        // Create a new array list of unique run lengths
        ArrayList<Integer> bitConsecsUnique = new ArrayList<Integer>();
        // Only add unique run lengths to new array list
        for (int element : bitConsecs) {
            if (!bitConsecsUnique.contains(element)) {
                bitConsecsUnique.add(element);
            }
        }
        int noDupLength = bitConsecsUnique.size();
        // determine max # of bits needed for given length
        int minMapBits = (int) Math.ceil((Math.log(noDupLength) / Math.log(2))) + 1;
        // determine max # of bits needed for given longest consecutive num of bits
        int minBits = (int) Math.ceil((Math.log(longestConsec) / Math.log(2))) + 1;

        // create file header
        // write the length of each of our runs
        BinaryStdOut.write(length);
        // write min bits needed for each of our consecutive run lengths
        BinaryStdOut.write(minBits);
        // write the length of each of our runs with no duplicates
        BinaryStdOut.write(noDupLength);
        // write starting bit
        BinaryStdOut.write(startBoolean);
        // create a map to convert each run length to its unique code value
        int[] consecMap = new int[longestConsec + 1];
        // write each length code into file header for each unique run length
        for (int i = 0; i < noDupLength; i++) {
            BinaryStdOut.write(bitConsecsUnique.get(i), minBits);
            consecMap[bitConsecsUnique.get(i)] = i;
        }

        // write out the code of each consecutive run length in the body of the file
        for (int i = 0; i < length; i++) {
            int consecShortened = consecMap[bitConsecs.get(i)];
            BinaryStdOut.write(consecShortened, minMapBits);
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
        // read file header
        // read size of each of the run lengths
        int length = BinaryStdIn.readInt();
        // read the minimum bits needed to write all of the run lengths
        int minBits = BinaryStdIn.readInt();
        // read the size of each of the unique run lengths
        int noDupLength = BinaryStdIn.readInt();
        // calculate the minimum bits needed for our code lengths
        int minMapBits = (int) Math.ceil((Math.log(noDupLength) / Math.log(2))) + 1;
        // read the starting bit
        boolean nextBoolean = BinaryStdIn.readBoolean();
        // create a map to map each code to its corresponding run length from the header file
        int[] consecMap = new int[noDupLength + 1];
        // loop through each of our codes
        for (int i = 0; i < noDupLength; i++) {
            // read each unique run length and add it to our map
            int nextConsec = BinaryStdIn.readInt(minBits);
            consecMap[i] = nextConsec;
        }

        // loop through each run length
        for (int i = 0; i < length; i++) {
            // read the code for the run length and determine its corresponding run length
            int mapIndex = BinaryStdIn.readInt(minMapBits);
            int nextConsec = consecMap[mapIndex];
            // write out the current bit the number of times of the run length to expanded file
            for (int j = 0; j < nextConsec; j++) {
                BinaryStdOut.write(nextBoolean);
            }
            // change boolean to opposite
            nextBoolean = !nextBoolean;
        }

//        int index = 0;
        // create map of ints to ints conversion
//        int[] consecMap = new int[length];
//        for (int i = 0; i < length; i++) {
//            int BinaryStdIn.readInt();
//        }

//        while (index < length * maxBits) {
//            int nextConsec = BinaryStdIn.readInt(maxBits);
//            // print the next boolean that number of times
//            for (int i = 0; i < nextConsec; i++) {
//                BinaryStdOut.write(nextBoolean);
//            }
//            nextBoolean = !nextBoolean;
//            index += maxBits;
//        }
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