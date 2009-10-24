import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class DHCPClient {
	private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
	private static int listenPort =  68;
	private static String serverIP = "127.0.0.1";
	private static int serverPort =  67;
	
	private static DatagramSocket socket = null;
	private static boolean assignedIP;
	private static byte[] clientIP;
	
	
	

	/*
	 * public DHCPClient(int servePort) { listenPort = servePort; new
	 * DHCPServer(); }
	 */

	public DHCPClient() {
		System.out.println("Connecting to DHCPServer at " + serverIP + " on port " + serverPort + "...");
		try {
			socket = new DatagramSocket(listenPort);  // ipaddress? throws socket exception
			assignedIP = false;   //ip not assigned when client starts
			
			//sendTestPacket();
			

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	public static  void sendPacket(byte[] payload) {
		assert(payload.length <= MAX_BUFFER_SIZE);
		
			try {
				//bind socket to correct source ip
				if (assignedIP) {
					InetSocketAddress assigned;
					assigned = new InetSocketAddress(
							InetAddress.getByAddress(clientIP), listenPort);
					socket.bind(assigned);
				} else {  //source ip is 0.0.0.0 when requesting ip
					InetSocketAddress broadcast = new InetSocketAddress(
							InetAddress.getByName("0.0.0.0"), listenPort);
					socket.close();
					socket = new DatagramSocket(null);
					socket.bind(broadcast);
				}
				
				DatagramPacket p = new DatagramPacket(payload, payload.length, InetAddress.getByName(serverIP), serverPort);
				System.out.println("Sending data: " + Arrays.toString(p.getData()));
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
		String temp = serverIP;
		serverIP = "255.255.255.255";
		sendPacket(payload);
		serverIP = temp;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DHCPClient client;
		/*
		 * if (args.length >= 1) { server = new
		 * DHCPClient(Integer.parseInt(args[0])); } else {
		 */
		client = new DHCPClient();
	    DHCPMessage msgTest = new DHCPMessage();
		//msgTest.discoverMsg(getMacAddress());
		printMacAddress();
		//sendPacket(msgTest.externalize());
		msgTest.requestMsg(getMacAddress(), new byte[]{(byte)192,(byte)168,1,9});
		sendPacket(msgTest.externalize());
		// }

	}
	
	public static byte[] getMacAddress() {
		byte[] mac = null;
		try {
			InetAddress address = InetAddress.getLocalHost();

			/*
			 * Get NetworkInterface for the current host and then read the
			 * hardware address.
			 */
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			mac = ni.getHardwareAddress();

			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		assert(mac != null);
		return mac;
	}
	
	public static void printMacAddress() {
		try {
			InetAddress address = InetAddress.getLocalHost();

			/*
			 * Get NetworkInterface for the current host and then read the
			 * hardware address.
			 */
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			byte[] mac = ni.getHardwareAddress();

			System.out.print("Hardware Address for current adapter: ");
			
			/*
			 * Extract each array of mac address and convert it to hexa with the
			 * . * following format 08-00-27-DC-4A-9E.
			 */
			for (int i = 0; i < mac.length; i++) {
				System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? "-"
						: "");
			}
			
			System.out.print("\n");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

}
