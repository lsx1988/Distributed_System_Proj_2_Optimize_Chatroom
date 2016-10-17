package au.edu.unimelb.tcp.client;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import org.json.simple.JSONObject;

public class MessageSendThread implements Runnable {

	private SSLSocket socket;

	private BufferedWriter out;
	
	private State state;

	private boolean debug;
	
	private Window frame;
	
	private String msg;
	
	private boolean wait;

	public MessageSendThread(SSLSocket socket, State state, boolean debug, Window frame) throws IOException {
		this.socket = socket;
		this.state = state;
		this.debug = debug;
		this.frame = frame;
		this.msg = null;
		this.wait = true;
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));		
	}

	@Override
	public void run() {

		try {
			// send the #newidentity command
			MessageSend(socket, "#newidentity " + state.getIdentity() + " " + state.getUsername() + " " + state.getPassword());
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		while (true) {
			try {
				while(wait){
					this.frame.getQuitButtton().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							msg = "#quit";
							wait = false;
						}
					});
					this.frame.getRefreshRoomButtton().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							msg = "#list";
							wait = false;
						}
					});
				}
				MessageSend(socket, msg);
				//wait = true;
			} catch (IOException e) {
				System.out.println("Communication Error: " + e.getMessage());
				System.out.println("the button is wrong");
				System.exit(1);
			}
		}
		
	}

	private void send(JSONObject obj) throws IOException {
		if (debug) {
			System.out.println("Sending: " + obj.toJSONString());
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
		}
		out.write((obj.toJSONString() + "\n"));
		out.flush();
	}
	
	// send command and check validity
	public void MessageSend(SSLSocket socket, String msg) throws IOException {
		JSONObject sendToServer = new JSONObject();
		String []array = msg.split(" ");
		if(!array[0].startsWith("#")) {
			sendToServer = ClientMessages.getMessage(msg);
			send(sendToServer);
		}
		else if(array.length == 1) {
			if(array[0].startsWith("#list")) {
				sendToServer = ClientMessages.getListRequest();
				send(sendToServer);
			}
			else if(array[0].startsWith("#quit")) {
				sendToServer = ClientMessages.getQuitRequest();
				send(sendToServer);
			}
			else if(array[0].startsWith("#who")) {
				sendToServer = ClientMessages.getWhoRequest();
				send(sendToServer);
			}
			else {
				System.out.println("Invalid command!");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
		}
		else if (array.length == 2) {
			if(array[0].startsWith("#joinroom")) {
				sendToServer = ClientMessages.getJoinRoomRequest(array[1]);
				send(sendToServer);
			}
			else if(array[0].startsWith("#createroom")) {
				sendToServer = ClientMessages.getCreateRoomRequest(array[1]);
				send(sendToServer);
			}
			else if(array[0].startsWith("#deleteroom")) {
				sendToServer = ClientMessages.getDeleteRoomRequest(array[1]);
				send(sendToServer);
			}
			else {
				System.out.println("Invalid command!");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
		}
		else if(array.length == 4){
			if(array[0].startsWith("#newidentity")){
				sendToServer = ClientMessages.getNewIdentityRequest(array[1],array[2],array[3]);
				send(sendToServer);
			}
			else {
				System.out.println("Invalid command!");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
		}
		else {
			System.out.println("Invalid command!");
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
		}
		
	}

	public void switchServer(SSLSocket temp_socket, BufferedWriter temp_out) throws IOException {
		// switch server initiated by the receiving thread
		// need to use synchronize
		synchronized(out) {
			out.close();
			out = temp_out;
		}
		socket = temp_socket;
	}
}
