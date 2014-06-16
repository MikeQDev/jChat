import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;


public class ChatClient {
	Socket sock;
	BufferedReader reader;
	PrintWriter writer;
	JTextArea chatBox;
	JTextField chatInput;
	SimpleDateFormat dF = new SimpleDateFormat("H:m:ss");
	Date d;
	
	public static void main(String[] args) {
		new ChatClient().startClient();
	}
	public void makeConnection(){
		final int PORT = 9001;
		try {
			sock = new Socket("127.0.0.1", PORT);
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			writer = new PrintWriter(sock.getOutputStream());
			new Thread(new ChatListener()).start();
			System.out.println("Connected to server & listening");
		} catch (ConnectException ex){
			JOptionPane.showMessageDialog(null,"Error connecting to server! Exiting program.","Error",JOptionPane.WARNING_MESSAGE);
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("CLOSING CONN");
	}
	public void startClient(){
		buildGUI();
		makeConnection();
	}
	private void buildGUI(){
		JFrame f = new JFrame("Chat client");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel userInput = new JPanel();
		
		chatInput = new JTextField(20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendListener());
		
		userInput.add(chatInput);
		userInput.add(sendButton);
		
		chatBox = new JTextArea(20,35);
		chatBox.setEditable(false);
		chatBox.setWrapStyleWord(true);
		chatBox.setLineWrap(true);
		
		JScrollPane chatScroller = new JScrollPane(chatBox);
		chatScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		chatScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		f.add(BorderLayout.SOUTH, userInput);
		f.add(chatScroller);
		
		f.pack();
		f.setVisible(true);
	}
	private class SendListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(!chatInput.getText().equals("")){
				if(chatInput.getText().length()>=200){
					JOptionPane.showMessageDialog(null,"Please limit your message to 200 or fewer characters.\n"
							+ "(Current length: "+chatInput.getText().length()+")");
				}else{
					writer.println(chatInput.getText());
					writer.flush();
					chatInput.setText("");
				}
			}
			chatInput.requestFocus();
		}
	}
	private class ChatListener implements Runnable{
		public void run(){
			String msgReceived;
			try {
				while((msgReceived = reader.readLine())!=null){
					d = new Date();
					System.out.println(dF.format(d)+" received "+msgReceived);
					chatBox.append("["+dF.format(d)+"] "+msgReceived+"\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
