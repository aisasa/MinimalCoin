package minimalcoin;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.*;

public class MCInput implements Serializable{
    private MCOutput prevOut;
    private byte[] signature;
    private PublicKey pub;
    
    public MCInput(MCOutput out, MCKeyStore keyStore) throws Exception {
        // Getting key par from output address to test its existence...
        MCKeyPar kp = keyStore.get(out.getAddress()).getKeyPar();
        // ...in which case we build the input
        if (kp != null) {
            // Including vector of transaction in inputs
            prevOut = out;
            pub = kp.getPublicKey();
            PrivateKey priv = kp.getPrivateKey();
            signature = MCUtils.sign(out.contentToSign(), priv);
            // To log:
            System.out.println("> New input: ");
            System.out.println("    ·Linked output: " + out.toString());
        }
        else 
            throw new Exception();
    }
    
    // Input constructor for new coin (prevOut outPoint: previous tx 0, index 0).
    // We use to encode pw the double hash function defined in Tools class 
    public MCInput(String password, MCOutput out) throws Exception {
        prevOut = out;
        if ((new BigInteger("f831cac72922a387d9a8f2e04cc6c25c47d408b7049a8104faa80c5", 16)).
                equals(MCUtils.dHash224(password.getBytes()))) {
            prevOut = out;
            pub = null;
            String str = "coinbase input";
            signature = str.getBytes();
        }
    }
 
    public MCOutput getPrevOut() {
        return prevOut;
    }
    
    public byte[] getSignature() {
        return signature;
    }
    
    public PublicKey getPubKey() {
        return pub;
    }
    
    public boolean verify() throws Exception {
        if (pub == null && (new String(signature)).equals("coinbase input"))
            return true;
        return MCUtils.verify(prevOut.contentToSign(), signature, pub);
    }
    
    public String contentToSign() {
        //return mcTxId.toString(16) + inpIdx + prevOut + new BigInteger(1, signature).toString(16) + pub.toString();
        return prevOut + new BigInteger(1, signature).toString(16) + pub.toString();
    }
    
    @Override
    public String toString() {
        String str2 = "        Prev output: " + prevOut.toString();
        String str3 = "        Signature: " + new BigInteger(1, signature).toString(16); //new String(signature);
        String str4 = "        Public key: ";
        if (pub != null)
            str4 = str4 + pub.toString();
        return "\n" + str2 + "\n" + str3 + "\n" + str4;
    }
    
}
