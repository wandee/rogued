package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import DHCPService.DHCPServer;
import junit.framework.TestCase;

public class DHCPServerTest extends TestCase {
	public void testBind() {
//		class serverThread extends Thread {	
//			public void run() {
//				DHCPServer s = new DHCPServer(8080);
//				byte[] receive = s.receivePacket();
//			}
//		}
		
//		(new serverThread()).start();
	
		DHCPServer.main(new String[]{"8080"});
		
		
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(8081);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("socket exception occured");
		}
		
		byte[] buf = {1,2,3};
		int length = buf.length;
		DatagramPacket p = new DatagramPacket(buf,length);
		try {
			socket.send(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail ("ioException encountered");
		}
	}
}
