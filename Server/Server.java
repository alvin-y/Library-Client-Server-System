import java.io.*;
import java.net.*;

public final class Server {
	public static void main(String argv[]) throws Exception { //where p is port
		int port = Integer.parseInt(argv[0]); 
		//System.out.println("Server running on port: " + port);
		ServerSocket server = null;
		
		try {
			server = new ServerSocket(port); //make new socket
		}catch(IOException e) {
			System.err.println(e);
		}	

		while(true) {
			Socket clientConnect = server.accept(); //accept clients
			
			Protocol request = new Protocol(clientConnect);
			
			Thread thread = new Thread(request);
			thread.start();
		}
	}
}
