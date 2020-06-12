/*--------------------------------------------------------

1. Name / Date:Liang Gao / Apr 16, 2020 

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_241-b07

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeServer
> java JokeServer secondary
> java JokeClient
> java JokeClient localhost localhost
> java JokeClientAdmin



5. List of files needed for running the program.

e.g.:

 a. checklist-joke.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

6. Notes:This is like the most tough assignment I have ever met, I saw a lot of instances and 
watched a lot of videos about web program. To be honest, I learnt a lot from it. There are sitll
some bugs in my program, but it can be compiled and run.

----------------------------------------------------------*/

import java.io.*; // import imout and output libraries
import java.net.*; // input networking libraries to connet between server and client
import java.util.HashMap;//import HashMap in oder to create HashMap in the program
import java.util.Random;//import Random to generate random numbers


/*class definition, this class is for recording the state of client*/ 
class ClientState {
  // Client record variables initialized. showedJokes and showedProverbs arrays will keep track of which jokes have been seen 0 - not seen 1 - seen
  /**/
  String name;//a declaration of variable with String type, name of joke or proverb
  String id;//a declaration of variable with String type, index of joke or proverb
  int[] showedJokes = {0, 0, 0, 0};//an array of showed jokes with 0 which means not showed, 1 means showed
  int[] showedProverbs = {0, 0, 0, 0};//an array of showed proverbs with 0 which means not showed, 1 means showed

  // setters and getters
  public String getName() {//get name
    return name;
  }

  public String getId() {//get id which is index
    return id;
  }

  public int[] getshowedJokes() {//get showed jokes
    return showedJokes;
  }

  public int[] getshowedProverbs() {//get showed proverbs
    return showedProverbs;
  }

  public void setName(String name) {//name setting
    this.name = name;
  }

  public void setId(String id) {//id setting
    this.id = id;
  }

  public void setshowedJokes(int[] showedJokes) {//showed jokes setting
    this.showedJokes = showedJokes;
  }

  public void setshowedProverbs(int[] showedProverbs) {//showed proverbs setting
    this.showedProverbs = showedProverbs;
  }
}


/*class definition, this AdminLooper class is for creating new thread*/
class AdminLooper implements Runnable {//the Runnable interface is implemented by class AdminLooper

  public static boolean adminControlSwitch = true;//to use it as a condition in the while loop

  public void run(){ // running the loop for listening from client
    System.out.println("In the admin looper thread");//inform the user of what current thread is this
    
    int q_len = 6; //six requests at most to queue in the OS
    int port = 5050;  // a different port for ClientAdmin
    Socket sock;//a Socket variable decaration, create a socket to communicate with client

    try{//using the 'try-catch' method to catch the possible exceptions
      ServerSocket servsock = new ServerSocket(port, q_len);//creating socket on server side
      while (adminControlSwitch) {//a while loop, to read information continuously
        //Waiting for next connection from AdminClient
        sock = servsock.accept();
        new ClientAdminWorker (sock).start(); //start to run this thread
      }
    }catch (IOException ioe) {System.out.println(ioe);}//catch exception and print error
  }
}


//JokeClientAdmin Worker thread
class ClientAdminWorker extends Thread {


  Socket sock; //a Socket variable decaration, receive connection request from client

  ClientAdminWorker(Socket s) {sock = s;}//constructor of class ClientAdminWorker who encapsulates with a Socket s and assign s to sock
  

  /*overwrite method run to run the code*/ 
  public void run() {
    PrintStream clientOut = null; /*Declare a PrintStream variable for data sending*/
    BufferedReader clientIn = null; /*Declare a BufferedReader variable for the sake of read in data from client*/

    try {//using the 'try-catch' method to catch the possible exceptions
      /*get input stream from client after connected and create buffer to read input stream*/
      clientIn = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
      /*send out the stream from client*/
      clientOut = new PrintStream((sock.getOutputStream()));
      try {//using the 'try-catch' method to catch the possible exceptions

        /* a boolean value clientAdmin will be sent out*/
        clientOut.println(JokeServer.secondarySever);
        /* read the input stream from client as string and assign to JokeServer.name*/
        JokeServer.name = clientIn.readLine();
        /* if current mode is joke, change it to proverb mode*/
        if (JokeServer.mode.equals("Joke")) {
          JokeServer.mode = "Proverb";
        } else {/*if current mode is not joke, change it to joke mode*/
          JokeServer.mode = "Joke";
        }

        /* assign current mode to a String variable*/
        String stateNotice = "Switched mode, currently in " + JokeServer.mode + " mode.";
        /*print current mode on the screen to inform users what the current mode is*/
        System.out.println(stateNotice); 

        clientOut.println(stateNotice);


      } catch (IOException x) {//catch the Exception once it occurred
        System.out.println("Server read error");//simply print error
        x.printStackTrace();//telling the reason of the error, easier for programmers to debug
      }
      sock.close(); //we should close the sock everytime we done it
    } catch (IOException e) {// Catching the exception when we do getInputStream and OutputStream
      System.out.println(e);//print error
    }
  }
}

class Worker extends Thread {

  Socket sock; //a Socket variable decaration, receive connection request from client
  Worker(Socket s) {sock = s;}//constructor of class Worker who encapsulates with a Socket s and assign s to sock

  /*overwrite method run to run the code*/ 
  public void run() {
    PrintStream out = null; /*Declare a PrintStream variable for data sending*/
    BufferedReader in = null; /*Declare a BufferedReader variable for the sake of read in data from client*/

    try {//using the 'try-catch' method to catch the possible exceptions
      /*get input stream from client after connected and create buffer to read input stream*/
      in = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
      /*send out the stream from client*/
      out = new PrintStream((sock.getOutputStream())); 
      try {//using the 'try-catch' method to catch the possible exceptions 
        /* read the input stream from client as string and assign to JokeServer.name*/
        JokeServer.name = in.readLine(); 
        /* Print jokes/proverbs on the screen as the user request*/
        System.out.println("Here is the " + JokeServer.mode + ", " + JokeServer.name + "!"); 
        /* read the input stream from client as string and assign to JokeServer.id*/
        JokeServer.id = in.readLine();

        /*If the client id is stored in clientData Hashtable, we take it out, if not, we create a new client record*/
        if (JokeServer.clientData.containsKey(JokeServer.id)) {
          JokeServer.cs = JokeServer.clientData.get(JokeServer.id);//get the item in clientData Hashtable and assign to JokeServer.cs
          JokeServer.id = JokeServer.clientData.get(JokeServer.id).id;//get the id in clientData Hashtable and assign to JokeServer.id
          JokeServer.name = JokeServer.clientData.get(JokeServer.id).name;//get the name in clientData Hashtable and assign to JokeServer.name
          JokeServer.jokesShowed = JokeServer.clientData.get(JokeServer.id).showedJokes;//get the showedJokes in clientData Hashtable and assign to JokeServer.jokesShowed
          JokeServer.proverbsShowed = JokeServer.clientData.get(JokeServer.id).showedProverbs;//get the showedProverbs in clientData Hashtable and assign to JokeServer.proverbsShowed
        }else{
          ClientState user = new ClientState();//creating a new ClientSate user
          user.setName(JokeServer.name);//name setting
          user.setId(JokeServer.id);//id setting
          JokeServer.clientData.put(JokeServer.id, user);//inserting id into the clientData Hashtable
          JokeServer.cs = JokeServer.clientData.get(JokeServer.id);//get the item in clientData Hashtable and assign to JokeServer.cs
        }

        /*call the sendBackToClient method to send the name back to the client with two arguments JokeServer.name and out*/
        sendBackToClient(JokeServer.name,out); 

      }catch(IOException x) {//catch the Exception once it occurred
        System.out.println("Server read error");// print out error ption occurred. 
        x.printStackTrace();//telling the reason of the error, easier for programmers to debug
      }
      sock.close(); //we should close the sock everytime we done it
    } catch (
        IOException ioe) {// Catching the exception when we do getInputStream and OutputStream
      System.out.println(ioe);// pirnt error
    }


  }

  /*Method declaration with two parameters of type String and PrintStream respectively, this methos is used to send jokes/proverbs to the client*/
  public void sendBackToClient(String name, PrintStream out) {
    Message message = selectJokesAndProverbs(JokeServer.cs);//select out jokes or proverbs and assign it to message
    //if the current mode is joke, we print the joke, and print out joke cycles if not null, oterwise print a proverb and records proverb cycles.
    if (JokeServer.mode.equals("Joke")) {// if the current mode is joke
      if (message.getJokeCycle() != null) {//if joke cycle is not null
        out.println(message.getJokeCycle());//get cycle
      }
      out.println(JokeServer.getJoke(message.nextJoke));//print joke

    }else{
      if (message.getProverbCycle() != null) {//if proverb cycle is not null
        out.println(message.getProverbCycle());//get proverb cycle
      }
      out.println(JokeServer.getProverb(message.nextJoke));//print proverb
    }
  }

  /* Method declaration with parameter of type ClientState, it selects which joke or proverb is next in a random way, 
   * if the joke or proverb showed already, it will select another one which is not showed and make a flag on it.
   * After all the jokes or proverbs showed up, reset array to all not show.
   */

  public Message selectJokesAndProverbs(ClientState cs) {
    int nextIndex = 0;
    String nextJoke = "";
    HashMap<String, String> jokeSets = new HashMap<>();//hash map creating
    HashMap<String, String> proverbSets = new HashMap<>();//hash map creating
    /* Inserting keys and values to the jokeSets hashtable*/
    jokeSets.put("0", "JA");
    jokeSets.put("1", "JB");
    jokeSets.put("2", "JC");
    jokeSets.put("3", "JD");
    /* Inserting keys and values to the proverbSets hashtable*/
    proverbSets.put("0", "PA");
    proverbSets.put("1", "PB");
    proverbSets.put("2", "PC");
    proverbSets.put("3", "PD");

    Message message = new Message();// create a new message of type Message
    Random randomNum = new Random();// create a random number
    int randomIndex = randomNum.nextInt(4);//got random number range from 0 to 4(not included)

    if (JokeServer.mode.equals("Joke")) {//if current mode is joke, 
      boolean showedAllJokes = true;//declare a boolean value and initialize to true
      int[] showedJokes = cs.getshowedJokes();//declare an array of int type assign with client state
      for (int i = 0; i < showedJokes.length; i++) {//iterating the array
        if (showedJokes[i] != 1) {//if the joke has not been showed
          showedAllJokes = false;//set showedAllJokes to false
          break;
        }
      }

      if (showedAllJokes) {//if all the jokes have been showed up
        System.out.println("\nJOKE CYCLE COMPLETED\n");//print info to inform the user that all the jokes have been showed up
        showedJokes[0] = 0;//reset joke to not seen
        showedJokes[1] = 0;//reset joke to not seen
        showedJokes[2] = 0;//reset joke to not seen
        showedJokes[3] = 0;//reset joke to not seen
        message.setJokeCycle("JOKE CYCLE COMPLETED\n");//call set cycle method
      }

      if (showedJokes[randomIndex] == 0 && showedAllJokes) {//conditoins
        showedJokes[randomIndex] = 1;//set to one
        cs.setshowedJokes(showedJokes);//set showed jokes
        nextJoke = jokeSets.get(String.valueOf(randomIndex));//get joke and assign to next joke,cast randomIndex from int to String
        message.nextJoke = nextJoke;//assigning next joke to message
        return message;//return
      }else{
        for (int i = 0; i < showedJokes.length; i++) {//iterating the array
          if (showedJokes[i] == 0) {
            showedJokes[i] = 1;
            cs.setshowedJokes(showedJokes);
            nextJoke = jokeSets.get(String.valueOf(i));
            message.nextJoke = nextJoke;
            break;
          }

        }
        return message;
      }
    }else{//the same way as how to deal with jokes

      boolean showedAllProverbs = true;//declare a boolean value and initialize to true
      int[] showedProverbs = cs.getshowedProverbs();//declare an array of int type assign with client state

      for (int i = 0; i < showedProverbs.length; i++) {//iterating the array
        if (showedProverbs[i] != 1) {//if the proverb has not been showed
          showedAllProverbs = false;//set showedAllProverbs to false
          break;
        }
      }

      if (showedAllProverbs) {//if all the proverbs have been showed up
        System.out.println("\nPROVERB CYCLE COMPLETED\n");//print info to inform the user that all the proverbs have been showed up
        showedProverbs[0] = 0;//reset proverb to not seen
        showedProverbs[1] = 0;//reset proverb to not seen
        showedProverbs[2] = 0;//reset proverb to not seen
        showedProverbs[3] = 0;//reset proverb to not seen
        message.setProverbCycle("PROVERB CYCLE COMPLETED\n");//call set cycle method
      }

      if (showedProverbs[randomIndex] == 0 && showedAllProverbs) {
        showedProverbs[randomIndex] = 1;
        cs.setshowedJokes(showedProverbs);
        nextJoke = proverbSets.get(String.valueOf(randomIndex));
        message.nextJoke = nextJoke;
        return message;
      } else {

        for (int i = 0; i < showedProverbs.length; i++) {
          if (showedProverbs[i] == 0) {
            showedProverbs[i] = 1;
            cs.setshowedProverbs(showedProverbs);
            nextJoke = proverbSets.get(String.valueOf(i));
            message.nextJoke = nextJoke;
            break;
          }
        }
      }
      return message;
    }
  }

//delcare a Message class for data transfer 
class Message {
  //variables decalration
  String nextJoke;
  String jokeCycle;
  String proverbCycle;
  
  //setters and getters
  public String getNextJoke() {
    return nextJoke;
  }

  public void setNextJoke(String nextJoke) {
    this.nextJoke = nextJoke;
  }

  public String getJokeCycle() {
    return jokeCycle;
  }

  public void setJokeCycle(String jokeCycle) {
    this.jokeCycle = jokeCycle;
  }

  public String getProverbCycle() {
    return proverbCycle;
  }

  public void setProverbCycle(String proverbCycle) {
    this.proverbCycle = proverbCycle;
  }
  }
}


// The most important class
public class JokeServer {

  /*Declarng varibales with static identifier for the sake of using them in the whole project.*/
  static String mode = "Joke";
  static String name;
  static String id = "";
  static int[] jokesShowed;
  static int[] proverbsShowed;
  static ClientState cs;
  static Boolean secondarySever = false;

  static HashMap<String, String> jokes = new HashMap<>();//Creating hashmap to store jokes
  static HashMap<String, String> proverbs = new HashMap<>();//Creating hashmap to store proverbs
  static HashMap<String, ClientState> clientData = new HashMap<>();//Creating hashmap to store client datas

  /* A method declaration, this method is used to get jokes correspond to the certian key*/ 
  public static String getJoke(String key) {
   /*intersing jokes to the HashMap*/
    jokes.put("JA", "What did the 0 say to the 8?  Nice belt!");
    //showing user's name in the joke to make it more fun
    jokes.put("JB", "Why " + name + " has more hair than before? He stopped coding!");
    jokes.put("JC", "What does Charles Dickens keep in his spice rack? The best of thymes, the worst of thymes.");
    jokes.put("JD", "What did the lest eye say to the right eye?  Between you and me, something smells");

    /*The format when a joke is gave out*/
    if (JokeServer.secondarySever) {
      return "<S2> " + key + " " + name + ": " + jokes.get(key);//format like this if it's a secondary server
    }else{
      return key + " " + name + ": " + jokes.get(key);//format like this if it's primary server
    }
  }

  /* A method declaration, this method is used to get jokes correspond to the certian key*/ 
  public static String getProverb(String key) {
    /*intersing provebs to the HashMap*/
      proverbs.put("PA", "A journey of thousand miles begins with a single step.");
      proverbs.put("PB", "All good things come to an end.");
      proverbs.put("PC", "Always put your best foot forward.");
      proverbs.put("PD", "An apple a day keeps the doctor away.");

    /*The format when a proverb is gave out*/
    if (JokeServer.secondarySever) {
      return "<S2> " + key + " " + name + ": " + proverbs.get(key);//format like this if it's a secondary server
    }else{
      return key + " " + name + ": " + proverbs.get(key);//format like this if it's primary server
    }
  }

  // This is the main method.
  public static void main(String[] args) throws IOException {

    String mode = "Joke"; // set the mode to Joke by default.
    Socket sock; // Declaring a socket to receive connect request from client
    int q_len = 6; // six requests at most to queue in the OS
    int port = 0; // A port declaration, starts with 0 cuz there are 2 servers in the program, we have to decide which port should be used according to the situation

    AdminLooper AL = new AdminLooper(); // create a thread for ClientAdmin
    Thread t = new Thread(AL);
    t.start();  // run the thread, wating for Admin connection

    System.out.println("Sebastian Gao's Joke Server running.");

    //if it's primary server 
    if (args.length < 1) {//it means no extra command added behind java.JokeServer
      port = 4545;//set port to 4545
      //print server status to inform the user what server is being used
      System.out.println("Primary server: localhost, port " + port + ". JokeClientAdmin using port 5050.");
    }else{//if it's secondary server, which mean some command added after java.JokeServer
      port = 4546;//set port to 4546
      System.out.println("<S2>Secondary server: localhost, port " + port + ". JokeClientAdmin using port 5051.");
      secondarySever = true;//this is for toggle port numbers
    }
    //Print to the screen to inform users which mode is under running
    System.out.println("Start from "+ JokeServer.mode + " mode.");
    //Creating a ServerSocket to listen at port, waiting for the connection form client
    ServerSocket serverSock = new ServerSocket(port, q_len);
    while (true) {// while loop
      sock = serverSock.accept(); // Waiting for the connection from client
      //Create a new worker thread (multi-threaded server) and pass it a socket, then start it
      new Worker(sock).start();//starts the new thread to receive from client

    }
  }
}





