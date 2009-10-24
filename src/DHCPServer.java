import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;



public class DHCPServer {
	private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
	private static final int LIMIT_IP = 255;
	private static int maxNumIP;
	private static int listenPort = 67;
	private static int clientPort = 68;
	
	
	private static DatagramSocket socket = null;
	
	
	
	private ArrayList[] options = new ArrayList[4];

	private static byte[] subnet = new byte[4]; //192.168.1.0 
	private static byte[] subnetMask = new byte[]{(byte) 255,(byte) 255,(byte) 255,0}; 
	private static byte[] router = new byte[]{(byte) 192,(byte) 168,1,(byte)254};
	
	//Table Data
	private static byte[][] ipTable = new byte[254][4]; //0,255 reserved(1-254 assignable)
	private static byte[][] macTable = new byte[254][6];
	private static byte[][] hostNameTable = new byte[254][];
	private static long[] leaseStartTable = new long[254];
	private static int[] leaseTimeTable = new int[254];
	
	private static DHCPTable dhcpTable;
	private static int numAssigned = 0;
	
	private static long startTime;
	private static long defLeaseTime = 3600; //1 hour lease default
	
	private static String NL;
	
	public DHCPServer(int servePort, String config) {
		readConfigFile(config);
		new DHCPServer(servePort);
	}
	
	public DHCPServer(int servePort) {
		if (servePort > 0) {
			listenPort = servePort;
		}
		new DHCPServer();
	}

	public DHCPServer() {
		//set server start time
		startTime = System.currentTimeMillis();
		
		//clear ip table
		for (int i=0; i < ipTable.length; i++) {
			ipTable[i][0] = 0;
			ipTable[i][1] = 0;
			ipTable[i][2] = 0;
			ipTable[i][3] = 0;
			hostNameTable[i] = new String("").getBytes();
		}
		
		//calculate netmask address
		System.out.println("Router IP: " + DHCPUtility.printIP(router));
		System.out.println("SubnetMask: " +  DHCPUtility.printIP(subnetMask));
		subnet[0] = (byte) (subnetMask[0] & router[0]);
		subnet[1] = (byte) (subnetMask[1] & router[1]);
		subnet[2] = (byte) (subnetMask[2] & router[2]);
		subnet[3] = (byte) (subnetMask[3] & router[3]);
		System.out.println("sn & r = network addr: " + DHCPUtility.printIP(subnet));
		
		//calculate max number of assignable ip addresses for this subnet
		
		
		BitSet subnetBits = DHCPUtility.bytes2Bits(DHCPServer.subnet);
		boolean done = false;
		int count = 32;
		for (int i=subnetBits.length()-1; i >= 0 && !done ; i--) {
			if (subnetBits.get(i)) {
				count = count - (i + 1);
				done = true;
			}
		}
		
		maxNumIP = (int) (Math.pow(2, count) - 2); //-2 for broadcast and subnet address
		System.out.println("count: " + count + " maxNumIp: 2^" + count + " - 2 = " + maxNumIP);
		//assert(maxNumIP <= LIMIT_IP) : "ip out of range?";
		
		try {
			socket = new DatagramSocket(listenPort);
			//System.out.println("Success! Now listening on port " + listenPort + "...");
			System.out.println("Listening on port " + listenPort + "...");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // ipaddress? throws socket exception
		
		//create table
		createTable();
		
		NL = System.getProperty("line.separator");
		log("log.txt", "DHCPServer: init complete server started" + NL);
		
		
		/*//showTable();
		for (int i=0; i < 254; i++) {
			addAssignedIP(assignIP(new byte[]{0,0,0,0}));
		}*/
		
		
	}



	public static byte[] receivePacket() {
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
		//System.out.println("Data Received: " + Arrays.toString(p.getData()));
		//log("log.txt", "DHCPServer: packet received");
		
		return p.getData();

	}
	
	public static void sendPacket(String clientIP, byte[] payload) {
		assert(payload.length <= MAX_BUFFER_SIZE);
	
		try {
			DatagramPacket p = new DatagramPacket(payload, payload.length, InetAddress.getByName(clientIP), clientPort);
			System.out.println("Sending data: " + 
					//Arrays.toString(p.getData()) + 
					"to " + p.getAddress().toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void broadcastPacket(byte[] payload) {
		assert(payload.length <= MAX_BUFFER_SIZE);
	
		try {
			String broadcastIP = "255.255.255.255";
			DatagramPacket p = new DatagramPacket(payload, payload.length, InetAddress.getByName(broadcastIP), clientPort);
		    //System.out.println("Broadcasting data: " + Arrays.toString(p.getData()));
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
			server = parseArgs(args);
		} else {
			server = new DHCPServer();
		}
		//server is always listening
		boolean listening = true;
		while (listening) {
			byte[] packet = receivePacket();
			process(packet);
		}
	}

	private static DHCPServer parseArgs(String[] args) {
		int port = -1;
		String config = "config.txt";
		for (int i=0; i < args.length; i++) {
			if (args[i].toUpperCase().matches("-P") || args[i].toUpperCase().matches("-PORT")) {
				port = Integer.parseInt(args[i+1]);
			} else if (args[i].toUpperCase().matches("-C") || args[i].toUpperCase().matches("-CONFIG")) {
				config = args[i+1];
			}
		}
		return new DHCPServer(port, config);
	}

	private static void process(byte[] msg) {
		DHCPMessage request = new DHCPMessage(msg);
		byte msgType = request.getOptions().getOptionData(DHCPOptions.DHCPMESSAGETYPE)[0]; 
		
		if (request.getOp() == DHCPMessage.DHCPREQUEST) {
			if (msgType == DHCPOptions.DHCPDISCOVER) {
				System.out.println("DHCP Discover Message Received");
				log("log.txt", "DHCPServer: DHCP Discover Message Received"+ NL + request.toString());
				byte[] offer = createOfferReply(request);
				System.out.println("Broadcasting Offer Reply");
				log("log.txt", "DHCPServer: Broadcasting Offer Reply" + NL);
				broadcastPacket(offer);
			} else if (msgType == DHCPOptions.DHCPREQUEST) {
				System.out.println("DHCP Request Message Received");
				log("log.txt", "DHCPServer: DHCP Request Message Received" + NL + request.toString());
				byte[] ack = createACKReply(request);
				System.out.println("Sending ACK reply to " + request.printCHAddr());
				log("log.txt", "DHCPServer: Sending ACK reply to " + request.printCHAddr());
				broadcastPacket(ack);
			} else if (msgType == DHCPOptions.DHCPINFORM) {
					System.out.println("DHCP Inform Message Received");
					log("log.txt", "DHCPServer: DHCP Inform Message Received" + NL + request.toString());
			} else if (msgType == DHCPOptions.DHCPDECLINE) {
				//client arp tested and ip is in use..
				//possibly delete record of client?
				System.out.println("DHCP Decline Message Received");
				log("log.txt", "DHCPServer: DHCP Decline Message Received" + NL + request.toString());
			} else if (msgType == DHCPOptions.DHCPRELEASE) {
				//client relinquishing lease early
				//possibly delete binding record of client?
				System.out.println("DHCP Decline Message Received");
				log("log.txt", "DHCPServer: DHCP Decline Message Received" + NL + request.toString());
			} else {
				System.out.println("Unknown DHCP Message Type: " + msgType);
				log("log.txt", "DHCPServer: Unknown DHCP Message Type:" + msgType);
			}
		} else {
			System.out.println("DHCP Reply Message received, where DHCP Request was expected!");
			log("log.txt", "DHCP Reply Message received, where DHCP Request was expected!");
		}
	}

	private static byte[] createACKReply(DHCPMessage request) {

		// compare request ip, to transaction offer ip, ensure it is still
		// unique

		/*byte[] ip = assignIP(request.getGIAddr());
		addAssignedIP(ip);*/
		int row = -1;
		for (int i=0; i < macTable.length; i++) {
			if (DHCPUtility.isEqual(request.getCHAddr(), macTable[i])) {
				row = i;
			}
	    }	
		assert(row >= 0) : "mac address not matching";
		
		/*if (request.getHType() == DHCPMessage.ETHERNET10MB) {
			macTable[row] = request.getCHAddr();
		} else {
			assert (false) : "unimplemented";
		}*/

		hostNameTable[row] = new String("test").getBytes();
		leaseStartTable[row] = System.currentTimeMillis();
		leaseTimeTable[row] = 3600; // 1 hour least time

		
		// ip is now unique
		// offer ip to requesting client

		DHCPMessage ackMsg = new DHCPMessage();
		System.out.println(request.getXid() + " " + DHCPUtility.printMAC(request.getCHAddr())+ " " + DHCPUtility.printMAC(macTable[row]) + " " + DHCPUtility.printIP(ipTable[row]));
		ackMsg.ackMsg(request.getXid(), request.getCHAddr(), ipTable[row]);
		return ackMsg.externalize();
	}

	private static byte[] createOfferReply(DHCPMessage discover) {
		macTable[numAssigned] = discover.getCHAddr();
		
		byte[] ip = assignIP(discover.getGIAddr());
		addAssignedIP(ip);
		
		
		
		//ip is now unique
		//offer ip to requesting client
		
		DHCPMessage offerMsg = new DHCPMessage();
		offerMsg.offerMsg(discover.getXid(), discover.getCHAddr(),ip);
		return offerMsg.externalize();
	}
	
	//generate a unique ip appropriate to gateway interface subnet address
	private static byte[] assignIP(byte[] gIAddr) {
		byte[] ip = new byte[4];
		
		//DHCPMessage originated from same subnet
		if (DHCPUtility.printIP(gIAddr).compareTo("0.0.0.0") == 0) {
			System.out.println("DHCPMessage originated from same subnet");
			ip = new byte[]{subnet[0], subnet[1], subnet[2], subnet[3]};
			
			//search ip table for unique ip
			for (int i=1; i <= maxNumIP && !isUnique(ip); i++) {
				if (ip[3] + i - 256 >= 0) {
					ip[2]++;
				}
				ip[3] = (byte) ((ip[3] + 1) % 256);
			}
			
			//unique ip to assign is:
			System.out.println("Assigning ip: " + DHCPUtility.printIP(ip));
			
		} else { //DHCPMessage originated from gIAddr subnet
			assert(false) : "unimplemented"; 
		}
		return ip;
	}
	


	//verify if ip is unique in the ip table
	private static boolean isUnique(byte[] ip){
		boolean done = false;
		boolean isUnique = true;
		for (int i=0; i < ipTable.length && isUnique && !done ; i++) {
			if (ip[0] == ipTable[i][0] && ip[1] == ipTable[i][1] &&
				ip[2] == ipTable[i][2] && ip[3] == ipTable[i][3]) {
					isUnique = false;
			} else if(ipTable[i][0] == 0 && ipTable[i][1] == 0 &&
				      ipTable[i][2] == 0 && ipTable[i][3] == 0) {
					  	done = true;
			}
		}
		return isUnique;
	}
	
	//add ip to list of assigned ips
	private static void addAssignedIP(byte[] assignedIP) {
		boolean done = false;
		for (int i=0; i < ipTable.length && !done; i++) {
			if (ipTable[i][0] == 0 && ipTable[i][1] == 0 &&
				ipTable[i][2] == 0 && ipTable[i][3] == 0) {
					ipTable[i] = assignedIP;
					System.out.println("adding " + DHCPUtility.printIP(assignedIP) + " to table");
					numAssigned++;
					//showTable();
					done = true;
			}
		}
	}
	
	private static void readConfigFile(String fileName) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			while (line != null) {
				if (line.trim().startsWith("#")) {
					//System.out.println("comment ignored: " + line);
				} else if (line.trim().toUpperCase().startsWith("SUBNETMASK")) {
					byte[] ip =  DHCPUtility.strToIP(line);
					if (ip != null) subnetMask = ip;
				} else if (line.trim().toUpperCase().startsWith("ROUTER")) {
					byte[] ip =  DHCPUtility.strToIP(line);
					if (ip != null) router = ip;
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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

	public static void setRouter(byte[] router) {
		DHCPServer.router = router;
	}

	public static byte[] getRouter() {
		return router;
	}
	public static byte[] getSubnetMask() {
		return subnetMask;
	}

	public static void setSubnetMask(byte[] subnetMask) {
		DHCPServer.subnetMask = subnetMask;
	}
	
	public static long upTime() {
		return System.currentTimeMillis()-startTime;
	}
	
	public static String printUpTime(){
		return new String("Server Uptime: " + upTime()+"ms");
	}
	
/*	public static void showTable() {
		//dhcpTable.repaint();
		
	}*/
	
	private static void createTable() {
		TableModel dataModel = new AbstractTableModel() {
			String[] columnNames = {"IP Address",
		            "MAC Address",
		            "Hostname",
		            "Lease Start Time",
		            "Lease Time"};
			
			public String getColumnName(int col) {
		        return columnNames[col].toString();
		    }


			public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {
				return numAssigned;
			}

			public Object getValueAt(int row, int col) {
				Object ret = null;
				switch (col) {
				case 0:
					ret =  DHCPUtility.printIP(ipTable[row]);
					break;
				case 1:
					ret =  DHCPUtility.printMAC(macTable[row]);
					break;
				case 2:
					ret =  new String(hostNameTable[row]);
					break;
				case 3:
					ret =  leaseStartTable[row];
					break;
				case 4:
					ret =  leaseTimeTable[row];
					break;
				}
				//fire cell update event so table refreshes itself
				fireTableCellUpdated(row, col);
				return ret;
			}
		};
		dhcpTable = new DHCPTable();
		dhcpTable.setModel(dataModel);
	}
}