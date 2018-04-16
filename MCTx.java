package minimalcoin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.sql.*;
import java.security.PrivateKey;

public class MCTx implements Serializable {
    private final ArrayList<MCInput> vIn = new ArrayList<>();
    private final ArrayList<MCOutput> vOut = new ArrayList<>();
    private BigInteger hashTxID;
    private BigDecimal totalIn = BigDecimal.valueOf(0.00);
    private BigDecimal totalOut = BigDecimal.valueOf(0.00);
    private Timestamp timeStamp;
    
    public MCTx(MCInput inp) throws Exception {
        vIn.add(inp);
        totalIn = totalIn.add(inp.getPrevOut().getValue());
        // To log?
    }
    
    public MCTx(MCInput inp, MCOutput out) throws Exception {
        vIn.add(inp);
        vOut.add(out);
        totalIn = totalIn.add(inp.getPrevOut().getValue());
        totalOut = totalOut.add(out.getValue());
        // To log?
    }
    
    public boolean addInput(MCInput inp) {
        if (isFinished()){
            return false;
        }
        vIn.add(inp);
        totalIn = totalIn.add(inp.getPrevOut().getValue());
        return true;
    }
    
    public boolean addOutput(MCOutput out) {
        if (isFinished()){
            return false;
        }
        vOut.add(out);
        totalOut = totalOut.add(out.getValue());
        return true;
    }
    
    // Note use of terminate when we build the transaction, then validate()
    public boolean terminate(MCWallet wallet, PrivateKey priv) throws Exception{ 
        // 1. Setting now time
        timeStamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        // 2. A transaction can be unambiguously defined (identified) by its inputs
        hashTxID = MCUtils.dHash224((vIn.toString() + timeStamp.toString()).getBytes());
        // 3. Update wallet removing outputs consumed (new outputs belonging this wallet is delayed upon blockr receipt) 
        // For each input...
        ListIterator<MCInput> iterIn = vIn.listIterator();
        while(iterIn.hasNext()) {
            MCInput input = iterIn.next();
            // ...we get the previous output,...
            MCOutput prevOut = input.getPrevOut();
            // ...confirm if it corresponds to a coinbase tx,...
            // TODO: confirm correction!!!
            if (!prevOut.getAddress().equals(new BigInteger("0")))
                // ...to check otherwise if it is in the wallet,...
                if(!wallet.contains(prevOut.getAddress())) 
                    return false;
                else {
                    // ...and with its address we locate the wallet entry...
                    MCWalletEntry wEntry = wallet.get(prevOut.getAddress());
                    // ...to delete the output
                    wallet.removeOutput(wEntry, prevOut);
                }
        }
        // 4. Complete outpoint in every output
        ListIterator<MCOutput> iterOut = vOut.listIterator();
        int i = 0;
        while(iterOut.hasNext()) {
            MCOutput output = iterOut.next();
            output.setOutPoint(hashTxID, i);
            i++;
        }
        // 5. Validate -> valid transactions other that belonging to first block are sending to network
        if (MCBChain.size() > 0 && validate())  
        // 6. Send to network
            sendTx();
        else
            return false;
        // 6. To log
        System.out.println("> Transaction " + hashTxID.toString(16) + " completed and sent: ");
        System.out.println(this.toString());
        // 7. Done!
        return true;
    }
    
    // Note use of validate from terminate() (if we build the tx) or from another class (if we receive tx)
    public boolean validate() throws Exception {
        // TODO: is it complete???
        // 1. Testing that inputs and outputs exist, and sum of values is equal
        if (!(nInputs() > 0 && nOutputs() > 0 && totalIn.equals(totalOut)))
            return false;
        // 2. Verify input signatures
        ListIterator<MCInput> iterIn = vIn.listIterator();
        while(iterIn.hasNext()) {
            MCInput input = iterIn.next();
            if (!input.verify())
                return false;
        }
        // 3. Verify hash as an additional check
        if (!hashTxID.equals(MCUtils.dHash224((vIn.toString() + timeStamp.toString()).getBytes())))
            return false;
        
        return true;
    }
    
    private void sendTx() throws Exception {
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        MCUtils.initRec();
        MCUtils.sendObject(this, ip);
    }
    
    public BigInteger getHash() {
        return hashTxID;
    }
    
    public ArrayList<MCInput> getVIn() {
        return vIn;
    }
    
    public ArrayList<MCOutput> getVOut() {
        return vOut;
    }
    
    public String getTimestamp() {
        return timeStamp.toString();
    }
    
    public BigDecimal getTotalIn() {
        return totalIn;
    }
    
    public BigDecimal getTotalOut() {
        return totalOut;
    }
    
    public int nInputs() {
        return vIn.size();
    }
    
    public int nOutputs() {
        return vOut.size();
    }
    
    public boolean isFinished() {
        return hashTxID != null;
    }
    
    public void listInputs() {
        ListIterator<MCInput> iter = vIn.listIterator();
        while(iter.hasNext())
            System.out.println(iter.next());
    }
    
    public void listOutputs() {
        ListIterator<MCOutput> iter = vOut.listIterator();
        while(iter.hasNext())
            System.out.println(iter.next());
    }
    
    public ListIterator<MCOutput> getOutputs() {
        return vOut.listIterator();
    }
    
    public ListIterator<MCInput> getInputs() {
        return vIn.listIterator();
    }
    
    @Override
    public String toString() {  // TODO: fix textual representation
        return vIn.toString() + vOut.toString() + "\n        Timestamp: " + timeStamp.toString() + "\n        Hash: " + hashTxID.toString(16);
    }
}