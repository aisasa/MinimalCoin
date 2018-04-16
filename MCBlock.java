package minimalcoin;

import java.io.Serializable;
import java.util.ArrayList;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.ListIterator;

public class MCBlock implements Serializable {
    private final ArrayList<MCTx> txs;
    private final BigInteger hashPrevBlock;
    private BigInteger hash;
    private BigInteger fictionalMerkleRoot; // TODO: a true Merkle tree root
    private Timestamp timeStamp;
    private String hardness; 
    private long nonce;
    
    public MCBlock(MCTx tx, BigInteger hashPrev) {
        txs = new ArrayList<>();
        nonce = 0;
        hashPrevBlock = hashPrev;
        txs.add(tx);
    }
    
    public void addTx(MCTx tx) {
        txs.add(tx);
    }
    
    // Note use of terminate when we build the block, then validate()
    public void terminate() throws Exception {
        fictionalMerkleRoot = doFictionalMerkle();
        timeStamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        doPoW();
        // Avoiding first block particularity (not to send to network) sending only next blocks
        if(MCBChain.size() > 0 && validate()) {
            sendBlock();
        } 
    }
    
    // A fictional Merkle tree obtaining its 'root' hashing chained transactions
    private BigInteger doFictionalMerkle() throws Exception {
        String s = "";
        ListIterator<MCTx> iter = txs.listIterator();
        while(iter.hasNext())
            s = s + iter.next().toString();
        return MCUtils.dHash224(s.getBytes());
    }
    
    private void doPoW() throws Exception {
        PoW pow = new PoW(hashPrevBlock.toString(16) + fictionalMerkleRoot.toString(16) + timeStamp.toString());
        hardness = PoW.HARDNESS; 
        hash = pow.getResult();
        nonce = pow.getNonce();
    }
    
    private void sendBlock() throws Exception {
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        MCUtils.initRec();
        MCUtils.sendObject(this, ip);
    }
    
    // Note use of validate from terminate() (if we build the block) or from another class (if we receive the block)
    public boolean validate() throws Exception {
        // TODO: fix wallet thing
        // 1. Check hash
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update((hashPrevBlock.toString(16) + fictionalMerkleRoot.toString(16) + timeStamp.toString() + nonce).getBytes());
        BigInteger checkHash = new BigInteger(1, md.digest());
        if (!checkHash.equals(hash))
            return false;
        // 2. Check hashPrevBlock belongs to the last block in chain
        if (MCBChain.size() > 0) {
            if (!(MCBChain.getLastBlock().getHash()).equals(hashPrevBlock))
                return false;
        }
        if (MCBChain.size() == 0) {
            if (!(new BigInteger("0")).equals(hashPrevBlock))
                return false;
        }
        // 3. Check (fictional) Merkle tree
        if (!fictionalMerkleRoot.equals(this.doFictionalMerkle()))
            return false;
        // 4. Check timestamp if you want (in some way). Here, for instance, lapse no more than 5 min.
        Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
        if ((now.getTime() - timeStamp.getTime()) > MCUtils.TSMAXPERIOD) 
            return false;
        // 5. For each transaction:
        ListIterator<MCTx> iterTx = getTxs();
        while(iterTx.hasNext()) {
            MCTx tx = iterTx.next();
            // 5.1 Get every input...
            ListIterator<MCInput> iterInp = tx.getInputs();      
            while(iterInp.hasNext()) {
                // ...and check if its previous output is in UTxO...
                BigInteger oPointID = iterInp.next().getPrevOut().getOutPoint().getOutPointID();
                if (MCBChain.getUnspOutput(oPointID).getOutPoint().getOutPointID() == null) {
                    // ...considering that first block does not have previous UTxO records
                    if (MCBChain.size() > 0){
                        return false;        
                    }
                }  
            }   
            // 5.2 Validate tx, which implies 5.2.1 Check that inputs, outputs exist; 
            // 5.2.2 if out_values == in_values; 5.2.3 and verify input signatures
            if (!tx.validate())
                return false; 
        } 
        // 6. Log it and finish
        System.out.println("    Validation of block " + getHash().toString(16) + " OK");
        return true;        
    }
    
    @Override
    public String toString() {
        String s0 = "        Hash of prev Block: " + hashPrevBlock.toString(16);
        String s1 = "        Hash of this Block: " + hash.toString(16);
        String s2 = "        Hash root: " + fictionalMerkleRoot.toString(16);
        String s3 = "        Nonce: " + nonce;
        String s4 = "";
        ListIterator<MCTx> iter = txs.listIterator();
        while(iter.hasNext())
            s4 = s4 + iter.next().toString();
        s4 = "        Tx set: " + s4;
        return "\n" + s0 + "\n" + s1 + "\n" + s2 + "\n" + s3 + "\n" + s4;
    }
    
    public BigInteger getHash() {
        return hash;
    }
    
    public BigInteger getHashMerkle() {
        return fictionalMerkleRoot;
    }
    
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
    
    public String getHardness() {
        return hardness;
    }
    
    public long getNonce() {
        return nonce;
    }
    
    public ListIterator<MCTx> getTxs() {
        return txs.listIterator();
    }
}
