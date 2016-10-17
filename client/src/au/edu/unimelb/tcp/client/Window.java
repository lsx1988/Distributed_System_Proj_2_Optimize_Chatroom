package au.edu.unimelb.tcp.client;

import java.io.IOException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;

public class Window extends JFrame {

	private JPanel contentPane;
	private JTextField serverIP;
	private JTextField port;
	private JTextField identity;
	private JButton connect;
	private JPasswordField password;
	private JTextField userName;
	private JTextArea textIn;
	private JTextArea textOut;
	private JButton quit;
	private JTextField currentRoom;
	private JComboBox roomList;
	private JScrollPane scrollDisplay;
	private JScrollPane scrollDisplay_2;
	private JScrollPane scrollDisplay_3;
	private JLabel lblCurrentRoom;
	private JButton refreshRoomList;
	private JSeparator separator_2;
	private JButton joinRoom;
	private JSeparator separator_3;
	private JTextArea whoOut;
	private JButton refreshWho;
	private JButton sendMessage;
	private JCheckBox isDebug;


	public static void main(String[] args) {
		
/**
* Set up the SSLSocket library		
*/
		//Location of the Java keystore file containing the collection of 
		//certificates trusted by this application (trust store).
		System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\liush\\Documents\\GitHub_Root\\DS_Proj_2_Optimize_Chatroom\\mykeystore");
		SSLSocketFactory sslsocketfactory=(SSLSocketFactory) SSLSocketFactory.getDefault();
		
		EventQueue.invokeLater(new Runnable() {
			//Run the GUI window
			public void run() {
				//Create window object and make it visible
				Window frame = new Window();
				frame.setVisible(true);
				
				//listen to the connect button
				frame.connect.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						try{
							/*when the connect button is clicked, read in the parameter
							 * including hostname,indenity, port,isDebug,username and password
							 */
							String hostname = frame.serverIP.getText();
							String identity = frame.identity.getText();
							int port = Integer.parseInt(frame.port.getText());
							boolean debug = frame.isDebug.isSelected();
							String userName = frame.userName.getText();
							String password = new String(frame.password.getPassword());
							
							//After get the above data, connect to the specific server
							SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(hostname, port);
							
							//Create a object containing identity and roomid information(null)
							State state = new State(identity, "",userName,password);
							
							// start sending thread
							MessageSendThread messageSendThread = new MessageSendThread(socket, state, debug, frame);
							Thread sendThread = new Thread(messageSendThread);
							sendThread.start();
							
							//start receiving thread
							Thread receiveThread = new Thread(new MessageReceiveThread(socket, state, messageSendThread, debug,frame));
							receiveThread.start();
							
						} catch (UnknownHostException e1) {
							System.out.println("Unknown host");
						} catch (IOException e1) {
							System.out.println("Communication Error: " + e1.getMessage());
						}
					}
				});													
			}
		}
	);
}

	/**
	 * Create the frame.
	 */
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 601, 451);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		serverIP = new JTextField();
		serverIP.setBounds(32, 11, 57, 20);
		contentPane.add(serverIP);
		serverIP.setColumns(10);
		
		port = new JTextField();
		port.setBounds(141, 11, 57, 20);
		port.setToolTipText("");
		port.setColumns(10);
		contentPane.add(port);
		
		identity = new JTextField();
		identity.setBounds(283, 11, 57, 20);
		identity.setColumns(10);
		contentPane.add(identity);
		
		connect = new JButton("Login");
		connect.setBounds(350, 10, 89, 23);
		contentPane.add(connect);
		
		password = new JPasswordField();
		password.setBounds(251, 49, 89, 20);
		contentPane.add(password);
		
		JLabel lblIP = new JLabel("IP:");
		lblIP.setBounds(10, 14, 24, 14);
		contentPane.add(lblIP);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(107, 14, 24, 14);
		contentPane.add(lblPort);
		
		JLabel lblNickname = new JLabel("Nickname:");
		lblNickname.setBounds(223, 14, 50, 14);
		contentPane.add(lblNickname);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(10, 52, 57, 14);
		contentPane.add(lblUsername);
		
		userName = new JTextField();
		userName.setBounds(77, 49, 89, 20);
		userName.setColumns(10);
		contentPane.add(userName);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(193, 52, 57, 14);
		contentPane.add(lblPassword);
		
		textOut = new JTextArea();
		textOut.setBounds(10, 90, 330, 211);
		textOut.setFont(new Font("Arial", Font.PLAIN, 12));
		textOut.setEditable(false);
		textOut.setWrapStyleWord(true);
		textOut.setLineWrap(true);
		scrollDisplay = new JScrollPane(textOut);
		scrollDisplay.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDisplay.setBounds(10, 90, 330, 211);
		contentPane.add(scrollDisplay);
		
		textIn = new JTextArea();
		textIn.setBounds(10, 325, 330, 80);
		textIn.setEditable(true);
		textIn.setFont(new Font("Arial", Font.PLAIN, 12));
		textIn.setWrapStyleWord(true);
		textIn.setLineWrap(true);
		scrollDisplay_2 = new JScrollPane(textIn);
		scrollDisplay_2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDisplay_2.setBounds(10, 325, 330, 80);
		contentPane.add(scrollDisplay_2);
		
		whoOut = new JTextArea();
		whoOut.setBounds(350, 216, 197, 85);
		whoOut.setFont(new Font("Arial", Font.PLAIN, 12));
		whoOut.setEditable(false);
		whoOut.setWrapStyleWord(true);
		whoOut.setLineWrap(true);
		scrollDisplay_3 = new JScrollPane(whoOut);
		scrollDisplay_3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDisplay_3.setBounds(350, 216, 197, 85);
		contentPane.add(scrollDisplay_3);
		
		//contentPane.add(textOut);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 77, 537, 2);
		contentPane.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 312, 330, 2);
		contentPane.add(separator_1);
		
		textIn = new JTextArea();
		textIn.setBounds(10, 325, 330, 80);
		contentPane.add(textIn);
		
		quit = new JButton("Quit");
		quit.setBounds(350, 48, 89, 23);
		contentPane.add(quit);
		
		roomList = new JComboBox();
		roomList.setBounds(438, 135, 109, 20);
		contentPane.add(roomList);
		
		currentRoom = new JTextField();
		currentRoom.setBounds(438, 91, 109, 20);
		contentPane.add(currentRoom);
		currentRoom.setColumns(10);
		
		lblCurrentRoom = new JLabel("Current Room:");
		lblCurrentRoom.setBounds(350, 94, 78, 14);
		contentPane.add(lblCurrentRoom);
		
		refreshRoomList = new JButton("Refresh");
		refreshRoomList.setBounds(350, 134, 78, 23);
		contentPane.add(refreshRoomList);
		
		separator_2 = new JSeparator();
		separator_2.setBounds(350, 122, 197, 3);
		contentPane.add(separator_2);
		
		joinRoom = new JButton("Join Room");
		joinRoom.setBounds(350, 168, 197, 23);
		contentPane.add(joinRoom);
		
		separator_3 = new JSeparator();
		separator_3.setBounds(350, 202, 197, 3);
		contentPane.add(separator_3);
			
		refreshWho = new JButton("Show all the members");
		refreshWho.setBounds(350, 312, 197, 23);
		contentPane.add(refreshWho);
		
		sendMessage = new JButton("Send Message");
		sendMessage.setBounds(350, 382, 197, 23);
		contentPane.add(sendMessage);
		
		isDebug = new JCheckBox("is Debug");
		isDebug.setBounds(450, 10, 97, 23);
		contentPane.add(isDebug);
	}
	
	public JTextArea getTextIn() {
		return textIn;
	}

	public JTextArea getTextOut() {
		return textOut;
	}
	
	public JButton getConnectButtton() {
		return connect;
	}
	
	public JButton getQuitButtton() {
		return quit;
	}
	
	public JTextField getCurrentRoom() {
		return currentRoom;	 
	}
	
	public JComboBox getRoomList() {
		return roomList;	 
	}
	
	public JButton getRefreshRoomButtton() {
		return refreshRoomList;
	}
}
