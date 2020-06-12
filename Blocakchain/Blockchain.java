/*--------------------------------------------------------

1. Name / Date:Liang Gao / May 20, 2020

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_241-b07

3. Precise command-line compilation examples / instructions:

> javac -cp "gson-2.8.2.jar" Blockchain.java
> java -cp ".:gson-2.8.2.jar" Blockchain 0


4. Precise examples / instructions to run this program:

e.g.:

5. List of files needed for running the program.

e.g.:

 a. Blockchain.java
 b. gson-2.8.2.jar
 c. BlockInput0.txt
 d. BlockInput1.txt
 e. BlockInput2.txt

6. Notes:
I used the Sample codes that professor offered
I wanna say, OMG!! So many shortcomings are exposed when I'm doing this assignments, it's insane complicated.
I tried my best to work on it, but here is what I can get to. I didn't finish the key part and signature, etc.
I'm going to finish it later on.

----------------------------------------------------------*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.crypto.Cipher;

class BlockRecord {//class for informations about block
    /*these are the variables that we goona use*/
    String BlockID;
    String TimeStamp;
    String VerificationProcessID;
    String PreviousHash;
    UUID uuid;
    String Fname;
    String Lname;
    String SSNum;
    String DOB;
    String Diag;
    String Treat;
    String Rx;
    String RandomSeed; // Our guess. Ultimately our winning guess.
    String WinningHash;

    /* getters and setters*/
    public String getBlockID() {
        return BlockID;
    }

    public void setBlockID(String BID) {
        this.BlockID = BID;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String TS) {
        this.TimeStamp = TS;
    }

    public String getVerificationProcessID() {
        return VerificationProcessID;
    }

    public void setVerificationProcessID(String VID) {
        this.VerificationProcessID = VID;
    }

    public String getPreviousHash() {
        return this.PreviousHash;
    }

    public void setPreviousHash(String PH) {
        this.PreviousHash = PH;
    }

    public UUID getUUID() {
        return uuid;
    } // Later will show how JSON marshals as a string. Compare to BlockID.

    public void setUUID(UUID ud) {
        this.uuid = ud;
    }

    public String getLname() {
        return Lname;
    }

    public void setLname(String LN) {
        this.Lname = LN;
    }

    public String getFname() {
        return Fname;
    }

    public void setFname(String FN) {
        this.Fname = FN;
    }

    public String getSSNum() {
        return SSNum;
    }

    public void setSSNum(String SS) {
        this.SSNum = SS;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String RS) {
        this.DOB = RS;
    }

    public String getDiag() {
        return Diag;
    }

    public void setDiag(String D) {
        this.Diag = D;
    }

    public String getTreat() {
        return Treat;
    }

    public void setTreat(String Tr) {
        this.Treat = Tr;
    }

    public String getRx() {
        return Rx;
    }

    public void setRx(String Rx) {
        this.Rx = Rx;
    }

    public String getRandomSeed() {
        return RandomSeed;
    }

    public void setRandomSeed(String RS) {
        this.RandomSeed = RS;
    }

    public String getWinningHash() {
        return WinningHash;
    }

    public void setWinningHash(String WH) {
        this.WinningHash = WH;
    }

}

//class for ports setting
class Ports {
    //these are basic port numbers
    public static int KeyServerPortBase = 4710;
    public static int UnverifiedBlockServerPortBase = 4820;
    public static int BlockchainServerPortBase = 4930;
    //these are ports based on PID
    public static int KeyServerPort;
    public static int UnverifiedBlockServerPort;
    public static int BlockchainServerPort;

    public void setPorts(int PID) {//method for port setting with a parameter int
        KeyServerPort = KeyServerPortBase + (PID);
        UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + (PID);
        BlockchainServerPort = BlockchainServerPortBase + (PID);
    }
}


class UnverifiedBlockServer implements Runnable {//This is a server class for processing the unverified blocks
    BlockingQueue<BlockRecord> queue;//the queue of block declaration
    UnverifiedBlockServer(BlockingQueue<BlockRecord> queue) {//the consructor combine the priority queue to the local bariable
        this.queue = queue;
    }

    /*
     * The inner class is for processing the incoming unverified blocks
     */

    class UnverifiedBlockWorker extends Thread {
        Socket sock; // This is a socket for receiving unverified blocks

        UnverifiedBlockWorker(Socket s) {//constructor of class UnverifiedBlockWorker who encapsulates with a Socket s and assign s to sock
            sock = s;
        }

        public void run() {// run method
            try {
                /*get input stream from local after connected and create buffer to read input stream*/
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                StringBuilder builder = new StringBuilder();// string declaration
                String data;//
                while ((data = in.readLine()) != null){//if the read in stream is not null
                    builder.append(data);// append the data to builder
                }
//              String data = in.readLine();
                System.out.println("Put the block in priority queue: " + builder.toString() + "\n");
                //gson declaration for string to json format converting and vice versa
                Gson gson = new Gson();
                // Reading in and converting josn file to java object:
                BlockRecord blockRecordIn = gson.fromJson(builder.toString(), BlockRecord.class);
                queue.add(blockRecordIn);//adding the java object to queue
                sock.close();
            } catch (Exception x) {//exception catch
                x.printStackTrace();
            }
            try{
                Thread.sleep(2000);
            } catch (Exception x ){
                x.printStackTrace();
            }
        }
    }

    public void run() {
        int q_len = 6;// six requests at most to queue in the OS
        Socket sock;// Declaring a socket to receive connect request
        System.out.println("Starting the Unverified Block Server input thread using port: "
                + Integer.toString(Ports.UnverifiedBlockServerPort));
        try {
            //Creating a ServerSocket to listen at UnverifiedBlockServerPort, waiting for the connection
            ServerSocket servsock = new ServerSocket(Ports.UnverifiedBlockServerPort, q_len);
            while (true) {
                sock = servsock.accept(); // Waiting for the connection from client
                new UnverifiedBlockWorker(sock).start(); //starts the new thread
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

/*
 * This class is for consume the unverified blocks
 */
class UnverifiedBlockConsumer implements Runnable {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    BlockingQueue<BlockRecord> queue;
    BlockingQueue<BlockRecord> blockchainFinalList;//we use a queue as a blockchain
    int PID;

    public static String ByteArrayToString(byte[] ba){//a method to convert byte array to string
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for(int i=0; i < ba.length; i++){
            hex.append(String.format("%02X", ba[i]));
        }
        return hex.toString();
    }


    static String someText = "one two three";
    static String randString;
    /*Constructor to combine prioirty queue to the local variable.*/
    UnverifiedBlockConsumer(BlockingQueue<BlockRecord> queue, BlockingQueue<BlockRecord> blockchainFinalList) {
        this.queue = queue;
        this.blockchainFinalList = blockchainFinalList;
    }

    public void run() {
        //variable declarations
        BlockRecord data;
        PrintStream toServer;
        Socket sock;
        String newblockchain;
        String fakeVerifiedBlock;


        System.out.println("Starting the unverified block priority queue consumer thread.\n");
        try {
            while (true) { // Consuming blocks from the incoming queue. verify it and mulitcast new blockchain
                // blockchain
                data = queue.take(); // if no element in the queue, it will block until has new element
                System.out.println("Consumer got unverified block: " + data.getFname() + " " + data.getLname());
                int j;
                boolean isVerified = worker(data);//call the method to see if it's been verified
                //BlockRecord currBlock;
                /*
                 * we may receive blocks with the same data, but we store the data with the lowest verification timestamp
                 * in order to eliminate duplicate blocks
                 */
                if (isVerified == true) { //excludes most duplicates.
                    fakeVerifiedBlock = "<" + data.getFname() + " " + data.getLname() + " verified by P" + PID + " at time "
                            + Integer.toString(ThreadLocalRandom.current().nextInt(100, 1000)) + ">\n";
                    System.out.println(fakeVerifiedBlock);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    // Converting the java object to a JSON String:
                    String json = gson.toJson(data);
                    blockchainFinalList.add(data);// add the verified block to the blockchain list
                    try {//send this blockchain to all the processes in a json format
                        for (int i = 0; i < bc.numProcesses; i++) {
                            try {
                                sock = new Socket(bc.serverName, Ports.BlockchainServerPortBase + i);
                            } catch (ConnectException e) {
                                continue;
                            }
                            toServer = new PrintStream(sock.getOutputStream());
                            toServer.println(json);
                            toServer.flush(); // make the multicast
                            sock.close();
                        }
                    } catch (Exception e) {
                    }
                    Iterator<BlockRecord> iteratesBlock = blockchainFinalList.iterator();
                        while(iteratesBlock.hasNext()){
                            BlockRecord currBlock = iteratesBlock.next();
                            String valRandom = currBlock.getRandomSeed();
                        }


                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public  String randomAlphaNumeric(int count) {//a methos to generate random alphanumeric
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public boolean worker(BlockRecord block) throws InterruptedException {//this is the puzzle to verify blocks
        boolean isVerified = false;
        String concatString = block.getBlockID() + block.getFname() + block.getLname() + block.getDOB();
        String stringOut = ""; // put in the string of SHA256 string converted to HEX

        randString = randomAlphaNumeric(8);
        System.out.println("Number will be between 0000 (0) and FFFF (65535)\n");
        int workNumber = 0;     // Number will be between 0000 (0) and FFFF (65535), here's proof:
        workNumber = Integer.parseInt("0000",16); // parse the string number and converting it to a hex number
        System.out.println("0x0000 = " + workNumber);

        workNumber = Integer.parseInt("FFFF",16);
        System.out.println("0xFFFF = " + workNumber + "\n");

        try {

            for(int i=1; i<20; i++){ // For limiting how many times we gonna try
                randString = randomAlphaNumeric(8); // retrieve a random alphaNumeric seed string
                concatString = concatString + randString; // Concatenate it  with our input block data
                MessageDigest MD = MessageDigest.getInstance("SHA-256");//encrypt the input string
                byte[] bytesHash = MD.digest(concatString.getBytes("UTF-8")); // Get the hash value and put it in an array
                stringOut = ByteArrayToString(bytesHash); // convert the byte into string
                System.out.println("The Hash is: " + stringOut);

                workNumber = Integer.parseInt(stringOut.substring(0,4),16); // making the first 16 bits between 0000 (0) and FFFF (65535)
                System.out.println("First 16 bits in Hex and Decimal: " + stringOut.substring(0,4) +" and " + workNumber);
                if (!(workNumber < 10000)){  // if the number is not less than 10000, we did not make it
                    System.out.format("%d is not less than 10,000 so we did not solve the puzzle\n\n", workNumber);
                }
                if (workNumber < 10000){// if the number is less than 10000, we solved it
                    System.out.format("%d IS less than 10,000 so puzzle solved!\n", workNumber);
                    System.out.println("The seed (puzzle answer) was: " + randString);
                    block.setWinningHash(stringOut);//insert the generated hash into the block
                    block.setRandomSeed(randString);//insert the generated seed into the block
                    isVerified = true;
                    break;
                }
                try{Thread.sleep(2000);}catch(Exception e){}
            }
        }catch(Exception ex) {ex.printStackTrace();}

        return isVerified;
    }
}

// BlockchainWorker class, for comparing to existing blocks, make a replace if win
class BlockchainWorker extends Thread { // Class definition
    Socket sock;
    BlockingQueue<BlockRecord> blockchainFinalList;

    BlockchainWorker(Socket s,BlockingQueue<BlockRecord> blockchainFinalList) {////constructor of class UnverifiedBlockWorker who encapsulates with a Socket s and assign s to sock
        sock = s;
        this.blockchainFinalList = blockchainFinalList;
    }

    public void run() {//run method
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String data = "";
            String data2;
            while ((data2 = in.readLine()) != null) {
                data = data + data2;
            }
            System.out.println("         --NEW BLOCKCHAIN--\n" + data + "\n\n");
            System.out.println(data);

            if (Blockchain.PID == 0){//here we write the blocks to disk when PID = 0
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                // Write the JSON object to a file:
                try (FileWriter writer = new FileWriter("BlockchainLedger.json")) {
                    gson.toJson(blockchainFinalList, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sock.close();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
// BlockchainServer class
class BlockchainServer implements Runnable {
    BlockingQueue<BlockRecord> blockchainFinalList;//the real blockchain

    public BlockchainServer(BlockingQueue<BlockRecord> blockchainFinalList) {//constructor with blockchain in it
        this.blockchainFinalList = blockchainFinalList;
    }

    public void run() {
        int q_len = 6;
        Socket sock;
        System.out.println(
                "Starting the blockchain server input thread using port: " + Integer.toString(Ports.BlockchainServerPort));
        try {
            ServerSocket servsock = new ServerSocket(Ports.BlockchainServerPort, q_len);
            while (true) {
                sock = servsock.accept();
                new BlockchainWorker(sock, blockchainFinalList).start();
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

class PublicKeyServer implements Runnable {
    //public ProcessBlock[] PBlock = new ProcessBlock[3]; // One block to store info for each process.

    public void run(){
        int q_len = 6;
        Socket sock;
        System.out.println("Starting Key Server input thread using port: " + Integer.toString(Ports.KeyServerPort));
        try{
            ServerSocket servsock = new ServerSocket(Ports.KeyServerPort, q_len);
            while (true) {
                sock = servsock.accept();
                new PublicKeyWorker (sock).start();
            }
        }catch (IOException ioe) {System.out.println(ioe);}
    }
}

class PublicKeyWorker extends Thread { // Class definition
    Socket sock; // Class member, socket, local to Worker.
    PublicKeyWorker (Socket s) {sock = s;} // Constructor, assign arg s to local sock
    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String data = in.readLine ();
            System.out.println("Got key: " + data);
            sock.close();
        } catch (IOException x){x.printStackTrace();}
    }
}


/*-----------------------This part is for verify the Blockchain where I will keep continue----------------------------*/
class BlockJ {

    public static String CSC435Block =//dummy etry text
            "You will design and build this dynamically. For now, this is just a string.";

    public static final String ALGORITHM = "RSA"; //String variable for encryption

    //for block header
    public static String SignedSHA256;



    public BlockJ(String argv[]) {//constructor
        System.out.println("In the constructor...");
    }

    public void run(String argv[]) {

        System.out.println("Running now\n");

//        try {  // Remove the try block to see all the exceptions that might be raised in the method
//            DemonstrateUtilities(argv);
//        } catch (Exception x) {
//        }
        ;
    }

    public void DemonstrateUtilities(String args[]) throws Exception {
        System.out.println("\n =========> In DemonstrateUtilities <=========\n");
        /* CDE: Process numbers and port numbers to be used: */
        int pnum;
        int UnverifiedBlockPort;
        int BlockChainPort;

//        /* CDE If you want to trigger bragging rights functionality... */
//        if (args.length > 2) System.out.println("Special functionality is present \n");

        //process ID determination
        if (args.length < 1) pnum = 0;
        else if (args[0].equals("0")) pnum = 0;
        else if (args[0].equals("1")) pnum = 1;
        else if (args[0].equals("2")) pnum = 2;
        else pnum = 0; //process ID by default
        UnverifiedBlockPort = 4710 + pnum;
        BlockChainPort = 4810 + pnum;

        System.out.println("Process number: " + pnum + " Ports: " + UnverifiedBlockPort + " " +
                BlockChainPort + "\n");

        //time stamp generating
        Date date = new Date();
        //String T1 = String.format("%1$s %2$tF.%2$tT", "Timestamp:", date);
        String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
        String TimeStampString = T1 + "." + pnum + "\n"; // process ID is unique, collision is avoided
        System.out.println("Timestamp: " + TimeStampString);

        //hash generating for SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update (CSC435Block.getBytes());//calculate the certain string
        byte byteData[] = md.digest();//retrieve the encrpted data

        // convert from byte to hex
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        String SHA256String = sb.toString();
        //key generating
        KeyPair keyPair = generateKeyPair(999); // a random number should be used for our assignment

        byte[] digitalSignature = signData(SHA256String.getBytes(), keyPair.getPrivate());//get signature

        boolean verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), digitalSignature);//to see if verfied
        System.out.println("Has the signature been verified: " + verified + "\n");

        System.out.println("Hexidecimal byte[] Representation of Original SHA256 Hash: " + SHA256String + "\n");


        //sign the SHA256
        SignedSHA256 = Base64.getEncoder().encodeToString(digitalSignature);
        System.out.println("The signed SHA-256 string: " + SignedSHA256 + "\n");
        byte[] testSignature = Base64.getDecoder().decode(SignedSHA256);//signature test
        System.out.println("Testing restore of signature: " + Arrays.equals(testSignature, digitalSignature));

        verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), testSignature);//verift the signature
        System.out.println("Has the restored signature been verified: " + verified + "\n");


        //get the key in byte format
        byte[] bytePubkey = keyPair.getPublic().getEncoded();
        System.out.println("Key in Byte[] form: " + bytePubkey);
        //converting the key to string based on Base64
        String stringKey = Base64.getEncoder().encodeToString(bytePubkey);
        System.out.println("Key in String form: " + stringKey);

        String stringKeyBad = stringKey.substring(0,50) + "M" + stringKey.substring(51);
        System.out.println("\nBad key in String form: " + stringKeyBad);

        // Convert from string to byte
        byte[] bytePubkey2  = Base64.getDecoder().decode(stringKey);
        System.out.println("Key in Byte[] form again: " + bytePubkey2);
        //create a new X509EncodedKeySpec according to bytePubkey2
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(bytePubkey2);
        //convert the cartain secret key to a standard secret key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey RestoredKey = keyFactory.generatePublic(pubSpec);

        verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), testSignature);
        System.out.println("Has the signature been verified: " + verified + "\n");

        verified = verifySig(SHA256String.getBytes(), RestoredKey, testSignature);
        System.out.println("Has the CONVERTED-FROM-STRING signature been verified: " + verified + "\n");

        // Converting from bad string to byte
        byte[] bytePubkeyBad  = Base64.getDecoder().decode(stringKeyBad);
        System.out.println("Damaged key in Byte[] form: " + bytePubkeyBad);

        X509EncodedKeySpec pubSpecBad = new X509EncodedKeySpec(bytePubkeyBad);
        KeyFactory keyFactoryBad = KeyFactory.getInstance("RSA");
        PublicKey RestoredKeyBad = keyFactoryBad.generatePublic(pubSpecBad);

        verified = verifySig(SHA256String.getBytes(), RestoredKeyBad, testSignature);
        System.out.println("Has the CONVERTED-FROM-STRING bad key signature been verified: " + verified + "\n");

        /* here for simulating a puzzle work */
        System.out.println("We will now simulate some work: ");
        int randval = 27; // a random number
        int tenths = 0;
        Random r = new Random();
        for (int i=0; i<1000; i++){ // for loop whose upper limit is 1000
            Thread.sleep(100);
            randval = r.nextInt(100);
            System.out.print(".");
            if (randval < 4) {
                tenths = i;
                break;
            }
        }
        System.out.println(" <-- We did " + tenths + " tenths of a second of *work*.\n");



        /* Using public key for encrypt hash string */
        final byte[] cipherText = encrypt(SHA256String,keyPair.getPublic());

        /* Using private key for dencrypt hash string */
        final String plainText = decrypt(cipherText, keyPair.getPrivate());

        System.out.println("\nExtra encryption functionality in case you want it:");
        System.out.println("Starting Hash string: " + SHA256String);
        System.out.println("Encrypted Hash string: " + Base64.getEncoder().encodeToString(cipherText));
        System.out.println("Original (now decrypted) Hash string: " + plainText + "\n");

    }
    //method for sign data
    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(key);
        signer.update(data);
        return (signer.sign());
    }
    // method for verify data
    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initVerify(key);
        signer.update(data);

        return (signer.verify(sig));
    }
    // method for key pari generating
    public static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
        rng.setSeed(seed);
        keyGenerator.initialize(1024, rng);

        return (keyGenerator.generateKeyPair());
    }
    /* method for encryption  */
    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM); // Get RSA cipher object
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /* method for decryption*/
    public static String decrypt(byte[] text, PrivateKey key) {
        byte[] decryptedText = null;
        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new String(decryptedText);
    }
}
//this is a class for some regular processing
class bc {
    private static String FILENAME;// the .txt file gonna stored in
    static String serverName = "localhost";
    static String blockchain = "[First block]";
    static int numProcesses = 3; // Set this to match your batch execution file that starts N processes with args
    // 0,1,2,...N
    static int PID = 0; //process ID
    //this is a queue for storing blocks with smaller timestamp
    Queue<BlockRecord> ourPriorityQueue = new PriorityQueue<>(4, BlockTSComparator);

    /* Token indexes */
    private static final int iFNAME = 0;
    private static final int iLNAME = 1;
    private static final int iDOB = 2;
    private static final int iSSNUM = 3;
    private static final int iDIAG = 4;
    private static final int iTREAT = 5;
    private static final int iRX = 6;
    //a method to compare blocks by time stamp
    public static Comparator<BlockRecord> BlockTSComparator = new Comparator<BlockRecord>() {
        @Override
        public int compare(BlockRecord b1, BlockRecord b2) {
            String s1 = b1.getTimeStamp();
            String s2 = b2.getTimeStamp();
            if (s1 == s2) {
                return 0;
            }
            if (s1 == null) {
                return -1;
            }
            if (s2 == null) {
                return 1;
            }
            return s1.compareTo(s2);
        }
    };

    public void run(int pid) {
        System.out.println("Now running... \n");
        try {
            ListExample(pid);
        } catch (Exception x) {
        }
        ;
    }
    //a method to generate new block with a new block ID
    public void ListExample(int pid) {

        LinkedList<BlockRecord> recordList = new LinkedList<>();//a linked list to store block record

        switch (pid) {//file selection based on pid
            case 1:
                FILENAME = "BlockInput1.txt";
                break;
            case 2:
                FILENAME = "BlockInput2.txt";
                break;
            default:
                FILENAME = "BlockInput0.txt";
                break;
        }

        System.out.println("Reading data from file: " + FILENAME);

        try {
            /*Declare a BufferedReader variable for the sake of read in data from file*/
            BufferedReader br = new BufferedReader(new FileReader(FILENAME));
            String[] tokens = new String[10];
            String InputLineStr;
            String suuid;
            UUID idA;
            BlockRecord tempRec;

            //StringWriter sw = new StringWriter();
            int n = 0;
            while ((InputLineStr = br.readLine()) != null) {
                BlockRecord BR = new BlockRecord(); // Careful

                /* CDE For the timestamp in the block entry: */
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                Date date = new Date(n, n, n);
                String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
                String TimeStampString = T1 + "." + pid; // eliminate time stamp collisions!
                System.out.println("Timestamp: " + TimeStampString);
                BR.setTimeStamp(TimeStampString); // sort by TimeStamp

                /*
                 * Generating a random blockID
                 */
                suuid = new String(UUID.randomUUID().toString());
                BR.setBlockID(suuid);
                /* put the file data into the block record: */
                tokens = InputLineStr.split(" +"); // Tokenizing the input
                BR.setFname(tokens[iFNAME]);
                BR.setLname(tokens[iLNAME]);
                BR.setSSNum(tokens[iSSNUM]);
                BR.setDOB(tokens[iDOB]);
                BR.setDiag(tokens[iDIAG]);
                BR.setTreat(tokens[iTREAT]);
                BR.setRx(tokens[iRX]);
                recordList.add(BR);
                n++;
            }
            System.out.println(n + " records read." + "\n");


            Iterator<BlockRecord> iterator = recordList.iterator();//for block traversing
            while (iterator.hasNext()) {//add it to ourPriorityQueue if not empty
                ourPriorityQueue.add(iterator.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //sending data to all the processes
    public void MultiSend() {
        Socket sock;
        PrintStream toServer;
        try {

            if (!ourPriorityQueue.isEmpty()) {
                for (BlockRecord b : ourPriorityQueue) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    // Convert the Java object to a JSON String:
                    String json = gson.toJson(b);

                    for (int i = 0; i < numProcesses; i++) {// Send a sample unverified block to each server
                        try {
                            sock = new Socket(serverName, Ports.UnverifiedBlockServerPortBase + i);
                        } catch (ConnectException e) {
                            continue;
                        }
                        toServer = new PrintStream(sock.getOutputStream());
                        toServer.println(json);
                        toServer.flush();
                        sock.close();
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }



}

public class Blockchain {
    static int PID = 0; // Our process ID
    private static String FILENAME;
    public static Comparator<BlockRecord> BlockTSComparator = new Comparator<BlockRecord>() {//compare 2 records by time stamp
        @Override
        public int compare(BlockRecord b1, BlockRecord b2) {
            String s1 = b1.getTimeStamp();
            String s2 = b2.getTimeStamp();
            if (s1 == s2) {
                return 0;
            }
            if (s1 == null) {
                return -1;
            }
            if (s2 == null) {
                return 1;
            }
            return s1.compareTo(s2);
        }
    };
    //main method where the program starts
    public static void main(String argv[]) throws InterruptedException {

        int q_len = 6;// six requests at most to queue in the OS
        PID = (argv.length < 1) ? 0 : Integer.parseInt(argv[0]); // determine the process ID
        if (PID == 1) {Thread.sleep(1000); }
        if (PID == 2) {Thread.sleep(1000); }
        System.out.println("Sebastian Gao's BlockFramework. Control-c to quit.\n");
        System.out.println("Running on process " + PID + "\n");

        bc s = new bc(); //new object of class c
        s.run(PID);// run the method run
        /*create a priority queue to store blocks, the blocks are in the order of small to big*/
        BlockingQueue<BlockRecord> blockchainFinalList = new PriorityBlockingQueue<>(50, BlockTSComparator);
        /*create a priority queue to store unverifies blocks, the blocks are in the order from small to big*/
        BlockingQueue<BlockRecord> queue = new PriorityBlockingQueue<>(4, BlockTSComparator); // Concurrent queue for unverified blocks
        new Ports().setPorts(PID); // Creating the port number scheme, based on PID

        new Thread(new PublicKeyServer()).start(); // A new thread to process incoming public keys
        new Thread(new UnverifiedBlockServer(queue)).start(); // New thread for processing unverified blocks
        new Thread(new BlockchainServer(blockchainFinalList)).start(); // New thread for processing new blockchains
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        } // Waiting for the servers

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }// Waiting for the servers

        s.MultiSend(); // sending unverified blocks out to all the servers
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        } // Waiting for multicast example to the queue
        /*consuming the unverified blocks in the queue*/
        new Thread(new UnverifiedBlockConsumer(queue,blockchainFinalList)).start(); // Start consuming the queued-up unverified blocks
    }
}
