package minimalcoin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ListIterator;

public class MinimalCoin extends Thread {
    private static final MCWallet wallet = new MCWallet();
    private static final MCKeyStore keyStore = new MCKeyStore();
    private static final MCBChain blockchain = new MCBChain();
    
    public static void main(String[] args) throws Exception {     
        // 1. Getting initial state
        initState();
        
        // 2. Creating an output example and mobilizing value created above. 
        // 2.1 Generate a new beneficiary MCAddress and its key par, and store in key store 
        MCAddress addr1 = new MCAddress();
        addr1.toKeyStore(keyStore);
        // 2.2 New output to transfer the same value
        MCOutput out1 = new MCOutput(addr1.getAddress(), BigDecimal.valueOf(100.00));
        // 2.3 New 'ordinary' input and new transaction. We will use (from the wallet) previous out0
        // 2.3.1 Obtaining a list of output from wallet to reach jointly the value of output (100.00)
        ListIterator<MCOutput> iterOuts = wallet.locateValue(new BigDecimal("100.00")).listIterator();
        // 2.3.2 With first returned output we start to build the transaction...
        MCTx tx1 = new MCTx(new MCInput(iterOuts.next(), keyStore), out1);
        // ...while next needed outputs, if any, are added to transaction
        while(iterOuts.hasNext()){
            tx1.addInput(new MCInput(iterOuts.next(), keyStore));
        }
        // 2.3.3 New tx is confirmed, completed, verified, and sent to network
        tx1.terminate(wallet, addr1.getKeyPar().getPrivateKey());
        System.out.println("Wallet balance: " + wallet.getBalance());
        // 2.4 Receiving a tx from network (sent with MCTx.terminate(), last step) 
        Object temp = MCUtils.rec();
        if (temp instanceof MCTx){
            System.out.println("Tx received in MC main(): " + (MCTx)temp);
            System.out.println("Tx received: validate result " + ((MCTx)temp).validate());
        }
        // 2.5 Including the received tx in a new block...
        MCBlock block = new MCBlock((MCTx)temp, MCBChain.getLastBlock().getHash());//new BigInteger("0")); 
        // ...and confirm the block to check and send it to the network
        block.terminate();
        // 2.6 Receiving block from network (sent with MCBlock.terminate(), last step)
        Object temp2 = MCUtils.rec();
        if (temp2 instanceof MCBlock) 
            System.out.println("Block received in MC main(): " + ((MCBlock)temp2).toString());
        // 2.7 Including received block in block chain
        blockchain.addBlock((MCBlock)temp2, wallet, keyStore);
    }
    
    private static void initState() throws Exception {
        // 1. Generating an initial state with a coin creator output, included in a transaction,
        // included in a block, included in the block chain
        // 1.1 New coin creator special output with a value of 100,00 units, which
        // will be used as reference for the consequent input
        //MCOutput outCoinbase = new MCOutput("abcde", BigDecimal.valueOf(100.00));
        // 1.2 New special input using above output as previous output. Outpoint
        // is settled down with previous transaction id '0' and index '0'
        MCInput inp0 = new MCInput("abcde", new MCOutput("abcde", BigDecimal.valueOf(100.00))); //new MCInput("abcde", outCoinbase);
        // 1.3 Generate a new user MCAddress and its key par, and store in key store
        MCAddress addr0 = new MCAddress();
        addr0.toKeyStore(keyStore);
        // 1.4 A new 'ordinary' output to send new value to the user address
        MCOutput out0 = new MCOutput(addr0.getAddress(), BigDecimal.valueOf(100.00));
        // 1.5 Finally, we include input and output in a value creator transaction...
        MCTx tx0 = new MCTx(inp0, out0); 
        tx0.terminate(wallet, addr0.getKeyPar().getPrivateKey());
        // ...which is included in a block (the first block in the block chain)...
        MCBlock block0 = new MCBlock(tx0, new BigInteger("0"));
        block0.terminate();
        // ...which is included in the block chain!!!
        blockchain.addBlock(block0, wallet, keyStore);
        // END of initial state setup: a block chain, which include a block, 
        // which include a transaction, which include an unspent output
    }
}