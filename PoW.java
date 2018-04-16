package minimalcoin;

import java.math.BigInteger;
import java.security.MessageDigest;

public class PoW extends Thread {      // TODO: do it threaded!!!
    public static String HARDNESS = "abcde";
    private long nonce = 0;
    private long cont = 0;
    private final MessageDigest md;
    private final BigInteger result;
    
    public PoW(String str) throws Exception {
        md = MessageDigest.getInstance("SHA-256");
        String s = "";
        BigInteger bInt = new BigInteger("0");
        while (!s.startsWith(HARDNESS)) {
            md.update((str + ++nonce).getBytes());
            bInt = new BigInteger(1, md.digest());
            cont++;
            s = bInt.toString(16);
        }
        result = bInt;
    }
    
    public BigInteger getResult() {
        return result;
    }
    
    public long getCont() {
        return cont;
    }
    
    public long getNonce() {
        return nonce;
    }
    
    public void setHardness(String str) {
        HARDNESS = str;
    }
    
}
