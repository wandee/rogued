package DHCPService;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class DHCPServer {
	private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
	private static int listenPort = 67;
	private static int clientPort = 68;
	
	private  DatagramSocket socket = null;
	
	private ArrayList[] options = new ArrayList[4];

	public DHCPServer(int servePort) {
		listenPort = servePort;
		new DHCPServer();
	}

	public DHCPServer() {
		//System.out.println("Opening UDP Socket On Port: " + listenPort);

		try {
			socket = new DatagramSocket(listenPort);
			//System.out.println("Success! Now listening on port " + listenPort + "...");
			System.out.println("Listening on port " + listenPort + "...");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // ipaddress? throws socket exception


	}
	/**
	 * Broadcasts an offer packet to the subnet
	 */
	public void offerPacket() {
		byte[] offerIP = new byte[4];
		byte[] cMacAddress = new byte[16];
		DHCPMessage offer = new DHCPMessage();
	    byte[] payload = offer.offerMsg(cMacAddress, offerIP);
	    broadcastPacket(offer.externalize());
		
	}
	
	public  byte[] receivePacket() {
		byte[] payload = new byte[MAX_BUFFER_SIZE];
		int length = MAX_BUFFER_SIZE;
		DatagramPacket p = new DatagramPacket(payload, length);
		
		try {
			assert(socket != null);
			assert(p != null);
			socket.receive(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // throws i/o exception

		System.out.println("Connection established from " + p.getPort()+ p.getAddress());
		System.out.println("Data Received: " + Arrays.toString(p.getData()));
		
		return p.getData();

	}
	
	public  void sendPacket(String clientIP, byte[] payload) {
		assert(socket != null);
		assert(payload.length <= MAX_BUFFER_SIZE);
	
		try {
			DatagramPacket p = new DatagramPacket(payload, payload.length, InetAddress.getByName(clientIP), clientPort);
			System.out.println("Sending data: " + Arrays.toString(p.getData()) + "to :" + p.getAddress().toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public  void broadcastPacket(byte[] payload) {
		assert(payload.length <= MAX_BUFFER_SIZE);
	
		try {
			String broadcastIP = "255.255.255.255";
			DatagramPacket p = new DatagramPacket(payload, payload.length, InetAddress.getByName(broadcastIP), clientPort);
			System.out.println("Broadcasting data: " + Arrays.toString(p.getData()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 */


	public static void main(String[] args) {
		DHCPServer server;
		if (args.length >= 1) {
			server = new DHCPServer(Integer.parseInt(args[0]));
		} else {
			server = new DHCPServer();
		}
		//server is always listening
		boolean listening = true;
		while (listening) {
			byte[] packet = server.receivePacket();
			server.process(packet);
		}
	}
	
	
	private  void process(byte[] msg) {
		DHCPMessage request = new DHCPMessage(msg);
		byte msgType = request.getOptions().getOptionData(DHCPOptions.DHCPMESSAGETYPE)[0]; 
		
		if (request.getOp() == DHCPMessage.DHCPREQUEST) {
			if (msgType == DHCPOptions.DHCPDISCOVER) {
				System.out.println("DHCP Discover Message Received");
			} else if (msgType == DHCPOptions.DHCPREQUEST) {
				System.out.println("DHCP Request Message Received");
			} else {
				System.out.println("Unknown DHCP Message Type. Type: " + msgType);
			}
		} else {
			System.out.println("DHCP Reply Message received, where DHCP Request was expected!");
		}
	}

}
