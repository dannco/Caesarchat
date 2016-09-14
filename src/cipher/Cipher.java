package cipher;

import java.util.Random;
import java.util.stream.IntStream;

public class Cipher {
    static final int MIN = 32;
    static final int MAX = 126; 
    static final int DEFAULT_KEY_LENGTH = 10;
    static Random rng = new Random();
    
    public static String run(String msg, String key, boolean encrypt) {
        int index = 0;
        int shift = encrypt?-1:1;
        
        String out = "";
        char[] keyArr = key.toCharArray();
        char[] msgArr = msg.toCharArray();

        for (char c : msgArr) {
            if (c < MIN || c > MAX) continue;
            int v = c + shift*keyArr[index++];
            if (index==keyArr.length) {
                index = 0;
            }
            while (v<MIN || v>MAX) {
                v += (v<MIN? 1:-1)*(MAX - MIN + 1);
            }
            out += (char)v;
        } 
        return out;
    }

    public static String newKey() {
        return newKey(DEFAULT_KEY_LENGTH);
    }
    public static String newKey(int length) {
        StringBuffer buffer = new StringBuffer();
        IntStream.range(0,length).forEach(x ->
            buffer.append((char)(Math.abs(rng.nextInt())%(MAX-MIN)+MIN))
        );
        return buffer.toString();
    }
}