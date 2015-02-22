import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Scanner;

import javax.swing.JTextArea;

//Boolean quitFlag= false;

public class MultiThreadServer {
	
	public static void main(String[] args) throws IOException { 
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int PortNumber = 0;  

    System.out.println("Enter Server Port Number : \n");
    PortNumber = Integer.parseInt(br.readLine());
    
    if (PortNumber == 0)  // Check the input of port number
    throw new IllegalArgumentException("PortNumber: "+PortNumber+"is not valid");

  

//    create default directory
	File pub = new File("pub");
	File music = new File("pub/music");
	File photo = new File("pub/photo");
	
	if (!pub.exists()) 
		pub.mkdir();
	if (!music.exists())
		music.mkdir();
	if (!photo.exists())
		photo.mkdir();

// 	  Create a server socket to accept client connection requests

    int clientNumber =1;
    ServerSocket servSock = new ServerSocket(PortNumber);
    System.out.println("Server Socket Established at port number: "+PortNumber);
    
    try {
        
    while(true){
//    		accepting socket connections
			Socket clntSock = servSock.accept();  
	        
		    SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
	        InetAddress inetAddress = clntSock.getInetAddress();
		    System.out.println("Handling client at " + clientAddress);
		    
	        System.out.println("Client " + clientNumber + "'s host name is "
	                + inetAddress.getHostName() + "\n");
	        System.out.println("Client " + clientNumber + "'s IP Address is "
	                + inetAddress.getHostAddress() + "\n");
//	       print the host name and ip address of connecting client
	        
		    HandleTCPClient handler = new HandleTCPClient(clntSock,servSock);
		    new Thread(handler).start();
//		    start a new server process for each connecting client
		    clientNumber ++;
    	}
    }

    catch (SocketException e) {
//    	catch socket connection expections
    	System.out.println("Forcing all sockets connection to close");
    }

	}
	
	


}
