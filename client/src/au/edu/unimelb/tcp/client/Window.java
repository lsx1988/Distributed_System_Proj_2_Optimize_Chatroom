package au.edu.unimelb.tcp.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
	private JTextField textarea_serverIP;
	private JTextField textarea_port;
	private JTextField textarea_identity;
	private JButton button_connect;
	private JPasswordField textarea_password;
	private JTextField textarea_userName;
	private JTextArea textarea_type;
	private JTextArea textarea_display;
	private JButton button_quit;
	private JTextField textarea_currentRoom;
	private JComboBox textlist_roomList;
	private JScrollPane scrollDisplay;
	private JScrollPane scrollDisplay_2;
	private JScrollPane scrollDisplay_3;
	private JLabel lblCurrentRoom;
	private JButton button_refreshRoomList;
	private JButton button_joinRoom;
	private JSeparator separator_3;
	private JTextArea textarea_whoOut;
	private JButton button_refreshWho;
	private JButton button_sendMessage;
	private JCheckBox isDebug;
	private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
	private SSLSocketFactory sslsocketfactory;
	private static Window temp_frame;
	private JButton btnCreate;
	private JTextField textarea_newRoom;
	private JButton btnDelete;


	public static void main(String[] args) {
		
		//设置ssl协议
		System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\liush\\Documents\\GitHub_Root\\DS_Proj_2_Optimize_Chatroom\\mykeystore");

		//运行主面板
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window frame = new Window();
					frame.setVisible(true);
					temp_frame = frame;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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
		
		textarea_serverIP = new JTextField();
		textarea_serverIP.setBounds(32, 11, 57, 20);
		contentPane.add(textarea_serverIP);
		textarea_serverIP.setColumns(10);
		
		textarea_port = new JTextField();
		textarea_port.setBounds(141, 11, 57, 20);
		textarea_port.setToolTipText("");
		textarea_port.setColumns(10);
		contentPane.add(textarea_port);
		
		textarea_identity = new JTextField();
		textarea_identity.setBounds(283, 11, 57, 20);
		textarea_identity.setColumns(10);
		contentPane.add(textarea_identity);
		
		button_connect = new JButton("Login");
		button_connect.setBounds(350, 10, 89, 23);
		contentPane.add(button_connect);
		//when the connect botton is clicked
		button_connect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				String hostname = textarea_serverIP.getText();
				String identity = textarea_identity.getText();
				int port = Integer.parseInt(textarea_port.getText());
				boolean debug = isDebug.isSelected();
				String userName = textarea_userName.getText();
				String password = new String(textarea_password.getPassword());
				State state = new State(identity, "",userName,password);
							
				try {
					
					//After get the above data, connect to the specific server
					sslsocketfactory=(SSLSocketFactory) SSLSocketFactory.getDefault();
					SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(hostname, port);
					
					// start sending thread
					MessageSendThread messageSendThread = new MessageSendThread(socket, state, debug, temp_frame,messageQueue);
					Thread sendThread = new Thread(messageSendThread);
					sendThread.start();
					
					//start receiving thread
					Thread receiveThread = new Thread(new MessageReceiveThread(socket, state, messageSendThread, debug,temp_frame,messageQueue));
					receiveThread.start();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			}
		});
		
		textarea_password = new JPasswordField();
		textarea_password.setBounds(251, 49, 89, 20);
		contentPane.add(textarea_password);
		
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
		
		textarea_userName = new JTextField();
		textarea_userName.setBounds(77, 49, 89, 20);
		textarea_userName.setColumns(10);
		contentPane.add(textarea_userName);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(193, 52, 57, 14);
		contentPane.add(lblPassword);
		
		textarea_display = new JTextArea();
		textarea_display.setBounds(10, 90, 330, 211);
		textarea_display.setFont(new Font("Arial", Font.PLAIN, 12));
		textarea_display.setEditable(false);
		textarea_display.setWrapStyleWord(true);
		textarea_display.setLineWrap(true);
		scrollDisplay = new JScrollPane(textarea_display);
		scrollDisplay.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDisplay.setBounds(10, 90, 330, 211);
		contentPane.add(scrollDisplay);
		
		textarea_type = new JTextArea();
		textarea_type.setBounds(10, 325, 330, 80);
		textarea_type.setEditable(true);
		textarea_type.setFont(new Font("Arial", Font.PLAIN, 12));
		textarea_type.setWrapStyleWord(true);
		textarea_type.setLineWrap(true);
		scrollDisplay_2 = new JScrollPane(textarea_type);
		scrollDisplay_2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDisplay_2.setBounds(10, 325, 330, 80);
		contentPane.add(scrollDisplay_2);
		
		textarea_whoOut = new JTextArea();
		textarea_whoOut.setBounds(350, 216, 197, 85);
		textarea_whoOut.setFont(new Font("Arial", Font.PLAIN, 12));
		textarea_whoOut.setEditable(false);
		textarea_whoOut.setWrapStyleWord(true);
		textarea_whoOut.setLineWrap(true);
		scrollDisplay_3 = new JScrollPane(textarea_whoOut);
		scrollDisplay_3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDisplay_3.setBounds(350, 252, 197, 85);
		contentPane.add(scrollDisplay_3);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 77, 537, 2);
		contentPane.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 312, 330, 2);
		contentPane.add(separator_1);
		
		button_quit = new JButton("Quit");
		button_quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageQueue.add("#quit");
				button_connect.setEnabled(true);
			}
		});
		button_quit.setBounds(350, 48, 89, 23);
		contentPane.add(button_quit);
		
		textlist_roomList = new JComboBox();
		textlist_roomList.setBounds(438, 185, 109, 20);
		contentPane.add(textlist_roomList);
		
		textarea_currentRoom = new JTextField();
		textarea_currentRoom.setBounds(438, 91, 109, 20);
		contentPane.add(textarea_currentRoom);
		textarea_currentRoom.setColumns(10);
		
		lblCurrentRoom = new JLabel("Current Room:");
		lblCurrentRoom.setBounds(350, 94, 78, 14);
		contentPane.add(lblCurrentRoom);
		
		button_refreshRoomList = new JButton("Refresh");
		button_refreshRoomList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textlist_roomList.removeAllItems();
				messageQueue.add("#list");
			}
		});
		button_refreshRoomList.setBounds(350, 184, 78, 23);
		contentPane.add(button_refreshRoomList);
		
		button_joinRoom = new JButton("Join Room");
		button_joinRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String roomid = (String)textlist_roomList.getSelectedItem();
				messageQueue.add("#joinroom" + " " + roomid);
			}
		});
		button_joinRoom.setBounds(350, 218, 197, 23);
		contentPane.add(button_joinRoom);
		
		separator_3 = new JSeparator();
		separator_3.setBounds(350, 238, 197, 3);
		contentPane.add(separator_3);
			
		button_refreshWho = new JButton("Show all the members");
		button_refreshWho.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textarea_whoOut.setText("");
				messageQueue.add("#who");
			}
		});
		button_refreshWho.setBounds(350, 348, 197, 23);
		contentPane.add(button_refreshWho);
		
		button_sendMessage = new JButton("Send Message");
		button_sendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = textarea_type.getText();
				textarea_type.setText("");
				textarea_display.append("Me:"+msg+"\r\n");
				messageQueue.add(msg);
			}
		});
		button_sendMessage.setBounds(350, 382, 197, 23);
		contentPane.add(button_sendMessage);
		
		isDebug = new JCheckBox("is Debug");
		isDebug.setBounds(450, 10, 97, 23);
		contentPane.add(isDebug);
		
		btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String new_roomid = textarea_newRoom.getText();
				messageQueue.add("#createroom " + new_roomid);
			}
		});
		btnCreate.setBounds(350, 119, 78, 23);
		contentPane.add(btnCreate);
		
		textarea_newRoom = new JTextField();
		textarea_newRoom.setColumns(10);
		textarea_newRoom.setBounds(438, 119, 109, 20);
		contentPane.add(textarea_newRoom);
		
		btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String roomid = textarea_currentRoom.getText();
				messageQueue.add("#deleteroom "+roomid);
			}
		});
		btnDelete.setBounds(350, 150, 197, 23);
		contentPane.add(btnDelete);
	}


	public JPanel getContentPane() {
		return contentPane;
	}


	public JTextField getTextarea_serverIP() {
		return textarea_serverIP;
	}


	public JTextField getTextarea_port() {
		return textarea_port;
	}


	public JTextField getTextarea_identity() {
		return textarea_identity;
	}


	public JButton getButton_connect() {
		return button_connect;
	}


	public JPasswordField getTextarea_password() {
		return textarea_password;
	}


	public JTextField getTextarea_userName() {
		return textarea_userName;
	}


	public JTextArea getTextarea_type() {
		return textarea_type;
	}


	public JTextArea getTextarea_display() {
		return textarea_display;
	}


	public JButton getButton_quit() {
		return button_quit;
	}


	public JTextField getTextarea_currentRoom() {
		return textarea_currentRoom;
	}


	public JComboBox getTextlist_roomList() {
		return textlist_roomList;
	}


	public JScrollPane getScrollDisplay() {
		return scrollDisplay;
	}


	public JScrollPane getScrollDisplay_2() {
		return scrollDisplay_2;
	}


	public JScrollPane getScrollDisplay_3() {
		return scrollDisplay_3;
	}


	public JLabel getLblCurrentRoom() {
		return lblCurrentRoom;
	}


	public JButton getButton_refreshRoomList() {
		return button_refreshRoomList;
	}


	public JButton getButton_joinRoom() {
		return button_joinRoom;
	}


	public JSeparator getSeparator_3() {
		return separator_3;
	}


	public JTextArea getTextarea_whoOut() {
		return textarea_whoOut;
	}


	public JButton getButton_refreshWho() {
		return button_refreshWho;
	}


	public JButton getButton_sendMessage() {
		return button_sendMessage;
	}


	public JCheckBox getIsDebug() {
		return isDebug;
	}


	public BlockingQueue<String> getMessageQueue() {
		return messageQueue;
	}


	public SSLSocketFactory getSslsocketfactory() {
		return sslsocketfactory;
	}


	public static Window getTemp_frame() {
		return temp_frame;
	}
}
