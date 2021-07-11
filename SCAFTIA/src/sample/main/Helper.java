package sample.main;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.Arrays;
public class Helper {



    //convert string to byte
    public static byte[] stringToBytes(String s){
        String[] parts = s.split("_");
        byte[] bytework = new byte[parts.length];
        int total = 0;

        for (int i = 0; i < parts.length; i++)
        {
            try {
                bytework[total] = Byte.parseByte(parts[i]);
                total++;
            } catch (Exception ex) {
                System.err.println( ex.getMessage());
            }
        }

        if ( total < 0)
        {
            return new byte[0];
        }
        byte[] bytesFinal = new byte[total];
        System.arraycopy(bytework, 0, bytesFinal, 0, bytesFinal.length);
        return bytesFinal;
    }

    //convert byte to string
    public static String bytesToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, array.length).forEach(i -> {
            sb.append(array[i] + "_");
        });
        return sb.toString().trim();
    }

    //convert byte to hex
    public static String convertBytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte temp : bytes) {
            int decimal = (int) temp & 0xff;  // bytes widen to int, need mask, prevent sign extension
            String hex = Integer.toHexString(decimal);
            result.append(hex);
        }
        return result.toString();
    }

    //split string equlally by size
    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);
        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

    //split fileName from path
    public static String getFileNameFromAllPath(String pathFile){
        String regEx4Win = "\\\\(?=[^\\\\]+$)";
        String[] tokens = pathFile.split(regEx4Win);
        if (tokens.length > 0)
            return   tokens[1];
        return "";
    }

    //denerate random port
    public static int genRandomPort() {
        int max=3000;
        int min =1000;
        if (min >= max) {
            throw new IllegalArgumentException();
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    //merger many byt[]
    public static byte[] concat(byte[]...arrays)
    {
        // Determine the length of the result array
        int totalLength = 0;
        for (int i = 0; i < arrays.length; i++)
        {
            totalLength += arrays[i].length;
        }
        // create the result array
        byte[] result = new byte[totalLength];

        // copy the source arrays into the result array
        int currentIndex = 0;
        for (int i = 0; i < arrays.length; i++)
        {
            System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
            currentIndex += arrays[i].length;
        }
        return result;
    }


    //find index of pattern in byt[]
    private static int indexOf(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j>0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }


    //split byt[] with first delimiter that found
    public static List<byte[]> splitByteWithFirstDelimiter(byte[] array, byte[] delimiter)
    {
       int index= indexOf(array,delimiter);
        List<byte[]> byteArrays = new LinkedList<byte[]>();
        byteArrays.add(Arrays.copyOfRange(array, 0, index));
        byteArrays.add(Arrays.copyOfRange(array, index+1, array.length));
        return byteArrays;
    }

    //read file byt[] from file path
    public static byte[] readFileBytes(String filePath)  {
        if (filePath.equals("")) return new byte[0];
        File file = new File(filePath);

        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }

}
