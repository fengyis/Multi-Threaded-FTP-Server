import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class TCPEchoClient {
	
   static String exceptionfilepath="";
   
  public static void main(String[] args) throws IOException {
	
//	create default file directory
	File file = new File("pub");
	File music = new File("pub/music");
	File photo = new File("pub/photo");
	
	
	if (!file.exists()) 
		file.mkdir();
	if (!music.exists())
		music.mkdir();
	if (!photo.exists())
		photo.mkdir();
		
	 
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	Boolean exit = false;
	String serverName = "";
	String message = "";
	int portNumber = 0;

    System.out.println("Enter Server Name : ");
    serverName = br.readLine(); 
    
    System.out.println("Enter Server Port Number : ");
    portNumber = Integer.parseInt(br.readLine());
    
    
//   check correct input for port number and host name
    if ((serverName.isEmpty()) || portNumber ==0 )
    	throw new IllegalArgumentException("Please enter Server name and Port Number");
    
    
    String server = serverName;       // Server name or IP address
    // Convert argument String to bytes using the default character encoding
    int servPort = portNumber;

    // Create socket that is connected to server on specified port
    Socket socket = new Socket(server, servPort);
    System.out.println("Connected to server");
    
    try{
	    while(!exit){
	    	System.out.println("Enter command to send : ");
	        
	        String[] splited = br.readLine().split("\\s+");
	        message = splited[0];
		
		    InputStream in = socket.getInputStream();
		    OutputStream out = socket.getOutputStream();
		    
		    DataOutputStream dos= new DataOutputStream(out);
		    DataInputStream  dis= new DataInputStream(in);
		    
		    Send(dos,message);
		    String command = ReceiveString(dis);
		    
		    if (command.equals((message)))
		    		System.out.println("Server successfuly received: " + command);
			
		    if (message.contains("read")){
		    	//read file path
		    	String filePath =splited[1];
		    	exceptionfilepath = filePath;
		        Send(dos,filePath);
		        //waiting server to confirm if the file exists, if yes,receive the file
		        //if no, exit.
		        if(ReceiveString(dis).contains("yes")){
		        	
		        	System.out.println("Server transmitting requested file");
		        	
			        byte[] recvdFileinBytes = ReceiveByte(dis);
			        
			        FileOutputStream fos = new FileOutputStream(filePath);
			        fos.write(recvdFileinBytes);
			        System.out.println("Stored the file at the directory" + filePath);
		        }else{
		        	System.out.println("The file requested does not exist on server");
		        }
		
		    }else if (message.contains("write")){
	//	    	read file path
		    	String filePath =splited[1];
		    	Send(dos,filePath);
	//	    	check to see if the file to write exists or is a directory
		    	File f = new File(filePath);
		    	if(f.exists() && !f.isDirectory()) {
		    		System.out.println("The file to write exists on client");
		    		Path path = Paths.get(filePath);
	//	    		if yes, send the file
		    		byte[] writedata = Files.readAllBytes(path);
		    		Send(dos,writedata);
		    		
		    	}else{
		    		System.out.println("The file to write does not exist");
		    	}
	//	    	receive ACK
		    	String ack = ReceiveString(dis);
		    	System.out.println(ack);
		  	  
		  	  
		    }else if (message.contains("list-all")){
	//	    	list all command, receive all file info
		        String directory = splited[1];
		        Send(dos,directory);
		        String fileNameandSize = ReceiveString(dis);
	//	        System.out.println(fileNameandSize);
		    }else if (message.contains("bye")){
	//	    	bye operation,close client socket
		    	ReceiveString(dis);
		    	exit = !exit;
		    	System.out.println("Closing socket,will exit the connection with server");
		    	socket.close(); 
		    }else if (message.contains("quit")){
	//	    	client sends quit command to server, notify the server to quit
	//	    	wait for server to input the quit in server side command window
		    	System.out.println("Waiting for server to enter quit command");
		    	String decision = ReceiveString(dis);
		    	
		    	if(decision.contains("Quit command Entered,Server will be shut down")){
	//	    		if server entered quit, close the client's socket and end this client connection
		    		System.out.println("Server shut down confirmed.Bye!");
		    		exit = !exit;
			    	socket.close(); 
		    	}else if(decision.contains("There are existing clients, can not quit")) {
		    		System.out.println("There are existing clients, can not quit");
		    	}else{
		    		System.out.println("Server side did not enter quit.Socket connections will remain");
		    	}
	//	    	if no, continue
	    	
	    }
	    
    	}}catch(java.io.EOFException e){
    	  	File exceptionfile = new File(exceptionfilepath);
    		if(exceptionfile.delete()){
    			System.out.println("not completly transfered file :"+exceptionfile.getName() + " is deleted!");
    		}else{
    			System.out.println("not completly transfered file delete operation is failed.");
    		}
    	}
    
  }
  
//   check the if the object returned by server if the same object sent by client
	  public static int CheckBytes(InputStream in,byte[] data) throws SocketException, IOException{
		  int totalBytesRcvd = 0;  // Total bytes received so far
		    int bytesRcvd;           // Bytes received in last read
		    while (totalBytesRcvd < data.length) {
		      if ((bytesRcvd = in.read(data, totalBytesRcvd,  
		                        data.length - totalBytesRcvd)) == -1)
		      throw new SocketException("Connection closed prematurely");
		      totalBytesRcvd += bytesRcvd;
		    }  // data array is full
		  
		 return totalBytesRcvd;
	  }
	  
//	  send string
	  public static void Send(DataOutputStream dos , String string) throws IOException{
		  byte[] stringInBytes = string.getBytes("UTF-8");  
        dos.writeInt(stringInBytes.length);
        dos.write(stringInBytes); 
        dos.flush();   
	  }
//	  send bytes
	  public static void Send(DataOutputStream dos , byte[] data) throws IOException{
	        dos.writeInt(data.length);
	        dos.write(data); 
	        dos.flush();   
	  }
//	  receive string
	  public static String ReceiveString(DataInputStream dis) throws IOException{
		  int size = dis.readInt();  
	      byte[] stringInBytes = new byte[size];  
	      
	      dis.readFully(stringInBytes);  
	      String string = new String(stringInBytes, "UTF-8"); 
	      System.out.println("Client received : " + string);
	      
	      return string;
	  }
//	  receive byte
	  public static byte[] ReceiveByte(DataInputStream dis) throws IOException{
		  int size = dis.readInt();  
	      byte[] RecvdBytes = new byte[size];  
	      dis.readFully(RecvdBytes);  
	      return RecvdBytes;
	  }
}
