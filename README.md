# ping
PINGClient 
The PINGClient sends ping packets and receive ping responses from your ping server.  
The PINGClient accepts five command-line arguments: a. The first one is either the <hostname> or the <ip> of your ping server, b. The second is the <port> number your server is running on, the third one is the <ClientID>, the fourth argument is the <number_of_ping_request_packets> to send to the  server, and the fifth argument is the number of <wait> seconds that the client will wait  to receive a response for each request packet. 
Ping client constructs and send <number_of_ping_request_packets> ping request packets using a UDP connection to the ping server identified by the  hostname/IP and port described earlier. 
The client constructs and send a valid PING packet consisting of PING  header and PING payload.  
Each packet sent by the client should has a payload of random data of size between 150 to 300 bytes.  
The PING header includes the following fields: 
o “Version” field of a byte length that includes the version number of your  ping system. Let the value of this field to be “1” corresponding to Ver 1.0, o “ClientID” field of type integer is set to the client ID from the command line argument and is used to identify the client, 
o “SequenceNo” field of type integer. The value starts at 1 and progresses to  <number of packets> for each successive ping message sent by the client, o “Timestamp” field of type float. The client sets this value to the current time when this packet is constructed, 
o “Size” field of type integer that is set to the number of bytes (size) of the payload portion of the ping packet. It is randomly selected between 150 and 300 bytes.  
The PING payload includes the following lines 
o "Host: <hostname>" where <hostname> is the hostname of the client or the ip, 
o "Class-name: VCU-CMSC440-SPRING-2023", 
o "User-name: <your last name>, <your first name>". 
o “Rest: <the remaining bytes to fill the random payload size>”.
• After sending each request packet, the client waits up to <wait> second for a ping response from the server before sending the next packet. Because UDP is an unreliable protocol, a packet sent from the client to the server or from the server to the client may be lost in the network. If no reply is received within <wait> seconds, the client assumes that the packet is lost during transmission across the network, and then proceeds with the transmission of the next packet. 
If a response is received from the server, the client proceeds immediately with the transmission of the next ping request packet. Moreover, the client parses the received response packet and does the following:  
o Prints the reply packet (header and payload) using the same format described earlier for transmitted ping packets apart from the divider lines that should be “----------- Received Ping Response Packet Header ----------” and “-------- -- Received Ping Response Packet Payload -------------”.  
o Calculates and prints the round-trip time (RTT), in seconds in the following format “RTT: <RTT> seconds”. RTT is calculated by calculating the difference between the time the response packet is received by the client, and  the timestamp field in the packet, which was set when the packet was created.  
• If the client times out and did not receive a reply for the packet that was just sent, the client prints: “--------------- Ping Response Packet Timed-Out ----------- -------” 
• After the client is done with the transmission of all ping request packets, it prints a summary of the ping process. In particular, the client needs to print the number of transmitted ping requests, number of received ping response, minimum,  maximum, and average RTTs at the end of all ping packets. In addition, the client calculates and prints the packet loss rate (in percentage). 

PINGServer 
• The program accepts two command-line arguments: the first is <port>, which is the port that it will listen on for incoming pings from clients, and the second is <loss>, which is the percentage of packet drop. 
o If any of the arguments are incorrect, exit after printing an error message of  the form “ERR - arg x”, where x is the argument number. 
o The only error-checking that needs to be done on the port is to ensure it is a  positive integer less than 65536. 
If the program is successful in creating the server socket using the input port number argument, your program prints this out in the format of:  
o “PINGServer started with server IP: <ip>, port: <port> …”, where  <port> is the input argument, and <ip> is the IP address of the server  machine. 
If your program is unsuccessful in creating the socket using the input port number argument, it is because this port number is already being taken by another active socket. 
o In this case, the program exits after printing an error message “ERR - cannot create PINGServer socket using port number <port>”, where port is the input argument. 
If the socket is created successfully, the server sits in an infinite loop listening for incoming UDP packets. 
When a packet arrives, the server simply capitalizes the encapsulated data in the received packet and then sends it back to the client. 
In this project, we assume that <loss>% of the client’s packets will be lost. We simulate this in the server code in which when a packet arrives to the server, the server generates a random integer number in the range of [1, 100]. If the randomized integer is less than or equal to <loss>, the server ignores the received packet (simulating a packet drop). If the randomized integer is greater than <loss>, the server resumes normally and handle the received packet as described earlier. 
For each packet arrives to the server,  
o The program prints the client's IP address, port, packet’s sequence number, and whether the packet will be dropped or not in the format of  
▪ “IP:<ip> :: Port:<port> :: ClientID:<ClientID> :: Seq#:<Seq#> :: DROPPED” in case if the packet will be ignored such as “IP:10.0.0.3 :: port:63307 :: ClientID:333 :: Seq#:56 :: DROPPED”, 
▪ or “IP:<ip> :: Port:<port> :: ClientID:<ClientID> :: Seq#:<Seq#> ::  RECEIVED” otherwise.  
o Prints the header and the payload of the received ping packet in the following format: 
▪ ----------Received Ping Request Packet Header---------- 
▪ Version: <Version field> 
▪ Client ID: <ClientID field> 
▪ Sequence No.: <SequenceNo field> 
▪ Time: <Timestamp field> 
▪ Payload Size: <Size field> 
▪ ---------Received Ping Request Packet Payload------------ 
▪ Host: <hostname> 
▪ Class-name: VCU-CMSC440-SPRING-2023 
▪ User-name: <your last name>, <your first name> 
▪ Rest: <the remaining random payload data> 
▪ --------------------------------------- 
Constructs a valid ping response packet including: 1) the ping response header fields (Version, ClientID, SequenceNo, Timestamp, Size) that should copy the  corresponding values in the received ping packet, and 2) the ping response payload that includes capitalization of each line of the payload of the received ping packet. Note that the payload size of the response packet should be equal to the payload size of ping request packet.  
Prints the reply packet (header and payload) using the same format described earlier for transmitted ping packets apart from the divider lines that should be “----------- Ping Response Packet Header ----------” and “---------- Ping Response Packet Payload -------------”.  
The ping server program remains running until the user closes it with Ctrl-C.
