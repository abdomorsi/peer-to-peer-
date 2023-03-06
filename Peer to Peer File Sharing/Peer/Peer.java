package Peer;
/**
 * 
 */

/**
 * @author Best Team
 *
 */
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.*;
class UserThread extends Thread {
	Thread t;
	Socket connectSocket;

	UserThread(Socket s) {
		connectSocket = s;
	}

	public void run() {
		Scanner reader = new Scanner(System.in);
		try {
			DataOutputStream dos = new DataOutputStream(connectSocket.getOutputStream());
			DataInputStream dis = new DataInputStream(connectSocket.getInputStream());
			while (true) {
                // list of choices
				System.out.println("Choose >> DownLoad" );
				System.out.println("       >> Send Message" );
				System.out.println("       >> Show Messages" );
				System.out.println("       >> Show File Content" );
				System.out.println("       >> list All files" );
				System.out.println("       >> Exit  " );

				// choosing which operation want to do
				String choice = reader.nextLine(); //read the choice from the user
				///            download operation
				if (choice.compareToIgnoreCase("download") == 0) {
					dos.writeUTF(choice);//sending the choice to the center
					// Take the requested file from the user
					System.out.println("Enter file name :");
					String fileName = reader.nextLine();

					dos.writeUTF(fileName); // Send the file name to the Center

					// now the center screening if the file exists

					System.out.println(fileName);

					// Take an ack from the Center to check whether the file exists
					String Acknowledgment = dis.readUTF();
					if (Acknowledgment.compareToIgnoreCase("no") == 0) {
						System.out.println("Sorry this file is not available");
						continue;
					}
					// the file exists !
					// Take from Center num of uploaders then print
					int num_of_uploaders = dis.readInt();
					System.out.println("Number of Uploaders = " + num_of_uploaders);
					// Taking uploaders names from the Center
					System.out.println("Choose the uploader : ");
					for(int i = 0;i < num_of_uploaders;i++)
					{// showing the list of uploaders
						String line = dis.readUTF();
						System.out.println(line);
					}
					int choice2 = reader.nextInt(); // Taking the required uploader
					dos.writeInt(choice2); // Send the choice to the Center
					
					// Receiving the host name to establish a socket
					String peerAdress = dis.readUTF(); 
					
					// Establishing P2P Connection
					Socket connectToPeer = new Socket(peerAdress, 50010);
					DataOutputStream dos2 = new DataOutputStream(connectToPeer.getOutputStream());
					DataInputStream dis2 = new DataInputStream(connectToPeer.getInputStream());
					
					// where are you receiving the current downloads ?
					System.out.println("Enter the directory : ");
					String directory = reader.nextLine(); //To ignore \n char
					directory = reader.nextLine(); // take the name of directory with its location
					System.out.println(directory);
					File file = new File(directory +"\\"+ fileName); //put the file in the directory
					// load data
					byte[] bytes = new byte[16 * 1024]; //buffer to store data file
					FileOutputStream out = new FileOutputStream(file); //open the file to load its data
					dos2.writeUTF("download");// sending the operation that is required to the peer
					dos2.writeUTF(fileName);
					int count;

					while ((count = dis2.read(bytes)) > 0) {
						//get the file data
						out.write(bytes, 0, count);
					}
					dis2.close();
					dos2.close();
					out.close();
					System.out.println("Done");
				}
				else if(choice.compareToIgnoreCase("show file content") == 0)
				{
					dos.writeUTF(choice);//sending the choice to the center
					// Take the requested file from the user
					System.out.println("Enter file name");
					String fileName = reader.nextLine();

					dos.writeUTF(fileName); // Send the file name to the Center
					
					// Take an ack from the Center to check whether the file exists
					
					String Acknowledgment = dis.readUTF();
					if (Acknowledgment.compareToIgnoreCase("no") == 0) {
						System.out.println("Sorry this file is not available");
						continue;
					}
					// the file is existing !
					// Take from Center num of uploaders then print
					int num_of_uploaders = dis.readInt();
					System.out.println("Number of Uploaders = " + num_of_uploaders);
					// Taking uploaders names from the Center
					System.out.println("Choose the uploader : ");
					for(int i = 0;i < num_of_uploaders;i++)
					{//show the list of uploaders
						String line = dis.readUTF();
						System.out.println(line);
					}
					int choice2 = reader.nextInt(); // Taking the required uploader
					dos.writeInt(choice2); // Send the choice to the Center
					
					// Receiving the host name to establish a socket
					String peerAdress = dis.readUTF(); 
					
					// Establishing P2P Connection
					Socket connectToPeer = new Socket(peerAdress, 50010);
					DataOutputStream dos2 = new DataOutputStream(connectToPeer.getOutputStream());
					DataInputStream dis2 = new DataInputStream(connectToPeer.getInputStream());
					
					dos2.writeUTF("show file content");
					dos2.writeUTF(fileName);
					System.out.println("=================================================");
					String line;
					//load line by line
					line = dis2.readUTF(); // take the data from the peer
					while(line != null)
					{
						// printing the data
						System.out.println(line);
						try
						{
						line = dis2.readUTF();// take the data from the peer
						}
						catch(Exception e)
						{
							break;
						}
					}
					System.out.println("=================================================");
					
					dis2.close();
					dos2.close();
					connectToPeer.close();
					reader.nextLine(); // To ignore \n char
				}
				else if ( choice.compareToIgnoreCase("Show messages") == 0 )
				{
					dos.writeUTF(choice);//sending the choice to the center
					System.out.println("==================================================");
					for ( int i = 0 ; i < Peer.mailBox.size() ; i++ )
					{	// mailBox saved the all messages that are sent
						System.out.println(Peer.mailBox.get(i)); // print the message and its sender
					}
					System.out.println("==================================================");
				}
				else if (choice.compareToIgnoreCase("Send Message") == 0)
                {
					dos.writeUTF(choice); //sending the choice to the center
					System.out.println("Enter the user name : ");
                    String TargedUserName = reader.nextLine(); //take the receiver name
                    dos.writeUTF(TargedUserName); //sending the the name to the center
					// now screening process to check if the Entered user name if exists
                    String akg = dis.readUTF(); // get the ack from the center
                    if(akg.compareToIgnoreCase("no") == 0)
                    {
                        System.out.println("This user does not exist.");
                    }
                    else if (akg.compareToIgnoreCase("yes") == 0)
                    {
                        try
                        {
                        String HostOfOtherUser = dis.readUTF(); //get the hostName of the receiver
							// Establish P2P connection
						Socket ConnectMassage = new Socket( HostOfOtherUser , 50010);
                        DataOutputStream toPeer = new DataOutputStream(ConnectMassage.getOutputStream());
                        System.out.println("Enter the message : ");
                        String MassageSend = reader.nextLine(); // writing the message
                        toPeer.writeUTF(choice); // sending the operation that is required to the peer
                        toPeer.writeUTF(Peer.userName + " : " + MassageSend);

                        }
                        catch(Exception e)
                        {
                        	System.out.println(e.getMessage());
                        }
                    }

                }
				else if(choice.compareToIgnoreCase("List all files") == 0)
				{
					dos.writeUTF(choice);//sending the choice to the center
				    int num_of_files = dis.readInt(); //get How many file is available now ?
					// Now We receive file name by file name
					for(int i = 0 ; i < num_of_files ; i++)
				    {///show the file name
				        String fileName = dis.readUTF();
				        System.out.println(fileName);
				    }
				}
				else if (choice.compareToIgnoreCase("Exit") == 0) {
					dos.writeUTF(choice);//sending the choice to the center
					connectSocket.close();//closing the socket
					dis.close();
					dos.close();
					System.exit(0); //stop the app
				}
				
				else {
					System.out.println("Please enter correct input");
				}

			}
		} catch (Exception e) {
           System.out.println(e.getMessage());
		}

	}

}

class HiddenOperations extends Thread {
	Thread t;
	Socket connectSocket;
	Socket connectToPeer;
	String directory;
	HiddenOperations(Socket s, Socket p,String _directory) {
		connectSocket = s;
		connectToPeer = p;
		directory = _directory;
	}

	public void run() {
		try {
				
				DataOutputStream dos2 = new DataOutputStream(connectToPeer.getOutputStream());
				DataInputStream dis2 = new DataInputStream(connectToPeer.getInputStream());
				
				String operation = dis2.readUTF(); // Take the required operation from the peer
			if ( (operation.compareToIgnoreCase("Download") == 0) ) {
				String fileName = dis2.readUTF(); //get the file name wanted to download
				File file = new File(directory + "\\" + fileName);
				//loading the data
				byte[] bytes = new byte[16 * 1024];
				InputStream in = new FileInputStream(file);
				int count;
				while ((count = in.read(bytes)) > 0) {
					// uploading the data
					dos2.write(bytes, 0, count);
				}
				in.close();
			}
			else if ( (operation.compareToIgnoreCase("show file content") == 0 ))
			{
				String fileName = dis2.readUTF(); // get the file name
				File file = new File(directory + "\\" + fileName);
				BufferedReader inFromFile = new BufferedReader(new FileReader(file));//take data from the file
				String line; //load line
				line = inFromFile.readLine();
				while (line != null) {
					dos2.writeUTF(line); //send to the peer
					line = inFromFile.readLine(); //load next line
				}
				inFromFile.close(); //closing the file object
			}
			if (operation.compareToIgnoreCase("Send Message") == 0)
            {
                Peer.mailBox.add(dis2.readUTF()); //save the received messages in a mailBox (vector)
            }

			dos2.close();
			dis2.close();

		} catch (Exception e) {
				System.out.println("uploading problem");
				System.out.println(e.getMessage());
				
		}
	}

}

public class Peer {

	/**
	 * @param args
	 */
	public static String userName;
	// mailBox is a vector to save the messages tha are sent
	public static  Vector<String> mailBox =  new Vector<String>();
	public static void main(String[] args) {
		BufferedReader consoleReader = new BufferedReader( new InputStreamReader(System.in));
		try {
			
			Socket connectedSocket = new Socket("localhost", 50013);
			DataOutputStream dos = new DataOutputStream(connectedSocket.getOutputStream());
			DataInputStream dis = new DataInputStream(connectedSocket.getInputStream());
			dos.writeUTF(InetAddress.getLocalHost().getHostName());
			
			
			Boolean isUnique = false;
			while(isUnique == false)
			{
				System.out.println("Enter your username : ");
				userName = consoleReader.readLine();
				dos.writeUTF(userName); //sending the user name to check if it is unique
				isUnique = dis.readBoolean(); // read the response if it is unique or exist
												// if it unique will receive true and break , else will repeat
			}
		
			
			/// shared folder >> has what the user can share in the network
			System.out.println("Enter your shared folder : ");
			String directory = consoleReader.readLine(); // receive the folder name and its location
			File folder = new File(directory);
			File[] listOfFiles = folder.listFiles(); //list of the directory's files
			System.out.println(listOfFiles.length);
			for (File file : listOfFiles) { //loop over the directory
			    if (file.isFile()) {
			        dos.writeUTF(file.getName()); //get the file name
			    }
			}
			dos.writeUTF("*");
			
			
			Thread t = new UserThread(connectedSocket);;
			t.start();
			
			ServerSocket uploading = new ServerSocket(50010);
			while (true) {
				
				Socket connectToPeer = uploading.accept();
				Thread t2 = new HiddenOperations(connectedSocket,connectToPeer,directory);
	            t2.start();
			}

		
		}catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("couldn't connect");
		}

	}

}

