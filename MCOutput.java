package minimalcoin;

import java.io.Serializable;
import java.math.*;

public class MCOutput implements Serializable{
    private MCOutPoint outPoint;
    private BigInteger address;
    private BigDecimal value;
    
    public MCOutput(BigInteger adr, BigDecimal val) {
        address = adr;
        value = val;
        value = value.setScale(2, RoundingMode.HALF_DOWN);
        outPoint = null;
    }
    
    // Output constructor for new coin (outPoint: previous tx 0, index 0).
    // If password is not validated ("abcde" here; change and adjust as you like), 
    // then act as the usual constructor above
    // We use the double hash function defined in Tools class to encode pw  
    public MCOutput(String password, BigDecimal val) throws Exception {
        if ((new BigInteger("f831cac72922a387d9a8f2e04cc6c25c47d408b7049a8104faa80c5", 16)).
                equals(MCUtils.dHash224(password.getBytes()))) {
            address = new BigInteger("0");  // = null;
            value = val;
            value = value.setScale(2, RoundingMode.HALF_DOWN);
            outPoint = new MCOutPoint(new BigInteger("0"), 0);
        }
        else throw new Exception(); // TODO: to precise and process exceptions
    }
    
    public void setOutPoint(BigInteger txId, int idx) throws Exception {
        outPoint = new MCOutPoint(txId, idx);
    }
    
    public BigInteger getTxId() {
        return outPoint.mcTxId;
    }
    
    public int getOutIndex() {
        return outPoint.outIdx;
    }
    
    public BigInteger getAddress() {
        return address;
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public String contentToSign() {
        if (outPoint == null) 
            return "";
        return outPoint.mcTxId.toString(16) + outPoint.outIdx + address.toString(16) + value.toString();
    }
    
    @Override
    public String toString() {
        String str0 = "        Tx Id: ";
        String str1 = "        Output index: ";
        if (!(outPoint == null)) {
            str0 = str0 + outPoint.mcTxId.toString(16);
            str1 = str1 + outPoint.outIdx;
        }
        String str2 = "        Address: ";
        if (address != null)
            str2 = str2 + address.toString(16);
        String str3 = "        Value: " + value.toString();
        return "\n" + str0 + "\n" + str1 + "\n" + str2 + "\n" + str3; 
    }
    
    public class MCOutPoint implements Serializable {
        public BigInteger mcTxId;
        public int outIdx;
        public BigInteger outPointID;
        
        public MCOutPoint(BigInteger txId, int idx) throws Exception {
            mcTxId = txId;
            outIdx = idx;
            outPointID = MCUtils.dHash224((mcTxId.toString() + idx).getBytes());
        }
        
        public BigInteger getOutPointID() {
            return outPointID;
        }
        @Override
        public String toString() {
            return "Outpoint: txID = " + mcTxId.toString(16) + " Index: " + outIdx;
        }
    }
    
    public MCOutPoint getOutPoint() {
        return outPoint;
    }
}
