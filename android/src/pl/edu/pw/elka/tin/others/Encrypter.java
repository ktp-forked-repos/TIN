package pl.edu.pw.elka.tin.others;


public class Encrypter {

    public static String encode(String s, String key) {
       return new String(xorWithKey(s.getBytes(), key.getBytes()));
    }

    private static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        return out;
    }

}
