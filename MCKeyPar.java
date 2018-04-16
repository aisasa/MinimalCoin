package minimalcoin;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

public class MCKeyPar {
    private final KeyPair kPair;
    private final BigInteger pubXCoord;
    
    public MCKeyPar() throws Exception {
        // Settings
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec eccParam = new ECGenParameterSpec("secp256k1");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); 
        keyGen.initialize(eccParam, random); //(256, random);
        // Generating key par and identifying x coordinate
        kPair = keyGen.generateKeyPair();
        pubXCoord = ((ECPublicKey)kPair.getPublic()).getW().getAffineX();
    }
    
    public void keyParDump() {
        System.out.println("priv = " + new BigInteger(1, kPair.getPrivate().getEncoded()).toString(16));
        System.out.println("pub = " +  kPair.getPublic()); 
        System.out.println("pubXCoord = " + pubXCoord.toString(16)); 
    }
    
    public KeyPair getKeyPair() {
        return kPair;
    }
    public PrivateKey getPrivateKey() {
        return kPair.getPrivate();
    }
    public PublicKey getPublicKey() {
        return kPair.getPublic();
    }
    public BigInteger getPubXCoord(){
        return pubXCoord;
    }
    
}
