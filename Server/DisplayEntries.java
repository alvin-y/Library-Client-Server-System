import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.util.List;

public class DisplayEntries extends JFrame{
	public static final int WIDTH = 600;
	public static final int HEIGHT = 400;
	
	public DisplayEntries(List<Node> nodes){
		setTitle("Search Results");
		setSize(WIDTH, HEIGHT);
		setLayout(new BorderLayout());
		
		//Make the display
		JPanel display = new JPanel(new FlowLayout());
		JTextArea area = new JTextArea(20,50);
		JScrollPane scroll = new JScrollPane(area); //make scrollable
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		area.setEditable(false);
		display.add(scroll);
		for(Node node: nodes) { //print out the books
			area.append("ISBN: " + node.isbn + "\nTitle: ");
			if(!(node.title.equals(" "))) {
				area.append(node.title);
			}
			area.append("\nAuthor: ");		
			if(!(node.auth.equals(" "))) {
				area.append(node.auth);
			}
			area.append("\nPublisher: ");
			if(!(node.pub.equals(" "))) {
				area.append(node.pub);
			}
			area.append("\nYear: ");
			if(node.year > 0) {
				area.append(Integer.toString(node.year));
			}
			area.append("\n");
			area.append("\n");
		}
		add(display, BorderLayout.CENTER); //display
	}
}
