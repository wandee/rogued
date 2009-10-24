import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Arrays;

public class DHCPClient {
	private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
	private static int listenPort =  68;
	private static String serverIP = "127.0.0.1";
	private static int serverPort =  67;
	
	private static DatagramSocket socket = null;
	private static boolean assignedIP;
	private static byte[] clientIP;
	
	private static long startTime;
	private static String logFileName = "ClientLog.txt";
	private static String NL;

	/*
	 * public DHCPClient(int servePort) { listenPort = servePort; new
	 * DHCPServer(); }
	 */

	public DHCPClient() {
		//set client start time
		startTime = System.currentTimeMillis();
		
		System.out.println("Connecting to DHCPServer at " + serverIP + " on port " + serverPort + "...");
		try {
			socket = new DatagramSocket(listenPort);  // ipaddress? throws socket exception
			assignedIP = false;   //ip not assigned when client starts
			
			//bind socket to correct source ip
			/*if (assignedIP) {
				InetSocketAddress assigned;
				assigned = new InetSocketAddress(
						InetAddress.getByAddress(clientIP), listenPort);
				socket.bind(assigned);
			} else {  //source ip is 0.0.0.0 when requesting ip
				InetSocketAddress broadcast = new InetSocketAddress(
						InetAddress.getByName("0.0.0.0"), listenPort);
				//socket.close();
				socket = new DatagramSocket(null);
				socket.bind(broadcast);
			}*/
			
			
			//sendTestPacket();
			

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NL = System.getProperty("line.separator");
		log(logFileName, "DHCPClient: init complete server started" + NL);
	}

	public DHCPClient(int parseInt) {
		listenPort = parseInt;
	}

	private void sendTestPacket() {
		//byte[] payload = new byte[MAX_BUFFER_SIZE];
		int length = 6;
		byte[] payload = new byte[length];
		payload[0] = 'h';
		payload[1] = '3';
		payload[2] = 'l';
		payload[3] = 'l';
		payload[4] = 'o';
		payload[5] = '!';
		DatagramPacket p;
		try {
			p = new DatagramPacket(payload, length, InetAddress.getByName(serverIP), serverPort);
			//System.out.println("Connection Established Successfully!");
			System.out.println("Sending data: " + Arrays.toString(p.getData()));
			socket.send(p); //throws i/o exception
			socket.send(p);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public static  void sendPacket(byte[] payload, String serverIP) {
		assert(payload.length <= MAX_BUFFER_SIZE);
		
			try {

				DatagramPacket p = new DatagramPacket(payload, payload.length, InetAddress.getByName(serverIP), serverPort);
				System.out.println("Sending data: " + 
						//Arrays.toString(p.getData()) + 
						"to " + p.getAddress().toString());
				socket.send(p); //throws i/o exception
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	public static  void broadcastPacket(byte[] payload) {
		assert(payload.length <= MAX_BUFFER_SIZE);
		String broadcastIP = "255.255.255.255";
		sendPacket(payload,broadcastIP);
	}
	
	public static byte[] receivePacket() {
		System.out.println("Listening on port " + listenPort + "...");
		byte[] payload = new byte[MAX_BUFFER_SIZE];
		int length = MAX_BUFFER_SIZE;
		DatagramPacket p = new DatagramPacket(payload, length);
		
		try {
			socket.receive(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // throws i/o exception

		System.out.println("Connection established from " + p.getPort()+ p.getAddress());
		System.out.println("Data Received: " + Arrays.toString(p.getData()));
		//log("log.txt", "DHCPServer: packet received");
		
		return p.getData();

	}
	
	private static void log(String fileName, String transcript) {
		Timestamp logTime = new Timestamp(System.currentTimeMillis());
		String data = new String(logTime.toString() + " - " + printUpTime() + NL + transcript + NL);
		System.out.println(data);
		 try {
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(fileName,true));
			outputStream.write(data);
			outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static long upTime() {
		return System.currentTimeMillis()-startTime;
	}
	
	public static String printUpTime(){
		return new String("Server Uptime: " + upTime()+"ms");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DHCPClient client;
		
		if (args.length >= 1) { 
			client = new DHCPClient(Integer.parseInt(args[0])); 
		} else {
			client = new DHCPClient();

		}
		 
	    DHCPMessage msgTest = new DHCPMessage();
		msgTest.discoverMsg(DHCPUtility.getMacAddress());
		DHCPUtility.printMacAddress();
		broadcastPacket(msgTest.externalize());
		log(logFileName, "DHCPClient: Broadcasting Discover Message");
		DHCPMessage msg = new DHCPMessage(receivePacket());
		msg.printMessage();
		log(logFileName, "DHCPClient: DHCP Offer Message Received"+ NL + msg.toString());
		msgTest.requestMsg(DHCPUtility.getMacAddress(), new byte[]{(byte)192,(byte)168,1,9});
		broadcastPacket(msgTest.externalize());
		log(logFileName, "DHCPClient: Broadcasting Request Message");
		DHCPMessage msg1 = new DHCPMessage(receivePacket());
		msg1.printMessage();
		log(logFileName, "DHCPClient: DHCP ACK Message Received"+ NL + msg1.toString());
		// }

	}
	
	
}
