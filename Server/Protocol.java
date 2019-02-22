import java.io.* ;
import java.net.* ;
import java.util.* ;
import javax.swing.JOptionPane;

public class Protocol implements Runnable {
    Socket socket;
    public static List<Node> nodeList = Collections.synchronizedList(new ArrayList<Node>()); //make synchronized list
    public String confirm;
    
    // Constructor
    public Protocol(Socket socket) throws Exception {
		this.socket = socket;
    }
    
    // Implement the run() method of the Runnable interface.
    public void run() {
		try {
		    processRequest();
		} catch (Exception e) {
		    System.out.println(e);
		}
    }

    private void processRequest() throws Exception {    	
    	//Using scanner
    	Scanner read = new Scanner(socket.getInputStream());
    	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		  	
		String line;
		while(true) {
			line = read.nextLine(); //get next line		
			
//toServer layout: isbn | title | author | year | publisher
			String[] inputs = line.split("\\|");
			
			//parse all inputs
			String c_isbn = "";
			inputs[1] = inputs[1].replaceAll("[^0-9]", ""); //remove other characters
			if(inputs[1].equals("")) { //if empty
				inputs[1] = " ";
			}
			if(!(isbnValid(inputs[1])) && (line.startsWith("S") || line.startsWith("U"))) {
				line = "Invalid";
			}else {
				c_isbn = inputs[1];
			}
			String c_title = inputs[2];
			String c_auth = inputs[3];
			String c_pub = inputs[5];
			int c_year;
			try {
				c_year = Integer.parseInt(inputs[4]);
			}catch(NumberFormatException e) {
				//System.err.println("Bad year");
				c_year = -1;
			}
			
			//Process the message
			if(line.startsWith("N")) { //for no action
				out.println("Message without action sent.");
			}else if(line.equals("Invalid")) { //If isbn is invalid don't do anything
				out.println("Invalid ISBN, request failed");
			}else if((line.startsWith("S"))) { //submit code
				if (listContains(inputs[1])){ //if exists then don't do anything
					out.println("Book already exists, failed");
				}else {
					out.println("Successful submission");
					Node temp = new Node(c_isbn, c_year, c_title, c_pub, c_auth); //add to list
					nodeList.add(temp);
				}
			}else if (line.startsWith("U")) { //update code
				if(listContains(c_isbn)) { //if it exists in the arraylist
					Node node = returnNode(c_isbn); //get the node
					if(c_year != -1) { 
						node.year = c_year;
					}
					if(c_title != " ") {
						node.title = c_title;
					}
					if(c_pub != " ") {
						node.pub = c_pub;
					}
					if(c_auth != " ") {
						node.pub = c_auth;
					}					
					out.println("Update successful");
				}else {
					out.println("ISBN not found, update failed");
				}
			}else if (line.startsWith("G")) { //get results
				//System.out.println("Get");
				if (nodeList.isEmpty()) {
					out.println("Can't get from empty list");
				}else {
					int[] check = new int[5]; //set checking flags
					if(c_isbn.equals(" ")) {
						check[0] = 0;
					}else {
						check[0] = 1;
					}
					
					if(c_title.equals(" ")) {
						check[1] = 0;
					}else {
						check[1] = 1;
					}
					
					if(c_auth.equals(" ")){
						check[2] = 0;
					}else {
						check[2] = 1;
					}
					
					if(c_pub.equals(" ")){
						check[3] = 0;
					}else {
						check[3] = 1;
					}
					
					if(c_year == -1) {
						check[4] = 0;
					}else {
						check[4] = 1;
					}
							
					List<Node> temp = new ArrayList<Node>();
					boolean count = false;
					if(c_title.equals("ALL")) { //if get all
						for(Node node: nodeList) {
							temp.add(node);
						}
						count = true;
					}else {						//otherwise get given criteria
						for(Node node: nodeList){
							if(getCheck(node, check, c_isbn, c_title, c_auth, c_pub, c_year)) {
								temp.add(node);
								count = true;
							}
						}
					}
					if(count) { //if found
						DisplayEntries display = new DisplayEntries(temp);
						display.setVisible(true);
						out.println("Successfully displayed");
					}else { //if none
						out.println("No matching entries");
					}
					temp = null; //free
				}
			}else if (line.startsWith("R")) { //remove code
				if(c_title.equals("ALL")) { //if delete all
					if(c_title.equals("ALL") && (JOptionPane.showConfirmDialog(null, 
			                "Are you sure to delete all?", "Confirm Wipe", 
			                JOptionPane.YES_NO_OPTION,
			                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)) { //get confirm
						for(Node n: nodeList) {
							nodeList.remove(n);
						}
					}
					out.println("Deleted Everything!");
				}else{ //if only delte given criteria
					List<Node> temp = new ArrayList<Node>();
					boolean found = false;
					for(Node node: nodeList) {
						if(delCheck(node, c_isbn, c_title, c_auth, c_pub, c_year)) {
							temp.add(node);
							found = true;
						}
					}
					if(found) { //if found stuff to delete
						if (JOptionPane.showConfirmDialog(null, 
				                "Are you sure to delete?", "Confirm Delete", 
				                JOptionPane.YES_NO_OPTION,
				                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){ //confirm
				        	for(Node n: temp) {
				        		nodeList.remove(n);
				        	}
				        	out.println("Removal successful");
				        }else {
				        	out.println("Cancelled Delete");
				        }
					}else { //none found
						out.println("Nothing to delete");
					}
					temp = null;
				}
			}else { //shouldn't happen
				out.println("Something broke.");
				break;
			}
		}
		
		out.println("Close Server");
        // Close streams and socket.
        out.close();
        read.close();
        socket.close();
    }
    
    private boolean getCheck(Node n, int[] check, String c_isbn, String c_title, String c_auth, String c_pub, int c_year) {
    	//Search through each and check flag & if fills criteria
    	if(check[0] == 1 && !(n.isbn.equals(c_isbn))) {
    		return false;
    	}
    	if(check[1] == 1 && !(n.title.equals(c_title))) {
    		return false;
    	}
    	if(check[2] == 1 && !(n.auth.equals(c_auth))){
    		return false;
    	}
    	if(check[3] == 1 && !(n.pub.equals(c_pub))) {
    		return false;
    	}
    	if(check[4] == 1 && (n.year != c_year)){
    		return false;
    	}
    	
    	return true;
    }
    
    private boolean delCheck(Node n, String c_isbn, String c_title, String c_auth, String c_pub, int c_year) {
    	//if it matches any category, delete
    	if(n.isbn.equals(c_isbn)) {
    		return true;
    	}
    	if(n.title.equals(c_title)) {
    		return true;
    	}
    	if(n.auth.equals(c_auth)) {
    		return true;
    	}
    	if(n.pub.equals(c_pub)) {
    		return true;
    	}
    	if(n.year == c_year) {
    		return true;
    	}
    	return false;
    }
    
    private boolean listContains(String key) {
    	//check if list contains a isbn
    	boolean contains = false;
    	for (Node node: nodeList) {
    		if(node.isbn.equals(key)) {
    			contains = true;
    		}
    	}
    	return contains;
    }
    
    private boolean isbn10Valid(String isbn) {
    	//checks validity of 10 digit isbn
    	boolean valid = false;
    	
    	 try{
             int total = 0;
             for (int i = 0; i < 9; i++){
                 int digit = Integer.parseInt(isbn.substring(i,i+1));
                 total += ((10 - i) * digit);
             }

             String check = Integer.toString((11-(total%11))%11);
             if ("10".equals(check)){
                 check = "X";
             }

             return check.equals( isbn.substring( 9 ) );
         }catch ( NumberFormatException f){
             return false;
         }
    }

    
    private boolean isbnValid(String isbn) {
    	//check validity of 13 digit isbn
    	boolean valid = false;
    	
    	if(isbn.length() == 10) {
    		return isbn10Valid(isbn);
    	}
    	
    	if(isbn.length() != 13) { //if not 13 digits false
    		return false;
    	}
    	
    	
    	
    	try {
    		int total = 0;
    		for(int i = 0; i < 12; i++) { //for each character
    			int digit = Integer.parseInt(isbn.substring(i, i+1));
    			if(i % 2 == 0) {
    				total += digit;
    			}else {
    				total += digit * 3;
    			} 
    		}
    		
    		int check = 10 - (total % 10);
    		if(check == 10) {
    			check = 0;
    		}
    		
    		if(check == Integer.parseInt(isbn.substring(12))) {
    			valid = true;
    		}
    	}catch(NumberFormatException e) {
    		System.err.println("Bad isbn");
    		return false;
    	}
    	return valid;
    }
    
    private Node returnNode(String isbn) {
    	//returns a node with given isbn
    	for(Node node: nodeList) {
    		if (node.isbn.equals(isbn)) {
    			return node;
    		}
    	}
    	return null;
    }
}
