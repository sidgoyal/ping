import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import java.util.StringTokenizer;
//This is a simple ping client that sends and receives packets from the server. 
//Simulates a timeout of a server response based on the wait time
//Prints the summary of a ping response and calculates timings of all ping requests and responses
public class PINGClient {
	
	//Creates the randomized string of characters and digits of size bytes
	private static String payloadRest(int size)
	{
		int rightLimitletters = 122;
		int leftLimitletters = 97;
		int rightLimitnumbers = 57;
		int leftLimitnumbers = 48;
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(size);
		//randomly selects a letter for an even space and digit for an odd space
		for (int i = 0; i < size; i++)
		{
			if ((i%2)==0)
			{
				int randomLimitedInt = leftLimitletters + (int)(random.nextFloat() * (rightLimitletters - leftLimitletters + 1));
				buffer.append((char) randomLimitedInt);
			}
			
			if ((i%2) == 1)
			{
				int randomLimitedInt = leftLimitnumbers + (int)(random.nextFloat() * (rightLimitnumbers - leftLimitnumbers + 1));
				buffer.append((char) randomLimitedInt);
			}
		}
		return buffer.toString();
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
	
	public static void main(String argv[]) throws Exception
	{
		// socket variables
		DatagramSocket clientSocket;
		DatagramPacket sendPacket;
		DatagramPacket receivePacket;
		InetAddress IPAddress = null;
		
		// client variables
		String clientSentence, serverSentence;
		
		// command-line arguments
		int port = 0;
		String server;
		int clientID = 0;
		int numPackets = 0;
		int waitSec = 0;
		
		
		// process command-line arguments
		if (argv.length < 5) 
		{
			System.out.println ("ERR - there should be 5 arguments: hostname or ip address, port number, clientid, number of request packets, and wait time in seconds");
			System.exit (-1);
		}
		server = argv[0];
		try {
			port = Integer.parseInt(argv[1]);
		}
		catch (NumberFormatException e)
		{
			System.out.println("ERR - arg 2");
			System.exit(-1);
		}
		try {
			clientID = Integer.parseInt(argv[2]);
		}
		catch (NumberFormatException e)
		{
			System.out.println("ERR - arg 3");
			System.exit(-1);
		}
		try {
			numPackets = Integer.parseInt(argv[3]);
		}
		catch (NumberFormatException e)
		{
			System.out.println("ERR - arg 4");
			System.exit(-1);
		}
		try {
			waitSec = Integer.parseInt(argv[4]); 
		}
		catch (NumberFormatException e)
		{
			System.out.println("ERR - arg 5");
			System.exit(-1);
		}
		
		if (numPackets <= 0)
		{
			System.out.println("ERR - arg 4");
			System.exit(-1);
		}
		
		if (waitSec <= 0)
		{
			System.out.println("ERR - arg 5");
			System.exit(-1);
		}
		int responsePackets = 0;
		double minRTT = 0;
		double maxRTT = 0;
		double totalRTT = 0;
		int totalpayload = 0;
		
		int versionNumber = 1;
		
		
		// Create client socket to destination
		clientSocket = new DatagramSocket();
		try
		{
			IPAddress = InetAddress.getByName(server); //gets the ip address for the hostname
		}
		catch (UnknownHostException e)
		{
			System.out.println("ERR - arg 1, invalid hostname: " + server);
			System.exit(-1);
		}
		
		System.out.println("PINGClient started with server IP: " + IPAddress.getHostAddress() + ", port: " + port + ", client ID: " + clientID + ", packets: " + numPackets + ", wait: " + waitSec);

		//Binds to the server to ensure the port is open and PINGClient is able to connect to the server
		SocketAddress serverAddress = new InetSocketAddress(IPAddress, port);
		try
		{
			clientSocket.connect(serverAddress);
		}
		catch(Exception e)
		{
			System.out.println("ERR - arg 1 : Unable to connect to ip: " + IPAddress.getHostAddress() + " port: " + port);
			System.exit(-1);
		}
		
		clientSocket.setSoTimeout((waitSec)*1000);//setting the timeout value for receive in milliseconds
			
		//Creates each packet to send to the host and waits for a response from the server
		for (int i = 0; i < numPackets; i++)
		{
			long createTime = System.currentTimeMillis();//used to calculate RTT 
			
			//Generates a random number between 150 and 300 as the byte size of the payload of the packet
			int min = 150;
			int mx = 300;
			int size = (int) Math.floor(Math.random() * (mx - min +1) + min);
			totalpayload = totalpayload + size;
			
			// A new outputStream for every loop to send data
			ByteArrayOutputStream sendStream = new ByteArrayOutputStream(1024);

			//Sets up the header to send to the server
			Integer v = versionNumber;
			sendStream.write(v.byteValue());
			sendStream.write(intToBytes(clientID));
			sendStream.write(intToBytes(i));
			sendStream.write(longToBytes(createTime));
			sendStream.write(intToBytes(size));
			
			//Creates the payload to send to the server with the randomly generated string of random size

			// Sets up the payload string to send to the server
			String clientSentencePayload = server + " VCU-CMSC440-SPRING-2023 Goyal Nupur";
			byte[] payload = clientSentencePayload.getBytes();
			int payloadBytes = payload.length;
			String rest = payloadRest(size-payloadBytes);
			clientSentencePayload = clientSentencePayload + " " + rest;
			sendStream.write(clientSentencePayload.getBytes());
			byte[] sendData = sendStream.toByteArray();
		
			//Prints the Header and Payload in the proper format of the Ping Request Packet
			System.out.println("----------------Ping Request Packet Header-------------------");
			System.out.println("Version: " + versionNumber);
			System.out.println("ClientID: " + clientID);
			System.out.println("SequenceNo: " + i);
			System.out.println("TimeStamp: " + createTime);
			System.out.println("Payload Size: " + size);
			System.out.println("----------------Ping Request Packet Payload-------------------");
			System.out.println("Host: " + IPAddress.getHostName());
			System.out.println("Class-name: VCU-CMSC440-SPRING-2023");
			System.out.println("User-name: Goyal, Nupur");
			System.out.println("Rest: " + rest);
			System.out.println("-------------------------------------");
			System.out.println();
			
			
			// Create packet and send to server
			sendPacket = new DatagramPacket(sendData, sendData.length,  
				IPAddress, port);
			try 
			{
				clientSocket.send(sendPacket); //catch to make sure host exists or port is open
			}
			catch (Exception e)
			{
				System.out.println("ERR - Unable to send to ip: " + IPAddress.getHostAddress() + " port: " + port);
				System.exit(-1);
			}
		
			// Create receiving packet and receive from server
			byte[] receiveData = new byte[1024];
			receivePacket = new DatagramPacket(receiveData, receiveData.length);

			try {
				clientSocket.receive(receivePacket);
			}
			catch(PortUnreachableException e)
			{
				System.out.println("ERR - Unable to recieve from ip: " + IPAddress.getHostAddress() + " port: " + port);
				System.exit(-1);
			}
			catch(SocketTimeoutException e)
			{
				System.out.println("----------- Ping Response Packet Timed-Out ----------");
				System.out.println();
				continue;
			}
			catch (Exception e)
			{
				System.out.println("ERR - Unable to recieve from ip: " + IPAddress + " port: " + port);
				System.exit(-1);
			}
			serverSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
			
			//reads all the header information of the packet
			ByteArrayInputStream inStream = new ByteArrayInputStream(receiveData);
		
			int version = byteToInt(inStream);
		    int clientId = bytesToInt(inStream);
		    int seqNo = bytesToInt(inStream);
		    long receiveTime = bytesToLong(inStream);
		    int payloadsize = bytesToInt(inStream);
		    
		    //reads all the payload information of the packet
		    byte[] payLoadArray = new byte[size+1];
		    inStream.read(payLoadArray, 0, size + 1);
		    String payLoadString = new String(payLoadArray);
		    StringTokenizer s = new StringTokenizer(payLoadString, " ");
		    String clientHost = s.nextToken();
		    String className = s.nextToken();
		    String lastName = s.nextToken();
		    String firstName = s.nextToken();
		    String capsRest = s.nextToken();
		    
			responsePackets++;
			
			//Prints the Header and Payload in the proper format of the Ping Response Packet
			System.out.println("----------------Received Ping Response Packet Header-------------------");
			System.out.println("VERSION: " + version);
			System.out.println("CLIENTID: " + clientId);
			System.out.println("SEQUENCENO: " + seqNo);
			System.out.println("TIMESTAMP: " + receiveTime);
			System.out.println("PAYLOAD SIZE: " + payloadsize);
			System.out.println("----------------Received Ping Response Packet Payload-------------------");
			System.out.println("HOST: " + clientHost);
			System.out.println("CLASS-NAME: " + className);
			System.out.println("USER-NAME: " + lastName + ", " + firstName);
			System.out.println("REST: " + capsRest);
			System.out.println("-----------------------------------");			
			double RTT = receiveTime - createTime;
			if (i == 0)
			{
				minRTT = RTT;
			}
			if(RTT < minRTT)
			{
				minRTT = RTT;
			}
			if (RTT > maxRTT)
			{
				maxRTT = RTT;
			}
			totalRTT = totalRTT + RTT;
			System.out.println("RTT: " + (RTT/1000) + " seconds");	
			System.out.println();

		}
		// close the socket
		clientSocket.close();
		double packetloss = numPackets - responsePackets;
		DecimalFormat twod = new DecimalFormat("0.00");
		System.out.println("Summary: " + numPackets + " :: " + responsePackets + " :: " + twod.format(packetloss/numPackets) + " :: " + minRTT + " :: " + maxRTT + " :: " + twod.format(totalRTT/responsePackets) + "% :: " + twod.format(totalpayload/numPackets));
		
	} // end main
}
