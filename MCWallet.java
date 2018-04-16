package minimalcoin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

public class MCWallet {
    private final static HashMap<BigInteger, MCWalletEntry> wallet = new HashMap<>(); 
    private static BigDecimal walletBalance = BigDecimal.valueOf(0.00); 

    public void add(MCWalletEntry we){
        // TODO: test if we exists previously
        BigInteger addr = we.getAddress();
        wallet.putIfAbsent(addr, we);
        walletBalance = walletBalance.add(we.getBalance());
        // To log:
        System.out.println("> New entry in wallet: ");
        System.out.println("    ·Address: " + addr.toString(16));
        System.out.println("    ·Entry Balance: " + we.getBalance());
        System.out.println("    ·New wallet balance: " + this.getBalance());
        System.out.println("> " + this.size() + " items in wallet");
    }
    
    public void add(MCOutput out, BigInteger addr) {
        wallet.get(addr).addOutput(out);
        walletBalance = walletBalance.add(out.getValue());   
        System.out.println("> New output in wallet: ");
        System.out.println("    ·Address: " + addr.toString(16));
        System.out.println("    ·Value: " + out.getValue());
        System.out.println("    ·New wallet balance: " + this.getBalance());
    }
    
    public void removeOutput(MCWalletEntry wEntry, MCOutput out) {
        if(wEntry.removeOutput(out))
            walletBalance = walletBalance.subtract(out.getValue());
    }
    
    // Confirmar OK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public ArrayList<MCOutput> locateValue(BigDecimal value) throws Exception {
        // TODO: a better algorithm to choose the outputs
        if (getBalance().compareTo(value) < 0)
            throw new Exception();
        ArrayList<MCOutput> listOfOutputs = new ArrayList();
        Iterator<MCWalletEntry> iterEntries = wallet.values().iterator();
        while(iterEntries.hasNext()) {
            MCWalletEntry entry = iterEntries.next();
            if (entry.getBalance().compareTo(value) >= 0) { //<=================
                ArrayList<MCOutput> outs = entry.getOutputs();
                ListIterator<MCOutput> iterOuts = outs.listIterator();
                BigDecimal tempValue = new BigDecimal("0");
                while(iterOuts.hasNext()){
                    MCOutput out = iterOuts.next();
                    // We return the first output whose value is equal to sought value
                    if (out.getValue().equals(value)){
                        ArrayList<MCOutput> temp = new ArrayList<>();
                        temp.add(out);
                        return temp;
                    }
                    // If no such output is found, collect several outputs trying to gather an amount no smaller than value
                    else if (out.getValue().compareTo(value) < 0) {
                        listOfOutputs.add(out);
                        tempValue = tempValue.add(out.getValue());
                        if (tempValue.compareTo(value) >= 0)
                            return listOfOutputs;
                    }   
                }
            }

        }
        // If reached this point, some has gone wrong
        throw new Exception();
    }
    
    public BigDecimal getBalance() {
        return walletBalance;
    }
    
    public int size() {
        return wallet.size();
    }
    
    public MCWalletEntry get(BigInteger addr) {
        return wallet.get(addr);
    }
    
    public void remove(BigInteger addr) {
        wallet.remove(addr);
    }
    
    public boolean contains(BigInteger addr) {
        return wallet.containsKey(addr);
    }
    
    public Iterator<MCWalletEntry> list() {
        return (wallet.values()).iterator(); 
    }
    
    
}
