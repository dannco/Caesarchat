package cipher.test;

import cipher.Cipher;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class CipherTest {
    static final boolean ENCRYPT = true, DECRYPT = false;
    static final String DEFAULT_STRING = "this is a cipher.test, lorem ipsum dolor sit amet";
    String key, in, out;
    @Before
    public void setKey() {
        key = Cipher.newKey();
        in = DEFAULT_STRING;
        out = Cipher.run(in, key, ENCRYPT);
    }

    @Test
    public void encryptThenDecrypt() {
        assertFalse("String is properly encrypted",in.equals(out));
        String reIn = Cipher.run(out, key, DECRYPT);
        assertEquals(in, reIn);
    }

    @Test
    public void faultyKey() {
        String newKey;
        while ((newKey = Cipher.newKey()).equals(key)) {}
        String reIn = Cipher.run(out, newKey, DECRYPT);
        assertFalse("Cannot decode a string using the wrong key", in.equals(reIn));
    }

}
