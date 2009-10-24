import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * This class represents a DHCPTable.
 * @author DjLaivz
 *
 */
public class DHCPTable extends JTable {


	
  static Object[][] data = {
		    {"Mary", "Campione",
		     "Snowboarding", new Integer(5), new Boolean(false)},
		    {"Alison", "Huml",
		     "Rowing", new Integer(3), new Boolean(true)},
		    {"Kathy", "Walrath",
		     "Knitting", new Integer(2), new Boolean(false)},
		    {"Sharon", "Zakhour",
		     "Speed reading", new Integer(20), new Boolean(true)},
		    {"Philip", "Milne",
		     "Pool", new Integer(10), new Boolean(false)}
		};
	

	ArrayList<Object[]> list = new ArrayList<Object[]>();
	

	



	
	public DHCPTable() {
		super();
		init();
	}
	
	public DHCPTable(Object[][] data, Object[] columnNames) {
		super(data,columnNames);
		init();
	}
	


	public DHCPTable(TableModel dataModel) {
		super(dataModel);
		init();
	}
	
	private void init() {
		JFrame frame = new JFrame("DHCP Table");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		//JTable table = new JTable(dataModel);
		JScrollPane scrollPane = new JScrollPane(this);
		this.setFillsViewportHeight(true);
		
		frame.add(scrollPane);
		//frame.add(table);
		
		//Display the window.
        frame.pack();
        frame.setVisible(true);
		
	}
	
}
