import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;


public class ChatServer {
	ArrayList<PrintWriter> clientOutList;
	JTextField inputField;
	final int SERVPORT = 9001;
	public static void main(String[] args) {
		new ChatServer().startServer();
	}
	public void startServer(){
		int userID = 0;
		clientOutList = new ArrayList<>();
		try {
			ServerSocket s = new ServerSocket(SERVPORT);
			System.out.println("Server started on port "+SERVPORT+"... Listening for connections.");
			buildGUI();
			while(true){
				Socket clSocket = s.accept();
				System.out.println(clSocket.getInetAddress()+":"+clSocket.getPort()+" connected as user"+userID);
				
				clientOutList.add(new PrintWriter(clSocket.getOutputStream()));
				
				Thread t = new Thread(new ClientHandler("user"+userID, clSocket));
				t.start();
				userID++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch(BindException ex){
			JOptionPane.showMessageDialog(null, "Unable to start server - port already in use?", "Could not start", JOptionPane.WARNING_MESSAGE);
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void buildGUI(){
		JFrame f = new JFrame("Server notifier");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new FlowLayout());
		inputField = new JTextField(20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ServerSendListener());
		f.add(inputField);
		f.add(sendButton);
		f.pack();
		f.setVisible(true);
	}

	private class ServerSendListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try{
				String msg = inputField.getText();
				if(msg.equals("!killServer")){
					killServer();
				}else if(msg.startsWith("!spamServer")){
					String[] spamMsg = msg.split("!spamServer");
					for(int i=0; i<100; i++){
						tellEveryoneFromServer(spamMsg[1]);
					}
				}else{
					tellEveryoneFromServer(msg);
					inputField.setText("");
					inputField.requestFocus();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	private void killServer(){
		tellEveryoneFromServer("Server has been terminated!\n"
				+ "Restart your client when the server is online to continue chatting.");
		System.exit(0);
	}
	private void tellEveryone(String msg){
		Iterator it = clientOutList.iterator();
		while(it.hasNext()){
			PrintWriter curClient = (PrintWriter) it.next();
			curClient.println(msg);
			curClient.flush();
		}
	}
	private void tellEveryoneFromServer(String msg){
		tellEveryone("--SERVER MESSAGE: "+msg+"--");
		System.out.println("Server message to everyone: "+inputField.getText());
	}
	private class ClientHandler implements Runnable{ 
		String clientName;
		Socket clientSock;
		BufferedReader reader;
		
		public void run(){//Waits for messages from clients & then sends to everyone
			String msgReceived;
			
			try {
				while((msgReceived = reader.readLine())!=null){
					System.out.println("Received "+msgReceived+" from "+clientName);
					tellEveryone(clientName+": "+msgReceived);
				}
			} catch (SocketException ex){
				System.out.println(clientName+" has disconnected.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public ClientHandler(String clName, Socket clSock){
			clientName = clName;
			try {
				clientSock = clSock;
				reader = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
				tellEveryoneFromServer(clName+" has connected");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
