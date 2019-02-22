//imports used
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

import java.io.*;
import java.util.Scanner;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends JFrame implements ActionListener{
	public static final int WIDTH = 400;
	public static final int HEIGHT = 300;
	
	private JButton connectButton;
	
	private JTextArea messageBox;
	
	private String toServer = "N|";
	
	private JTextField addrField;
	private JTextField portField;
	private JTextField isbnField;
	private JTextField titleField;
	private JTextField authField;
	private JTextField yearField;
	private JTextField pubField;
	
	Socket connectionSocket = null;
	PrintWriter out = null;
	Scanner in = null;
	
	
	public static void main(String[] args){
		//make new calculator object
		Client aClient = new Client();
		//cannot manually resize
		aClient.setResizable(false);
		//display
		aClient.setVisible(true);
	}
	
	public Client() {
		setTitle("Bibliography Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WIDTH, HEIGHT);
		setLayout(new BorderLayout());
		
		//Address Port Area
		JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new GridLayout(2,1));
		
		JLabel addrLabel = new JLabel("Address: ", JLabel.CENTER); //address input
		addressPanel.add(addrLabel);
		addrField = new JTextField("127.0.0.1", 30);
		addrLabel.setLabelFor(addrField);
		addressPanel.add(addrField);

		JLabel portLabel = new JLabel("Port: ", JLabel.CENTER); //port input
		addressPanel.add(portLabel);
		portField = new JTextField("4444", 30);
		portLabel.setLabelFor(portField);
		addressPanel.add(portField);
				
		add(addressPanel, BorderLayout.NORTH); //add to gui
		
		//Labels
		JPanel labelPanel = new JPanel(new GridLayout(5,1));
				
		//Information Input Spots
		JPanel infoPanel = new JPanel(new GridLayout(5,1));

		JLabel isbnLabel = new JLabel("ISBN: ", JLabel.CENTER); //isbn label
		labelPanel.add(isbnLabel);
		isbnField = new JTextField(30);
		isbnLabel.setLabelFor(isbnField);
		infoPanel.add(isbnField);
		
		JLabel titleLabel = new JLabel("Title: ", JLabel.CENTER); //title label
		labelPanel.add(titleLabel);
		titleField = new JTextField(30);
		titleLabel.setLabelFor(titleField);
		infoPanel.add(titleField);
		
		JLabel authLabel = new JLabel("Author: ", JLabel.CENTER); //author label
		labelPanel.add(authLabel);
		authField = new JTextField(30);
		authLabel.setLabelFor(authField);
		infoPanel.add(authField);	
		
		JLabel yearLabel = new JLabel("Year: ", JLabel.CENTER); //year label
		labelPanel.add(yearLabel);
		yearField = new JTextField(30);
		yearLabel.setLabelFor(yearField);
		infoPanel.add(yearField);		
		
		JLabel pubLabel = new JLabel("Publisher: ", JLabel.CENTER); //publisher label
		labelPanel.add(pubLabel);
		pubField = new JTextField(30);
		pubLabel.setLabelFor(pubField);
		infoPanel.add(pubField);
		
		//add both panels to the gui
		add(labelPanel, BorderLayout.WEST);
		add(infoPanel, BorderLayout.CENTER);
		
		//Buttons
		JPanel buttonPanel = new JPanel( );
		buttonPanel.setLayout(new GridLayout(6,1)); 
		
		//connect button
		connectButton = new JButton("Connect");
		connectButton.addActionListener(this);
		buttonPanel.add(connectButton);
		
		//submit button
		JButton submitButton = new JButton("Submit"); 
		submitButton.addActionListener(this);
		buttonPanel.add(submitButton);

		//update button
		JButton updateButton = new JButton("Update"); 
		updateButton.addActionListener(this);
		buttonPanel.add(updateButton);

		// get button
		JButton getButton = new JButton("Get");
		getButton.addActionListener(this);
		buttonPanel.add(getButton);

		// remove button
		JButton delButton = new JButton("Remove");
		delButton.addActionListener(this);
		buttonPanel.add(delButton);
		
		//send button
		JButton sendButton = new JButton("Send Request");
		sendButton.addActionListener(this);
		buttonPanel.add(sendButton);
		
		add(buttonPanel, BorderLayout.EAST);
		
		//Return Message
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new FlowLayout());
		messageBox = new JTextArea(1,25);
		messageBox.setBackground(Color.WHITE);
		messageBox.setEditable(false);
		outputPanel.add(messageBox);
		add(outputPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			processAction(e);
		}catch(NullPointerException f) {
			System.err.println(f);
		}catch(buttonExistException e2) {
			System.err.println(e2);
		}catch(IOException e3) {
			System.err.println(e3);
		}
	}
	
	public void processAction(ActionEvent e) throws buttonExistException, IOException{
		String actionCommand = e.getActionCommand();
		if(actionCommand.equals("Connect")) { //connect to server
			connectServer();
			connectButton.setText("Disconnect");
		}
		else if(actionCommand.equals("Disconnect")) { //disconnect
			disconnectServer();
			connectButton.setText("Connect");
		}
		
		//toServer layout: isbn | title | author | year | publisher
		//build server messages
		else if(actionCommand.equals("Submit")) {
			messageBox.setText("Submission request");
			toServer = "S|";
		}
		else if(actionCommand.equals("Update")) {
			messageBox.setText("Update request");
			toServer = "U|";
		} 
		else if(actionCommand.equals("Get")){
			messageBox.setText("Get request | Put ALL in title for ALL Books");
			toServer = "G|";
		}
		else if(actionCommand.equals("Remove")) {
			messageBox.setText("Remove request | Put ALL in title for ALL Books");
			toServer = "R|";
		}
		else if(actionCommand.equals("Send Request")) {
			//System.out.println(toServer);
			//create message
			completeMessage();
			//messageBox.setText("Sent your request!");
		
			out.println(toServer); //send message
			
			//reset stuff
			toServer = "N|";
			isbnField.setText("");
			titleField.setText("");
			authField.setText("");
			pubField.setText("");
			yearField.setText("");
			messageBox.setText(in.nextLine()); //receive message
		}
		else { //Should never have this
			throw new buttonExistException("Button doesn't Exist");
		}
		//For testing purposes
		//messageBox.setText(toServer);
	}
	
	public void connectServer() throws IOException{
		try { //try to connect
			connectionSocket = new Socket(addrField.getText(), Integer.parseInt((portField.getText())));
			out = new PrintWriter(connectionSocket.getOutputStream(), true);
			in = new Scanner(connectionSocket.getInputStream());
			messageBox.setText("Connected!");
		}catch(UnknownHostException e) {
			messageBox.setText("This host doesn't exist.");
		}catch(IOException e) {
			messageBox.setText("Invalid address/port");
		}
	}
	
	public void disconnectServer() throws IOException {
		//disconnect
		messageBox.setText("Disconnected");
		out.close();
		in.close();	
		connectionSocket.close();
	}
	
	private void completeMessage() {
		if(isbnField.getText().equals("")){
			toServer += " |";
		}else {
			toServer += isbnField.getText() + "|";
		}
		
		if(titleField.getText().equals("")){
			toServer += " |";
		}else {
			toServer += titleField.getText() + "|";
		}
		
		if(authField.getText().equals("")){
			toServer += " |";
		}else {
			toServer += authField.getText() + "|";
		}
		
		if(yearField.getText().equals("")){
			toServer += "-1|";
		}else {
			toServer += yearField.getText() + "|";
		}
		
		if(pubField.getText().length() == 0){
			toServer += " |";
		}else {
			toServer += pubField.getText() + "|";
		}
	}
	
	public void windowClosing(WindowEvent e) {
		try {
			if(connectButton.getText().equals("Disconnect")) {
				out.close();
				in.close();
				connectionSocket.close();
			}else {
				System.exit(1);
			}
		}catch(Exception er) {
			System.err.println(er);
		}
	}
}
