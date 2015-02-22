import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.io.*;   // for IOException and Input/OutputStream

public class HandleTCPClient implements Runnable{
	
	  Socket clntSock;
	  ServerSocket srvSocket;
	  Boolean exit=false;
	  Scanner scaninput = new Scanner(System.in);

	  
	  public HandleTCPClient(Socket socket, ServerSocket srvSocket) throws IOException {
//		  handle the connecting client socket
	      this.clntSock = socket;
	      this.srvSocket = srvSocket;
	      SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
	      System.out.println("Handling client at " + clientAddress);
	  }

	  public void run() {
		  try{
			  InputStream in = clntSock.getInputStream();
		      OutputStream out = clntSock.getOutputStream();
			  DataOutputStream dos= new DataOutputStream(out);
		      DataInputStream  dis= new DataInputStream(in);
		
	      
	      while (!exit) 
	      { // Run forever, servicing connections
	     
		      String command = ReceiveString(dis);
		      Send(dos,command);
		      

		      if (command.contains("read")){
//		    	  read operation
		    	  String filePath = ReceiveString(dis);
		    	  System.out.println("Looking for file at : "+ filePath);
		    	  
		    	  File f = new File(filePath);
//		    	  check if the requesting path exists and check if it's a directory
//		    	  if yes, find and send the file
		    	  if(f.exists() && !f.isDirectory()) {
		    		Send(dos,"yes");
		    		Path path = Paths.get(filePath);
		    		byte[] data = Files.readAllBytes(path);
		    		Send(dos,data);
		    		
		    	  }else{
		    		Send(dos,"no");
		    	  }
		      //write operation
		      }else if (command.contains("write")){
		    	  //receive file path
		    	  String filePath = ReceiveString(dis);
		    	  System.out.println("Storing to file at : "+filePath);
		    	  GlobalFlag.exceptionfilepath = filePath;
		    	  //receive and storing file to the corresponding path
		    	  byte[] recvdFileinBytes = ReceiveByte(dis);
			      FileOutputStream fos = new FileOutputStream(filePath);
			      fos.write(recvdFileinBytes);
			      System.out.println("Stored the file at the directory " + filePath);
			      //send ACK 
			      Send(dos,"File successfuly stored at "+filePath+" on server");
		    	  
		      }else if (command.contains("list-all")){
		    	  
		    	  String directory = ReceiveString(dis);
		    	  System.out.println("Client request to list all file under "+directory+" directory");
		    	  
		    	  String fileNameandSize="";
		    	  File[] files = new File(directory).listFiles();
		
		    	  for (File file : files) {
		    	      if (file.isFile()) {
		    	          fileNameandSize = file.getName()+" "+"size= "+file.length()+"bytes \n" +fileNameandSize;
		    	      }
		    	  }
//		    	  if there is no files in the requesting directory, notify client
		    	  if (fileNameandSize.isEmpty()){
		    		  Send(dos,"This directory does not contain any file");
		    	  }else{
//		    	  if else, send the file
		    		  Send(dos, fileNameandSize);
		    	  }
		      }else if (command.contains("bye")){
		    	  	  Send(dos, "Server thread for client will be closed after client closes the socket conneciton");
		    	  	  exit = true;
		    	  	  GlobalFlag.clientNumber--;
		    	  	  System.out.println("Client socket connection is closed.Remaining client number is "+GlobalFlag.clientNumber);
		    	  	 
		    	  	  clntSock.close();  // Close the socket.  We are done with this client!
		      }else if (command.contains("quit")){
		    	 if(GlobalFlag.clientNumber ==1){
		    	  System.out.println("Please enter quit command in server console");
		    	  String serverCommand = scaninput.nextLine();
//		   		 enter quit command on server side console
		    	  if(serverCommand.contains("quit")){
		    		  Send(dos, "Quit command Entered,Server will be shut down");
		    		  exit = true;
//		    		  close server and client's socket
		    		  srvSocket.close();
		    		  clntSock.close();
		    		  System.out.println("Closing server, Server will be shut down");
		    	  }else{
//		    		  if no quit command entered, continue
		    		  Send(dos, "Server side did not enter quit.Socket connection will remain");
		    		  System.out.println("Server side did not enter quit.Socket connections will remain");
		    	  	}
		    	   }else{
		    		   Send(dos, "There are existing clients, can not quit");
		    		   System.out.println("There are existing clients, can not quit");
		    	   }	
		    }		
	      }
	      	  
		      }catch(java.io.EOFException e){
		    	  	File file = new File(GlobalFlag.exceptionfilepath);
		    		if(file.delete()){
		    			System.out.println("not completly transfered file :"+file.getName() + " is deleted!");
		    		}else{
		    			System.out.println("not completly transfered file delete operation is failed.");
		    		}
		      }catch (IOException e) {
			  			e.printStackTrace();
			   }
	      
	  	}
  
//  		receive string
		  public static String ReceiveString(DataInputStream dis) throws IOException{
			  int size = dis.readInt();  
		      byte[] stringInBytes = new byte[size];  
		      
		      dis.readFully(stringInBytes);  
		      String string = new String(stringInBytes, "UTF-8"); 
		      System.out.println("Server received : " + string);
		      
		      return string;
		  }
//		  send string 
		  public static void Send(DataOutputStream dos , String string) throws IOException{
			  byte[] stringInBytes = string.getBytes("UTF-8");  
	        dos.writeInt(stringInBytes.length);
	        dos.write(stringInBytes); 
	        dos.flush();   
		  }
//		  send bytes
		  public static void Send(DataOutputStream dos , byte[] data) throws IOException{
	        dos.writeInt(data.length);
	        dos.write(data); 
	        dos.flush();   
		  }
//		  receive bytes
		  public static byte[] ReceiveByte(DataInputStream dis) throws IOException{
			  int size = dis.readInt();  
		      byte[] RecvdBytes = new byte[size];  
		      dis.readFully(RecvdBytes);  
		      return RecvdBytes;
		  }
}
