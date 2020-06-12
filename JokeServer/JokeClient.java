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
import java.io.*;//import input and output library
import java.net.*;//import network library for socket creation

public class JokeClient{//a class definition, this is a client class
  /*Variable declarations*/
  static String name;//user's name
  static int port = 0;//Initializing port to 0
 	static boolean switchingMode = false;//Initializing boolean value to false for change mode
 	static String secondaryName;//Declare a secondary name, assign the secondary server to it
  static String primaryName;//Declare a String variable as the name of primary server

	public static void main(String args[]){//main method, where the program starts
		Boolean switchserver = true;//Declare a boolean value and set it to true, it's for server transfer
		System.out.println("Sebastian Gao's Joke Client");//Print out what is this program
    	if (args.length < 1) {//it means no extra command added behind java.JokeClient
      		primaryName = "localhost";//set server name
          port = 4545;//set port to 4545
      		System.out.println("Primary server: localhost, port: " + port + ". JokeClientAdmin running at port 5050.");//print server and port 
    	}else{
      		//System.out.println("Primary server: localhost, port 4545. JokeClientAdmin running at port 5050.");
      		if (args.length == 2) {//is means the server on the other host
        		secondaryName = args[1];//the name of the other host form standard in
            port = 4546;//set port to 4546
            /*print second server and the second port*/
        		System.out.println("<S2>Secondary server: " + secondaryName + ", port: " + port +". JokeClientAdmin running at port 5051.");
       		}
    	}

		/*get input stream from Standard in and create buffer to read it into buffer*/
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try{//use 'try-catch' method to run the code and catch the possible exceptions
      //varaibles declarartion
			String name;//this is the user's name
			String input;//read in stream to this variable
      int id = 0;//Initializing id to 0
      System.out.println("Welcom! Please enter your name to begin the program.");//print welcome 
      //print out any data stored in the buffer
      System.out.flush();

      name = in.readLine();//read standard in as String and assign to name
      id = 0 + (int) (Math.random() * ((1000000 - 0) + 1));//generate a random number and assign to id
      System.out.println("Press return to get jokes/proverbs, type 's' to toggle server, type 'quit' to exit program.");
			do{
				System.out.flush();//write out data in the buffer
				input = in.readLine();//read standard in as String and assign to input
        if (input.indexOf("quit") < 0) {
          	if (input.equals("s")) {//if standard in is 's'
            	if (switchserver) {//if switchserver is true
              		switchserver = false;//turn switchserver to false
              		JokeClient.port = 4546;//set port to 4546
              		System.out.println("<S2>Now communicating with " + secondaryName +", port: " + port + ".");
              		connectToServer(name, secondaryName, id, 4546);//call method to connect to server
            	}else{//if switchserver is false
              	switchserver = true;//turn to true
              	JokeClient.port = 4545;//set port to 4545
              	System.out.println("Now communicating with " + primaryName +", port: " + port + ".");
              	connectToServer(name, primaryName, id, 4545);
            	}
            }else{//if standard in is string other than 's'
              switchingMode = true;//set switchingMode to true
            	connectToServer(name, primaryName, id, JokeClient.port);//connect to serevr
          	}
        }
      }while (input.indexOf("quit") < 0);//exit program if 'quit' is typed in
      System.out.println("User terminated program.");
    }catch(IOException x) {// catch the possilbe exception
      x.printStackTrace();//telling the reason of the error, easier for programmers to debug
    }
  }
  /*a moethod to connect to server with parameters of two Strings and two ints*/
  static void connectToServer(String name, String primaryName, int id, int port) {
  	Socket sock; //a Socket variable decaration, create a socket to communicate with server
  	BufferedReader fromServer;//a BufferedReader variable declaration, so we can read stream into this variable
  	PrintStream toServer; //a PrintStream variable declaration，we can send data to server through this  
  	String textFromServer; //a String variable declaration，we can store the data from server to this variable
    try {//using the 'try-catch' method to catch the possible exceptions
      //create a new socket, using primaryName and port as arguments and assign to sock variable
      sock = new Socket(primaryName, port);  
      // read stream from server
      fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
      toServer = new PrintStream(sock.getOutputStream()); //send data to server
      //writing name
      toServer.println(name);//call method println to send name to Server
  		toServer.println(id);//call method println to send id to Server
      toServer.flush();//refresh output stream so as to make sure to send the stream to Server
      // Reading stream less and equal to 3 lines from server
      for (int i = 1; i <= 3; i++) {
    	  textFromServer = fromServer.readLine();//read stream as string from server and store them in textFromServer
        if (textFromServer != null) {
          System.out.println(textFromServer);//print textFromServer if not null
        }
      }
      //close socket everytime we finish it
      sock.close();

    }catch(IOException x){//catch the possible exception
      System.out.println("Error: Socket error.");//print error
      x.printStackTrace();//telling the reason of the error, easier for programmers to debug
    }
	}
}
