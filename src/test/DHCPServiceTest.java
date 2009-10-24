package test;
import DHCPService.DHCPClient;
import DHCPService.DHCPMessage;
import DHCPService.DHCPOptions;
import DHCPService.DHCPServer;
import junit.framework.TestCase;


public class DHCPServiceTest extends TestCase {

	public DHCPServiceTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void test() {
		DHCPServer s = new DHCPServer();
		DHCPClient c = new DHCPClient();
		DHCPMessage m = new DHCPMessage();
		DHCPOptions o = new DHCPOptions();
		assertTrue(s != null);
		assertTrue(c != null);
		assertTrue(m != null);
		assertTrue(o != null);
	}
	
	public void discover() {
		DHCPServer s = new DHCPServer();
		
		
	}
}
