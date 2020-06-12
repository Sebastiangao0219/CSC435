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

import java.io.*; //import input and output libraries
import java.net.*; //import networking libraries

//Class declaration
public class JokeClientAdmin {
	// Declare varibales with static idetifier so that they can be used in the whole program
  private static String mode;//for mode changing
  static boolean secondaryServer = false;//set a boolean value to false, to check if it's a second server
  static boolean switchingServerStatus = false;//set a boolean value to false, to check server status
  static boolean switchingMode = false;//set a boolean value to false, for mode switching
  static int port = 0;//Initializing port to 0, we can use it in the codes below
  static String primaryName;//Declaring a string value as primaryName
  static String secondaryName;//Declaring a string value as secondaryName

  public static void setMode(String mode) {//a method for mode setting
    JokeClientAdmin.mode = mode;
  }

  // This is the main method.
  public static void main(String[] args) {
    JokeClientAdmin admin = new JokeClientAdmin();//Instantiating an object of JokeClientAdmin
    /*make sure changing starts from joke mode*/
    String mode = "Proverb";
    //server port switching
    Boolean switchserver = true;
    setMode(mode);
    System.out.println(("Sebastian Gao's JokeClientAdmin"));//print this at the beginning of the program

    //If no extra argument added at the end of command line, then it's local host
    if (args.length < 1) {
      primaryName = "localhost";//set primaryName as local host
      port = 5050;//set port to 5050
      /*print current server and port*/
      System.out.println("Primary server: " + primaryName + ", port 4545. JokeClientAdmin running at port:" + port);
    }else{
    	//If 'secondary' is added at the end of command line, then it's another host
      if (args.length == 2) {
        secondaryName = args[1];
        port = 5051;//set port to 5051
        System.out.println("<S2>Secondary server: " + secondaryName + ", port 4546. JokeClientAdmin running at port:" + port);
      }
    }

    /*get input stream from Standard in and create buffer to read it into buffer*/
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    try{//use 'try-catch' method to run the code and catch the possible exceptions
    	// Declare a String variable adminInput initializing with empty string
      String adminInput = "";
      // guide users of what to type to choose doing something
      System.out.println("Press return to switch mode, type 's' to toggle server, type 'quit' to exit program.");
      	do {
      		//print out the remaing data in the buffer on to the screen 
        	System.out.flush();
        	adminInput = in.readLine();//read standard in as String and assign to variable adminInput
        	if (adminInput.indexOf("quit") < 0) {
          	if (adminInput.equals("s")) {//if standard in is 's'
            	if (switchserver) {//if switchserver is true
              	switchingMode = true;//set switchingMode to true
              	primaryName = secondaryName;
                  /*print information of host and port*/
              		System.out.println("Communicating with localhost at the moment, port 4546.");
              		switchserver = false;//set switchserver to false
            	}else{//if switchserver is false
              	switchingMode = true;//set switchingMode to true
              	primaryName = "localhost";//set primaryName to 'localhost'
              	switchserver = true;//tuen switchserver to true
              	System.out.println("Communicating with localhost at the moment, port 4545.");
            	}
            }else{//if standard in is string other than 's'
            	switchingMode = true;//set switchingMode to true
              //call the method communicateWithServer to conncet to server
            	communicateWithServer(adminInput, primaryName, port);
            	adminInput = "";//set adminInput to empty string
          	}
        	}
      	}while(adminInput.indexOf("quit") < 0);//quit the program if 'quit' typed in
      	System.out.println("User has terminated program.");//print notification of program closed
    }catch(IOException x){// catch the possilbe exception
      //telling the reason of the error, easier for us to debug
      x.printStackTrace();
    }
  }
  	

  //a method deal with all the communications with server.
  static void communicateWithServer(String name, String primaryName, int port) {
    Socket sock; //a Socket variable decaration, create a socket to communicate with server
    BufferedReader fromServer; //a BufferedReader variable declaration, so we can read stream into this variable
    PrintStream toServer; //a PrintStream variable declaration，we can send data to server through this  
    String textFromServer; //a String variable declaration，we can store the data from server to this variable
    try {//using the 'try-catch' method to catch the possible exceptions
    	sock = new Socket(primaryName, port);//create a new socket, using primaryName and port as arguments and assign to sock variable
    	fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));// read stream from server
    	toServer = new PrintStream(sock.getOutputStream()); //send data to server

      if (switchingServerStatus) {//if switchingServerStatus is true
        textFromServer = fromServer.readLine();//read stream as string from server and store them in textFromServer
        /*if textFromServer is true then assign true to JokeClientAdmin.secondaryServer, else assign false to JokeClientAdmin.secondaryServer*/
        JokeClientAdmin.secondaryServer = Boolean.parseBoolean(textFromServer);
        switchingServerStatus = false;//set switchingServerStatus to false
      }

      if (switchingMode) {//if switchingMode is true
        toServer.println(mode);//call method println to send modeto Server
        toServer.flush();//refresh output stream so as to make sure to send the stream to Server
        /*read stream as string from server and store them in textFromServer*/
        textFromServer = fromServer.readLine();
        // Reading stream less and equal to 2 lines from server
        for (int i = 1; i <= 2; i++) {
          textFromServer = fromServer.readLine();
          if (textFromServer != null){// if textFromServer is empty
          	System.out.println(textFromServer);//print out textFromServer
          }
        }
				switchingMode = false;//set switchingMode to false
      }
      sock.close();//close socket everytime we finish it

    }catch(IOException x){//catch the possible exception
      System.out.println("Error: Socket error.");//print error
      //trace up stack and print errors related to what caused exception
      x.printStackTrace();//telling the reason of the error, easier for programmers to debug
    }
  }

}