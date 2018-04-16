package minimalcoin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

public class MCBChain {
    private static final ArrayList<MCBlock> blockchain = new ArrayList<>(); 
    private static final HashMap<BigInteger, Integer> txIndex = new HashMap<>(); 
    private static final HashMap<BigInteger, MCOutput> uTxO = new HashMap<>();
    
    public boolean addBlock(MCBlock block, MCWallet wallet, MCKeyStore kStore) throws Exception {
        // 1. Validating block (taking into account if first block in chain)
        if(size() > 0 && !block.validate()) 
            return false;  
        // 2. Adding block to block chain
        blockchain.add(block);
        // 3. Update transactions index record
        txIndex.put(block.getHash(), blockchain.size()); 
        // 4. Update unspent outputs in UTxO and wallet (only outputs)
        ListIterator<MCTx> txIter = block.getTxs();
        // For each tx...
        while(txIter.hasNext()) {
            MCTx tx = txIter.next();
            // ...we remove consumed (spent) outputs...
            ListIterator<MCInput> txInp = tx.getInputs();
            while(txInp.hasNext()){
                uTxO.remove(txInp.next().getPrevOut().getOutPoint().getOutPointID());
            }
            // ...and include new unspent outputs in UTxo...
            ListIterator<MCOutput> txOut = tx.getOutputs();
            while(txOut.hasNext()){
                MCOutput output= txOut.next();
                uTxO.put(output.getOutPoint().getOutPointID(), output);
                // ...and in wallet where appropriate (only outputs; inputs are updatede by terminating txs)...
                BigInteger addr = output.getAddress();
                // ...checking if output is aiming to an address of our own property
                if(kStore.containsKey(addr)) {
                    if(wallet.contains(addr))
                        wallet.add(output, addr);   
                    else{
                        wallet.add(new MCWalletEntry(output, kStore.get(addr).getKeyPar().getPublicKey()));
                    }
                }
            }
        }
        // 5. Log it
        System.out.println("> New block #" + (size()-1) + " added to blockchain: " + block.toString());
        // Everything right. Bye!
        return true;
    }
    
    public static ListIterator<MCBlock> bcIter() {
        return blockchain.listIterator();
    }
    
    public static MCBlock getBlock(int index) {
        return blockchain.get(index);
    }
    
    public static MCBlock getLastBlock() {
        return blockchain.get(blockchain.size() - 1);
    }
    
    public static int getBlockIndex(BigInteger hash) {
        return txIndex.get(hash);
    }
    
    public static MCOutput getUnspOutput(BigInteger outPointID) {
        return uTxO.get(outPointID);
    }
    
    public static int getUtxoSize() {
        return uTxO.size();
    }
    
    public static int size() {
        return blockchain.size();
    }
    
}