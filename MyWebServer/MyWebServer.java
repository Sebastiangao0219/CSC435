/*--------------------------------------------------------

1. Name / Date:Liang Gao / Apr 30, 2020 

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_241-b07

3. Precise command-line compilation examples / instructions:

> javac MyWebServer.java
> javac MyTelnetClient.java


4. Precise examples / instructions to run this program:

e.g.:you may use the MyTelnetClient or MyListener to test this server codes

In separate shell windows:

> java MyWebServer
> java MyTelnetClient



5. List of files needed for running the program.

e.g.:

 a. checklist-mywebserver.html
 b. MyWebServer.java

6. Notes:For the "Return html files with MIME from MyWebServer" Task,
   I don't know if it's problem on my Mac or anything else, it cannot
   print out the expected result, but it runs totally fine in firefox.
   Besides, for task "Fully, recursively, navigates directories and files",
   I can recursively click in from the outside page to inner page, but I cannot
   recursively click form inner page to outter page, I'm not sure if my codes
   meet this task ot not.
   Overall, I'll give these two taks a Maybe. 

----------------------------------------------------------*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/*This class is for communication between a web server and client(really chanllenging btw)*/
public class MyWebServer {//this is a class declaration who contains the main method
	public static void main(String[] args) throws IOException {//main method where the program started
		int q_len = 6;// six requests at most to queue in the OS
		int port = 2540;//port for connection, it's like a password for server and client
		Socket sock;// Declaring a socket to receive connect request from client
		ServerSocket servsock = new ServerSocket(port, q_len);//Creating a ServerSocket to listen at port, waiting for the connection form client
		System.out.println("Sebastian Gao's Web Server listening at port:" + port + ".");//noticing user which server is being used now
		while(true){// a while loop waiting for the connection request from time to time
			sock = servsock.accept(); // Waiting for the connection from client
			new Worker(sock).start();//starts the new thread to receive from client
		}
	}
}

class Worker extends Thread{
	Socket sock;//a Socket variable decaration, receive connection request from client
	Worker (Socket s) {sock = s;}//constructor of class Worker who encapsulates with a Socket s and assign s to sock
	
	public void run() {
		try{PrintStream out = null;/*Declare a PrintStream variable for stream sendout*/
			BufferedReader in = null; /*Declare a BufferedReader variable for the sake of read in stream from client*/
			/*get input stream from socket after connected and create buffer to read input stream*/
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			/*instantiate PrintStream and take scoket output stream as a parameter */
			out = new PrintStream(sock.getOutputStream());
			String browserRequest = in.readLine();
                if(browserRequest != null){
                	String requestedFileName = null;
                	System.out.println(browserRequest);
					if(browserRequest.contains("GET")){
						requestedFileName = browserRequest.substring(4, browserRequest.length() - 9); //Using method substring to retrieve the name of file and assign it to a String type value requestedFileName
					}
					if(requestedFileName != null && requestedFileName.contains("..") == true) {//this is a basic security checking, if the input file name contains ".." then I could believe that this guy is ubreliable
                    	System.err.println("403 " + " FORBIDDEN " + "You have no the permission to access the requested URL.");//print error to the screen to notice the intruder
                	}
            		String contentType;//a type of content declaration  
            		if (requestedFileName != null && !requestedFileName.equals("/favicon.ico")){//it is needed to check if the file is a 'favicon.ico' file, if so, we ignore it

                		if (requestedFileName.contains(".html") || requestedFileName.contains(".htm")) {//type definition, if the file is end with '.html' or '.htm', it's type is "text/html"
                    		contentType = "text/html";
                    		processFileRequest(requestedFileName, out, contentType);//this type of file can be use in method processFileRequest which is to deal with file request
                		}else if (requestedFileName.contains(".txt") || requestedFileName.contains(".java")) {//type definition, if the file is end with '.txt' or '.java', it's type is "text/plain"
                    		contentType = "text/plain";
                    		processFileRequest(requestedFileName, out, contentType);//this type of file can be use in method processFileRequest which is to deal with file request
                		}else if (requestedFileName.contains("/cgi/addnums.fake-cgi")) {//type definition, if the file contains string "/cgi/addnums.fake-cgi", it is type of "text/html"
                    		contentType = "text/html";
                    		addNums(requestedFileName, out, contentType);//this type of file can be use in method addNums which is to deal with step 4 of this assignment
                		}else if (requestedFileName.contains("/")) {//type definition, if the file is end with '/', it's type is "text/html"
                    		contentType = "text/html";
                    		processDirectoryRequest(requestedFileName, out, contentType);//this type of file can be use in method processDirectoryRequest which is to deal with directory request
                		}else{//assumption for other files are all equal to "text/plain"
                    		contentType = "text/plain";
                		}

            		}
            	
				sock.close();//socket close
			} 
		}catch (IOException e) {//catch the Exception once it occurred
			//System.err.println(e);
			//e.printStackTrace();//telling the reason of the error, easier for programmers to debug
			System.out.println("Disconnected and listening now....");//once an error has been generated, notice the user connetion is done and listening again now
		}
	}
	/* 
	 * Here is method declaration, this method is to deal with file request, if user looking for file through a browser, we use this method to handle it
     * it has three built-in value with of two Strings and PrintStream respectively.
     */
	public void processFileRequest(String fileName, PrintStream toBrowser, String contentType) throws IOException{
		int numberOfBytes = -1111;//a value of int type declaration, it's for couunt the number from input stream
		String crlf = "\r\n";//this is just a simple encapsulation, to assign space and newline to String crlf
		if(fileName.startsWith("/")) {//here is a corner case that I am think of, if the file name starts with a slash '/'
			fileName = fileName.substring(1);//then I use method substring to remove it
		}
		/*Declare an object of File and find a file using class File*/
		File file = new File(fileName);
		/*Declare an object of InputStream and initialize it through object polymorphism*/
		InputStream inputStream = new FileInputStream(fileName);//declare an InputStream object inputStream and initialize it with fileName
		toBrowser.print("HTTP/1.1 200 OK" + crlf +"Content-Length: " + file.length() + crlf +
				"Content-Type: " + contentType + crlf + crlf);//print basic informations in MIME type on the screen 
		System.out.println("Sebastian Gao's Web server is dealing with file: " + fileName);
		/*Declare a byte array so I can read everything into this array*/
		byte[] fileBytes = new byte[(int)file.length()];//I use the 'file.length()' to determine the size of array for saving more space
		numberOfBytes = inputStream.read(fileBytes);//using the method read from InputStream to retrieve the number of bytes
		toBrowser.write(fileBytes, 0, numberOfBytes);//using the method write from PrintStream to write byte into fileBytes with length of 0-numberOfBytes
		toBrowser.flush();//clear the buffer
		inputStream.close();//close the stream
	}
	
	/* 
	 * Here is method declaration, this method is to deal with directory request, if user looking for dirctory through a browser, we use this method to handle it
     * it has three built-in value with of two Strings and PrintStream respectively.
     */
	public void processDirectoryRequest(String directoryName, PrintStream toBrowser, String contentType) throws IOException{
		String crlf = "\r\n";//this is just a simple encapsulation, to assign space and newline to String crlf
		toBrowser.print("HTTP/1.1 200 OK"); //another way to print basic  MIME type informations on the screen                                                                                                  
        toBrowser.print("Content-Length: " + directoryName.length());  //another way to print basic MIME type informations on the screen                                                                                           
        toBrowser.print("Content-type: " + contentType + crlf + crlf);  //another way to print basic MIME type informations on the screen   
		File file01 = new File("./" + directoryName + "/");//declare a file and initialiaze with request from browser
		File[] filesInDirectory = file01.listFiles();//I used 'listFiles' method to list the name of files in the directory and store them in an array of File filesInDirectory
		/* 
		 * For the following codes, I learnt what a html format should be like from online tutorial
		 * and I use the method print from PrintStream to construct a .html format
		 */
		toBrowser.print("<html><head>" + crlf);//here is the beginning of sytax of html language,open html and head
		toBrowser.print("<title>" + directoryName + "</title>" + crlf);//here is the title of this .html file which will not shown on the page
		toBrowser.print("</head><body>" + crlf);//close head and open body, the content after body is shown on the page
		toBrowser.print("<h1><a href=" + "\"http://localhost:2540\"" + ">" + "Home Page" + "</a ></h1>");//this is hyper-link to link  "http://localhost:2540" to "Home page" so we can click Home Page to go back to root page
		if(filesInDirectory!=null) {//if there have files in the array, we do the following for loop
            for (int i = 0; i < filesInDirectory.length; i++) {//a simple for loop
                if (filesInDirectory[i].isDirectory()) {//if file is dirctory, we create a hyper-link for it and use it's own name as a clickable link
                    toBrowser.print("<a href=\"" + filesInDirectory[i].getName()  + "/\">/" + filesInDirectory[i].getName() + "/</a ><br>");//a hyper-link construction,<br> means newline
                } else if (filesInDirectory[i].isFile()) {//if file is file, we create a hyper-link for it and use it's own name as a clickable link
                    toBrowser.print("<a href=\"" + filesInDirectory[i].getName() + "/\">/" + filesInDirectory[i].getName() + "</a ><br>");//a hyper-link construction,<br> means newline
                }
            }
        }
        toBrowser.print("</body></html>");//here is the sytax to finish the .html file
	}
	
	/* 
	 * Here is method declaration, this method is to deal with a page with person and 2 numbers, if user play this page in a browser, we use this method to handle it
     * it has three built-in value with of two Strings and PrintStream respectively.
     */
	public void addNums(String request, PrintStream toBrowser, String contentType) throws IOException{
		String crlf = "\r\n";//this is just a simple encapsulation, to assign space and newline to String crlf
		Map<String, String> map = new HashMap<String, String>();//create a hashmap to store the datas that we gonna use
		URL url = new URL("http:/" + request);//declare a URL variable and initialize it browser input
		String query = url.getQuery();//return the section the url looking for and assign it to String query
		String[] pairs = query.split("&");//spliting the query with '&' and put them in an array of String
		int indexOfEqualSign = -1111;//int variable declaration for mark the index of equal sign
		for (int i = 0; i < pairs.length; i++) {//Using a for loop to put person, number1 and number2 in the hash map
			indexOfEqualSign = pairs[i].indexOf("=");//Using the method 'indexOf' to find the index of '=' and assign the index to a String object 'indexOfEqualSign'
			/*Using method 'URLDecoder.decode' to transfer the MIME string to regular string with 'UTF-8' character set*/
			String key = URLDecoder.decode(pairs[i].substring(0, indexOfEqualSign), "UTF-8");
			/*Using method 'URLDecoder.decode' to transfer the MIME string to regular string with 'UTF-8' character set*/
			String value = URLDecoder.decode(pairs[i].substring(indexOfEqualSign + 1), "UTF-8");
			map.put(key, value);//Put the key and value into the hash map
		}
		String person = map.get("person");//get the value of key : "person" and assign it to String person
    	int n1 = -1111;//int variable declararion 
    	int n2 = -1111; //int variable declararion 
    	int result = -1111;//int variable declararion 
    	String num1 = map.get("num1");//retrieve the valure of key : num1 and assign it to String num1
		String num2 = map.get("num2");//retrieve the valure of key : num2 and assign it to String num2
		String replyToBrowser = "Dear " + person +", the sum of " + num1 + " and " + num2 + " is " + result;//add all the strings to calculate length
		int stringLen = replyToBrowser.length();//Using method .length() to get the number of length of string replyToBrowser
    	try {
			//String num1 = "";
			//String num2 = "";
			n1 = Integer.parseInt(num1);//parse string num1 to number num1 and assign it to int n1
    		n2 = Integer.parseInt(num2);//parse string num2 to number num2 and assign it to int n2
    		result = n1 + n2;
		} catch (NumberFormatException ex) {//exception catch
			System.err.println(ex);//print error
		}
    	System.out.println("CGI addnums- person: " + person + "  num1: " + n1 + "  num2: " + n2 + "  result: " + result);//print current situation

    	toBrowser.print("HTTP/1.1 200 OK");//to construct MIME type information
        toBrowser.print("Content-Length: " + stringLen);//to construct MIME type information
        toBrowser.print("Content-type: " + contentType + crlf + crlf);//to construct MIME type information
    	toBrowser.print("<html><head>" + crlf);//here is the beginning of sytax of html language,open html and head
    	toBrowser.print("<title>" + "CGI Addnum" + "</title>" + crlf);//here is the title of this .html file which will not shown on the page
    	toBrowser.print("</head><body>" + crlf);//close head and open body, the content after body is shown on the page
    	toBrowser.print("<h1>" + "Addnums" + "</h1>" + crlf);//this is the head of this page
    	toBrowser.print("<p>" + "Dear " + person + ", the sum of " + n1 + " and " + n2 + " is " + result + "</p>" + crlf);//print out the result
    	toBrowser.print("</body></html>" + crlf);//to finish html file
		toBrowser.flush();//clear buffer
	}
	
}






























































































































































