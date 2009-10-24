
import java.util.Arrays;
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
	public static final int DHCPSUBNETMASK = 1;
	public static final int DHCPROUTER = 3;
	public static final int DHCPREQUESTIP = 50;
	public static final int DHCPMESSAGETYPE = 53;
	public static final int DHCPSERVERIDENTIFIER = 54;
	public static final int DHCPPARAMREQLIST = 55;
	public static final int DHCPIPADDRLEASETIME = 51;
	public static final int DHCPCLIENTIDENTIFIER = 61;
	

	private static final int MAX_OPTION_SIZE = 320;
	
	//private LinkedList<byte[]> options = new LinkedList<byte[]>();
	private Hashtable<Integer,byte[]> options;
	
	public DHCPOptions() {
		 options = new Hashtable<Integer, byte[]>();
	}
	
	public DHCPOptions(byte[] options) {
		this.options = new Hashtable<Integer, byte[]>();
		this.internalize(options);
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
	
	public String toString() {
		String output = new String("");
		for (byte[] option : options.values()) {
			output += "optionID: " + option[0] + " optionLength: " + option[1] + " optionData: ";
			for (int i = 2; i < option.length; i++) {
				output += option[i] + (i == option.length - 1 ? "\n" : ", ");
			}
		}
		return output;
	}
	
	

	public static void main (String[] args) {
		DHCPOptions test = new DHCPOptions();
	
		//test.printOptions();
	}
	
	public void internalize(byte[] options) {
		//clear hash
		this.options.clear();
		
		//trim options
		options = trim(options);
		
		//get size
		int totalBytes = options.length;
		assert (totalBytes >= 4 && totalBytes <= MAX_OPTION_SIZE);
		
		//read magic cookie
		byte[] cookie = new byte[4];
		cookie[0] = options[0];
		cookie[1] = options[1];
		cookie[2] = options[2];
		cookie[3] = options[3];
		String magicCookie = new String(cookie[0] + 
								  "." + cookie[1] +
						     	  "." + cookie[2] +
							      "." + cookie[3]);
		System.out.println("cookie received: " + magicCookie);
		assert(magicCookie == "99.130.83.99");
		
		//copy bytes
		int bytes = 0;
		for (int i=4; i < totalBytes; i += bytes) {
			for (int p=0; p< 99999999; p++ );
			System.out.println("debug test1: iteration = " + i + " bytes = " + bytes + " totalBytes = " +totalBytes );
			bytes = 1;
			
			//if option
			if (options[i] != (byte) 0 && options[i] != (byte) 255) {
				//length of the option specified in length field
				int optionLength = options[i+1];
				
				//new byte array of size in length field
				byte[] option = new byte[optionLength];
				
				//debug
				assert(optionLength >= 1); System.out.println("option lengh: " + optionLength);
				
				//for each option data byte
				for (int j=0; j < optionLength; j++) {
					//set data
					option[j] = options[i+2+j];
				}
				bytes = 2 + optionLength; //opcode(1b) + length(1b) + data.length
				//set option data in this dhcpOptions object 
				this.setOptionData(options[i], option);
				
			} else if (options[i] == (byte) 255) { //end options
				assert(i == totalBytes-1);
			} else if (options[i] == (byte) 0) { //padding
				//keep reading if padding op code
				System.out.println("padding op code");
			} else {
				System.out.println("malformed option, code out of range (0-255). Code: " + options[i]);
			}
		}
		
		System.out.println("internalization complete: " + this.options.size() + " options entered");
	}

	//returns option byte array with removed trailing 0's 
	private byte[] trim (byte[] options) {
		int i;
		for (i=options.length-1; options[i] ==  0; i--);
		assert(options[i] == 255);
		int trimedLength = i+1;
		byte[] trimed = Arrays.copyOf(options, trimedLength);
		return trimed;
	}

	public byte[] externalize() {
		
		//get size
		int totalBytes = 0;
		for (byte[] option : this.options.values()) {
			totalBytes += option.length;
		}
		
		//account for magic cookie and end option code
		totalBytes += (4 + 1); //4-magic cookie + 1-end options code
		
		byte[] options = new byte[totalBytes];
		
		//add magic cookie
		options[0] = (byte) 99;
		options[1] = (byte) 130;
		options[2] = (byte) 83;
		options[3] = (byte) 99;
		
		//copy bytes
		int bytes = 4;
		for (byte[] option : this.options.values()) {
			for (int i=0; i < option.length; i++) {
				options[bytes+i] = option[i];
			}
			bytes += option.length;
		}
		
		//add end options code
		options[bytes] = (byte) 255;

		return options;
	}
}
