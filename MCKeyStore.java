package minimalcoin;

import java.math.BigInteger;
import java.util.HashMap;

public class MCKeyStore {
    private final static HashMap<BigInteger, MCAddress> keyStore = new HashMap<>();
    
    public void put(BigInteger addr, MCAddress mcAddr) {
        keyStore.put(addr, mcAddr);
        // To log:
        System.out.println("> New address and associated key par added to keyStore: ");
        System.out.println("    ·Address: " + (mcAddr.getAddress()).toString(16));
        System.out.println("> " + keyStore.size() + " items in keyStore");
    }
    
    public MCAddress get(BigInteger key) {
        return keyStore.get(key);
    }
    
    public void remove(BigInteger key) {
        keyStore.remove(key);
    }
    
    public boolean containsKey(BigInteger key) {
        return keyStore.containsKey(key);
    }
    
    public int size() {
        return keyStore.size();
    }
    
}