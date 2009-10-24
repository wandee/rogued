
import java.util.Hashtable;
import java.util.LinkedList;



/**
 * This class represents a hash table of options for a DHCP message. 
 * Its purpose is to ease option handling such as add, remove, or change.
 * @author Laivz
 *
 */
public class DHCPOptions {
	//DHCP Message Types
	public static final int DHCPDISCOVER = 1;
	public static final int DHCPOFFER = 2;
	public static final int DHCPREQUEST = 3;
	public static final int DHCPDECLINE = 4;
	public static final int DHCPACK = 5;
	public static final int DHCPNAK = 6;
	public static final int DHCPRELEASE = 7;
	
	//DHCP Option Identifiers
	public static final int DHCPMESSAGETYPE = 53;
	
	
	//private LinkedList<byte[]> options = new LinkedList<byte[]>();
	private Hashtable<Integer,byte[]> options;
	
	public DHCPOptions() {
		 options = new Hashtable<Integer, byte[]>();
	}
	
	public byte[] getOption(int optionID) {
		return options.get(optionID);
	}
	
	public void setOption(int optionID, byte[] option) {
		options.put(optionID, option);
	}
	
	public byte[] getOptionData(int optionID) {
		byte[] option = options.get(optionID);
		byte[] optionData = new byte[option.length-2];
		for (int i=0; i < optionData.length; i++)  optionData[i] = option[2+i];
		return optionData;
	}
	
	public void setOptionData(int optionID, byte[] optionData) {
		byte[] option = new byte[2+optionData.length];
		option[0] = (byte) optionID;
		option[1] = (byte) optionData.length;
		for (int i=0; i < optionData.length; i++) option[2+i] = optionData[i];
		options.put(optionID, option);
	}
	public void printOption (int optionID) {
		String output = new String("");
		if (options.get(optionID) != null) {
			byte[] option = options.get(optionID);
			for (int i=0; i < option.length; i++) {
				output += option[i]  +
						(i == option.length-1 ? "" : ","); 
			}
		} else {
			output = "<Empty>";
		}
		System.out.println(output);
	}
	
	public void printOptions () {
		for (byte[] option : options.values()) {
			printOption(option[0]);
		}
	}
	
	

	public static void main (String[] args) {
		DHCPOptions test = new DHCPOptions();
	
		//test.printOptions();
	}

	public byte[] externalize() {
		
		//get size
		int totalBytes = 0;
		for (byte[] option : this.options.values()) {
			totalBytes += option.length;
		}
		
		byte[] options = new byte[totalBytes];
		
		//copy bytes
		int bytes = 0;
		for (byte[] option : this.options.values()) {
			for (int i=0; i < option.length; i++) {
				options[bytes+i] = option[i];
			}
			bytes += option.length;
		}
		return options;
	}
	
}
