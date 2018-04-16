package minimalcoin;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;

public class MCAddress {
    private final BigInteger address;
    private final MCKeyPar keyPar;
    
    public MCAddress() throws Exception {
        keyPar = new MCKeyPar();
        BigInteger xCoord = keyPar.getPubXCoord();  
        address = MCUtils.dHash224(xCoord.toByteArray());
    }
    
    public BigInteger getAddress() {
        return address;
    }
    
    public MCKeyPar getKeyPar() {
        return keyPar;
    }
    
    public static BigInteger computeAddress(PublicKey pub) throws Exception {
        return MCUtils.dHash224(((ECPublicKey)pub).getW().getAffineX().toByteArray());
    }
    
    public void toKeyStore(MCKeyStore keyStore) {
        keyStore.put(address, this);
    }
}