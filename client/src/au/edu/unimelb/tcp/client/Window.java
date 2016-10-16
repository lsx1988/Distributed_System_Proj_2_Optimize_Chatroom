package au.edu.unimelb.tcp.client;

import java.io.IOException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.awt.BorderLayout;
import java.awt.EventQueue;
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

public class Window extends JFrame {

	private JPanel contentPane;
	private JTextField serverIP;
	private JTextField port;
	private JTextField identity;
	private JButton Connect;
	private JCheckBox isDebug;
	private JPasswordField password;
	private JTextField userName;

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
				frame.Connect.addActionListener(new ActionListener(){
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
							MessageSendThread messageSendThread = new MessageSendThread(socket, state, debug);
							Thread sendThread = new Thread(messageSendThread);
							sendThread.start();
							
							//start receiving thread
							Thread receiveThread = new Thread(new MessageReceiveThread(socket, state, messageSendThread, debug));
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
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		serverIP = new JTextField();
		serverIP.setBounds(32, 11, 57, 20);
		contentPane.add(serverIP);
		serverIP.setColumns(10);
		
		port = new JTextField();
		port.setToolTipText("");
		port.setColumns(10);
		port.setBounds(128, 11, 57, 20);
		contentPane.add(port);
		
		identity = new JTextField();
		identity.setColumns(10);
		identity.setBounds(255, 11, 57, 20);
		contentPane.add(identity);
		
		Connect = new JButton("Connect");
		Connect.setBounds(335, 10, 89, 23);
		contentPane.add(Connect);
		
		isDebug = new JCheckBox("Debug");
		isDebug.setBounds(278, 41, 57, 23);
		contentPane.add(isDebug);
		
		password = new JPasswordField();
		password.setText("sdfsdf");
		password.setBounds(205, 42, 57, 20);
		contentPane.add(password);
		
		JLabel lblIP = new JLabel("IP:");
		lblIP.setBounds(10, 14, 24, 14);
		contentPane.add(lblIP);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(99, 14, 24, 14);
		contentPane.add(lblPort);
		
		JLabel lblNickname = new JLabel("Nickname:");
		lblNickname.setBounds(195, 14, 50, 14);
		contentPane.add(lblNickname);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(10, 44, 57, 14);
		contentPane.add(lblUsername);
		
		userName = new JTextField();
		userName.setColumns(10);
		userName.setBounds(77, 42, 57, 20);
		contentPane.add(userName);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(144, 44, 57, 14);
		contentPane.add(lblPassword);
	}
}
