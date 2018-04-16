package minimalcoin;

import java.math.BigInteger;
import java.security.*;
// Netwok
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MCUtils {
    // Some CONSTANTS ==========================================================
    // Maximum difference between time stamp in a block and current time:
    public static final long TSMAXPERIOD = 5*60*1000;   // millis in 5 mins.
    // Period for transactions gathering to the next block before Proof of Work
    public static final long DELAY = 10*60*1000;  // millis in 10 mins.
    public static final long PERIOD = 10*60*1000;
    
    // Digital signature ECDSA =================================================
    public static byte[] sign(String str, PrivateKey priv) throws Exception {
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(priv);
        byte[] strBytes = str.getBytes("UTF-8");
        sig.update(strBytes);
        return sig.sign(); 
    }
    
    // str: string to validate; sigBytes: str signature; pub: public key to validate
    public static boolean verify(String str, byte[] sigBytes, PublicKey pub) throws Exception {
        Signature sigV = Signature.getInstance("SHA256withECDSA");
        sigV.initVerify(pub);
        byte[] strBytes = str.getBytes("UTF-8");
        sigV.update(strBytes);
        return sigV.verify(sigBytes);             
    }
    
    // Double hash function: first SHA-256 then SHA-224 ========================
    public static BigInteger dHash224(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bytes);
        byte[] intermHash = md.digest();
        md = MessageDigest.getInstance("SHA-224");
        md.update(intermHash);
        return new BigInteger(1, md.digest());
    }
    
    // Network =================================================================
    private static Receiver receiver;
    private static Sender sender;
    //private static Object oRec; //
    
    // Sender
    public static void sendObject(Object o, InetAddress remoteAddr) {
        sender = new Sender(o, remoteAddr);
    }
    
    private static class Sender {  
        public Sender(Object o, InetAddress remoteAddr) {
            try {
                Socket sSender = new Socket(remoteAddr, 6789);
                ObjectOutputStream oOut = new ObjectOutputStream(sSender.getOutputStream());
                oOut.writeObject(o);         
                sSender.close();
            } 
            catch (IOException ex) {
                Logger.getLogger(MCUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Receiver
    public static void initRec() throws Exception {
        receiver = new Receiver();
        //Thread.sleep(500);
        receiver.start();   // A new thread due Socket.accept()
    }
    
    public static Object rec() throws Exception {
        return Receiver.getObject();
    }
    
    private static class Receiver extends Thread { 
        //
        private static Object oRec;
        
        @Override
        public void run() { 
            try {  
                oRec = null;   // Re-init
                ServerSocket sSocket = new ServerSocket(6789);
                Socket socket = sSocket.accept();
                ObjectInputStream oIn = new ObjectInputStream(socket.getInputStream());
                oRec = oIn.readObject();
                socket.close();
                sSocket.close();
            } 
            catch (IOException | ClassNotFoundException e) {
                Logger.getLogger(MCUtils.class.getName()).log(Level.SEVERE, null, e);
            }
        }
               
        public static Object getObject() throws Exception {
            while(oRec == null){} // It's necessary a while to let run() act
            return oRec;
        }
    }

    // Timer ===================================================================
    public void startTimer(TimerTask task, long delay, long period) throws Exception {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, delay, period);
    }
    
}