import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.StringTokenizer;

// simulates a ping Server and randomly drops packets based on the loss value passed as an argument
public class PINGServer {
	
	// reads 4 bytes for an integer from input stream
	private static int bytesToInt(ByteArrayInputStream inStream)
	{
		byte[] value = new byte[4];
		inStream.read(value, 0, 4);
		return ByteBuffer.wrap(value).getInt();
	}
	
	// reads 8 bytes for a long from input stream
	private static long bytesToLong(ByteArrayInputStream inStream)
	{
		byte[] value = new byte[8];
		inStream.read(value, 0, 8);
		return ByteBuffer.wrap(value).getLong();
	}
	
	// reads 1 byte for an integer from input stream
	private static int byteToInt(ByteArrayInputStream inStream)
	{
		byte[] value = new byte[1];
		inStream.read(value, 0, 1);
		return ByteBuffer.wrap(value).get();
	}
	
	// returns a byteArray of 4 bytes to represent the integer value passed
	private static byte[] intToBytes(int value)
	{
		return ByteBuffer.allocate(4).putInt(value).array();
	}
	
	// returns a byteArray of 8 bytes to represent the long value passed
	private static byte[] longToBytes(long value)
	{
		return ByteBuffer.allocate(8).putLong(value).array();
	}
	
	public static void main(String argv[]) throws Exception
    {
		// socket variable
		DatagramSocket serverSocket = null;
	
		// command-line arguments
		int port = 0;
		int loss = 0;
		
		// process command-line arguments
		if (argv.length < 2) 
		{
			System.out.println ("ERR - not enough arguments");
			System.exit(-1);
		}
		try
		{
			port = Integer.parseInt(argv[0]);
			loss = Integer.parseInt(argv[1]);
		}
		catch (Exception e)
		{
			System.out.println("Only put a space between arguments");
			System.exit(-1);
		}
		
		if (port > 65536)
		{
			System.out.println ("ERR - arg 1 port: "+ port + " has to be smaller than 65536");
			System.exit(-1);
		}
		
		if (port < 10000 || port > 11000)
		{
			System.out.println("ERR - arg 1");
			System.exit(-1);
		}
		
		if (loss < 1 || loss > 100)
		{
			System.out.println("ERR - arg 2");
			System.exit(-1);
		}
		// Create welcoming socket using given port
		try {
			serverSocket = new DatagramSocket(port);
		}
		catch (SocketException e)
		{
			System.out.println("ERR - cannot create PINGServer socket using port number " + port);
			System.exit(-1);
		}
		
		InetAddress serverIPAddress = InetAddress.getLocalHost();
		System.out.println("PINGServer started with server IP: " + serverIPAddress.getHostAddress() + ", port: " + port + "...");

		// While loop to handle arbitrary sequence of clients making requests
		while (true)
		{
		
			// Waits for some client to send a packet
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket 
					(receiveData,receiveData.length);

			try {
				serverSocket.receive(receivePacket);
			}
			catch(Exception e)
			{
				System.out.println("ERR - not able to receive from client, server exiting ...");
				System.exit(-1);
			}
			
			InetAddress IPAddress = receivePacket.getAddress();
			int clientPort = receivePacket.getPort();
			
			// stream to read all the individuals parts of the packet
			ByteArrayInputStream inStream = new ByteArrayInputStream(receiveData);
		
			// read all the header information of the packet
			int version = byteToInt(inStream);
		    int clientId = bytesToInt(inStream);
		    int seqNo = bytesToInt(inStream);
		    long requestTime = bytesToLong(inStream);
		    int size = bytesToInt(inStream);
		    	    
		    // generate a random number to determine if the packet should be dropped
			Random r = new Random();
			int determineLoss = r.nextInt(100) + 1;
			
			//reads all the payload information of the packet
		    byte[] payLoadArray = new byte[size+1];
		    inStream.read(payLoadArray, 0, size + 1);
		    String payLoadString = new String(payLoadArray);
		    StringTokenizer s = new StringTokenizer(payLoadString, " ");// using space as a delimiter to separate the payload variables
		    String clientHost = s.nextToken();
		    String className = s.nextToken();
		    String lastName = s.nextToken();
		    String firstName = s.nextToken();
		    String rest = s.nextToken();
		    
		    boolean dropped = false;
			if (determineLoss <= loss)
			{
				dropped = true;
				System.out.println("IP:" + IPAddress + " :: Port:" + clientPort + " :: ClientID:" + clientId + " :: Seq#:" + seqNo + " :: DROPPED");
			}
			else {
				System.out.println("IP:" + IPAddress + " :: Port:" + clientPort + " :: ClientID:" + clientId + " :: Seq#:" + seqNo + " :: RECEIVED");
			}
				
			//Prints the Header and Payload in the proper format of the Received Ping Request Packet
			System.out.println("----------------Received Ping Request Packet Header-------------------");
			System.out.println("Version: " + version);
			System.out.println("ClientID: " + clientId);
			System.out.println("SequenceNo: " + seqNo);
			System.out.println("TimeStamp: " + requestTime);
			System.out.println("Payload Size: " + size);
			System.out.println("----------------Received Ping Request Packet Payload-------------------");
			System.out.println("Host: " + clientHost);
			System.out.println("Class-name: " + className);
			System.out.println("User-name: " + lastName + ", " + firstName);
			System.out.println("Rest: " + rest);
			System.out.println("-----------------------------------");
			System.out.println();
				
			if (!dropped)
			{				
				// stream to send back the ping response
				ByteArrayOutputStream sendStream = new ByteArrayOutputStream(1024);
				
				Integer v = version;
				
				long receiveTime = System.currentTimeMillis();
				//Sets up the header to send to the client
				sendStream.write(v.byteValue());
				sendStream.write(intToBytes(clientId));
				sendStream.write(intToBytes(seqNo));
				sendStream.write(longToBytes(receiveTime));
				sendStream.write(intToBytes(size));
				
				// Convert the random payload string to all caps
				String serverSentence = payLoadString.toUpperCase();
				
				
				//Prints the Header and Payload in the proper format of the Ping Request Packet
				System.out.println("----------------Ping Response Packet Header-------------------");
				System.out.println("Version: " + version);
				System.out.println("ClientID: " + clientId);
				System.out.println("SequenceNo: " + seqNo);
				System.out.println("TimeStamp: " + receiveTime);
				System.out.println("Payload Size: " + size);
				System.out.println("----------------Ping Response Packet Payload-------------------");
				System.out.println("Host: " + clientHost.toUpperCase());
				System.out.println("Class-name: " + className.toUpperCase());
				System.out.println("User-name: " + lastName.toUpperCase() + ", " + firstName.toUpperCase());
				System.out.println("Rest: " + rest.toUpperCase());
				System.out.println("-------------------------------------");
				System.out.println();
				
				//Creates the payload to send to the client
				sendStream.write(serverSentence.getBytes());
				byte[] sendData = sendStream.toByteArray();
				
				// Write output line to socket
				DatagramPacket sendPacket = new DatagramPacket(sendData, 
						sendData.length, 
						IPAddress, 
						clientPort);
				try {
					serverSocket.send(sendPacket);
				}
				catch (Exception e) 
				{
					System.out.println("ERR - unable to respond to client: " + clientId + " on port: " + clientPort);
					System.out.println();
				}
			}
		} //  end while; loop back to accept a new client connection 
    } // end main
}
