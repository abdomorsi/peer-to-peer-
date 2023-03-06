package Peer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;


// pair class implementation
class Pair {
    public String first;
    public String second;
    public Pair(String _first,String _second)
    {
    	first = _first;
    	second = _second;
    }
}


class handlingUser extends Thread   {
	Thread t;
	Socket connectSocket;
	DataOutputStream dos;
	DataInputStream dis;
	handlingUser(Socket s,DataOutputStream d,DataInputStream dd) {
		connectSocket = s;
		dos = d;
		dis = dd;
	}

	public void run() {
		try
		{
			// Receiving host name , for real connection
			String peerHostName = dis.readUTF();
			
			// Receiving user name , for showing in the console
			// But MUST BE UNIQUE
			boolean isUnique = false;
			String userName = new String();
			while(isUnique == false)
			{
			 userName = dis.readUTF();
			// Checks in map if this user name exists
			if(Center.HostOfUser.containsKey(peerHostName)) // if yes, send a negative ACK
				dos.writeBoolean(false);
			else // else break from the loop
			{
				isUnique = true;
				dos.writeBoolean(true);
			}
				
			}
			
			Center.HostOfUser.put(userName,peerHostName); // mapping the user name with its host name
			Pair peer = new Pair(userName,peerHostName); 
	
			// Receiving files names
			Vector <String> userFiles =  new Vector<String>(); // we put files names in this vector 
			while(true)
			{
				
				String fileName = dis.readUTF(); // receiving the file name of wanted file to download
				if(fileName.charAt(0) == '*') {break;} // we stop when receiving '*'
				userFiles.add(fileName); // adding the file name to the vector	
				// checking if the file exists
				boolean flag = Center.filesOwners.containsKey(fileName);
				if(flag == true ) // if file exists we just add the peer to its vector
				{
					(Center.filesOwners.get(fileName)).add(peer);
				}
				else // if not , we insert the file with a new vector which has the peer
				{
					Vector<Pair>v = new Vector<Pair>();
					v.add(peer);
					Center.filesOwners.put(fileName,v);
				}
			}
			// avil list
			Center.userFiles.put(peer,userFiles); // now we insert the peer with its vector of files
			
			while(true)
			{
			// Reading an operation then processing
		     String input = dis.readUTF();
				if(input.compareToIgnoreCase("Exit") == 0)
				{
					// We loop over its vector of files from userFiles map
					// Each file we remove the peer from the vector of the file in fileOwners map
					for(String file : Center.userFiles.get(peer))
					{

						Center.filesOwners.get(file).remove(peer);
						// if the vector became empty, remove the file from the fileOwners map
						if(Center.filesOwners.get(file).isEmpty()) 
							Center.filesOwners.remove(file);
					}
					Center.userFiles.remove(peer);// remove the user from userFiles
				}
			
				else if(input.compareToIgnoreCase("download") == 0 || (input.compareToIgnoreCase("Show File Content") == 0))
				{
					String fileName = dis.readUTF(); // Reading the requested file
					boolean flag = Center.filesOwners.containsKey(fileName); // checks if exists
					if(flag == false)
					{
						dos.writeUTF("no"); // send negative ack
					}
					else
					{
						dos.writeUTF("yes");
						// if the file exists we send the number of uploader	
						// we get the number of uploaders from the vector size
						int num_of_uploaders = Center.filesOwners.get(fileName).size();
						dos.writeInt(num_of_uploaders);
						
						// Then we loop over the vector and send each uploader name
						int i = 1;

						for (Pair peer1 : Center.filesOwners.get(fileName)) {
				            dos.writeUTF(i + " -> " + peer1.first);
				        }
						int choice = dis.readInt(); // Reading the choice for uploader
						
						// "Center.filesOwners.get(fileName) " returns the vector of uploader
						// " get(choice - 1) " returns the requested peer
						// ".second" returns the host name of the peer
						String uploaderHostName = Center.filesOwners.get(fileName).get(choice - 1).second;
						// Now we send the host name of the requested peer 
						// to the peer wanting the file
						// Then there, a peer to peer connection will be Esablished
						dos.writeUTF(uploaderHostName); 			
					}

				}
				else if(input.compareToIgnoreCase("Send Message") == 0)
                {
					
                    String targetUser = dis.readUTF(); // Reading the target user name
                    
                    //Checking if the user name exists
                    boolean CheckName = Center.HostOfUser.containsKey(targetUser);
                    if ( CheckName == false )
                    {
                        dos.writeUTF("no");
                    }
                    else
                    {
                        dos.writeUTF("yes");
                        // Sending the hostName to the peer 
                        // to Establish a peer to peer connection between them
                        String targetHostName = Center.HostOfUser.get(targetUser);
                        dos.writeUTF(targetHostName); //sending the hostNAme to the connected peer
                    }
                }
				else if( input.compareToIgnoreCase("List all files") == 0 )
				{
					// First we send the number of files
					// We get the number of files from the Size of fileOwners Map
					// So, the peer will know the number of iterations of taking input
					dos.writeInt(Center.filesOwners.size());
					
					// Now We send file name by file name
				    for (String fileName : Center.filesOwners.keySet()) {
				        dos.writeUTF(fileName);
				    }
				}
			}

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}

	}

}

public class Center {
	// storing files and their owners , used when uploading and download a file
    public static  HashMap<String, Vector<Pair> > filesOwners = new HashMap<String, Vector<Pair> >();
    // storing peers and their files used when a peer disconnects
    public static HashMap<Pair, Vector<String>> userFiles = new HashMap<Pair, Vector<String>>();
    // maps the user name with its host name, used while sending a message
    public static  HashMap<String,String> HostOfUser = new HashMap<String, String>() ;
    
	public static void main(String[] args) {
		try
		{
			
		ServerSocket welcomingSocket = new ServerSocket(50013);
		System.out.println(" >> Server side <<");
		while(true)
		{
			Socket connectSocket = welcomingSocket.accept();
			DataInputStream dis = new DataInputStream(connectSocket.getInputStream());
			DataOutputStream dos = new DataOutputStream(connectSocket.getOutputStream());

			
			Thread t = new handlingUser(connectSocket,dos,dis);
			t.start();
		
		}
		}
		catch(Exception e)
		{
			System.out.println("center problem");
		}

	}

}
