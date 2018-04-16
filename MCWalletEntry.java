package minimalcoin;

import java.math.*;
import java.security.*;
import java.util.ArrayList;

public class MCWalletEntry {
    private BigInteger address;
    private final ArrayList<MCOutput> outputs;
    private BigDecimal balance;
    
    public MCWalletEntry(BigInteger addr) {
        address = addr;
        outputs = new ArrayList<>();
        balance = BigDecimal.valueOf(0.00);
    }
    
    public MCWalletEntry(MCOutput out, PublicKey pub) throws Exception {
        if (out.getAddress().equals(MCAddress.computeAddress(pub))) {
            address = out.getAddress();
            outputs = new ArrayList<>();
            outputs.add(out);
            balance = out.getValue();
        }
        else
            throw new Exception();
    }
    
    protected boolean addOutput(MCOutput out) {
        if (out.getAddress().equals(address)){
                outputs.add(out);
                balance = balance.add(out.getValue());
        }
        else
            return false;
        return true;
    }
    
    protected boolean removeOutput(MCOutput out) {
        if (out.getAddress().equals(address)){
                outputs.remove(out);
                balance = balance.subtract(out.getValue());
                System.out.println("> Removed output " + out.toString());
                // TODO: finish to log
                System.out.println("");
        } 
        else
            return false;
        return true;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public BigInteger getAddress() {
        return address;
    } 
    
    public ArrayList<MCOutput> getOutputs() {
        return outputs;
    }   
}
