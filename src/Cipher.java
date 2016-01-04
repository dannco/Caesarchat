import java.util.Random;

class Cipher {
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
            
            while (v<MIN) {
                v = v + MAX - MIN + 1;
            } while (v>MAX) {
                v = v - MAX + MIN - 1;
            }
            out += (char)v;
            
        } 
        return out;
    }

    public static String newKey() {
        return newKey(DEFAULT_KEY_LENGTH);
    }
    
    public static String newKey(int length) {
        String k = "";
        for (int i = 0 ; i < length ; i++) {
            int c = (Math.abs(rng.nextInt())%(MAX-MIN)+MIN);
            k += (char)c;
        }
        return k;
    }
}