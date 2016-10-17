package au.edu.unimelb.tcp.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageReceiveThread implements Runnable {

	private SSLSocket socket;
	private State state;
	private boolean debug;
	private Window frame;

	private BufferedReader in;

	private JSONParser parser = new JSONParser();

	private boolean run = true;
	
	private MessageSendThread messageSendThread;
	
	private SSLSocketFactory sslsocketfactory;

	public MessageReceiveThread(SSLSocket socket, State state, MessageSendThread messageSendThread, boolean debug, Window frame) throws IOException {
		this.socket = socket;
		this.state = state;
		this.debug = debug;
		this.frame = frame;
		this.messageSendThread = messageSendThread;		
		this.sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();	
	}

	@Override
	public void run() {
		
		try {
			this.in = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), "UTF-8"));
			JSONObject message;
			while (run) {
				message = (JSONObject) parser.parse(in.readLine());
				if (debug) {
					System.out.println("Receiving: " + message.toJSONString());
					System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				}
				MessageReceive(socket, message);
			}
			System.exit(0);
			in.close();
			socket.close();
		} catch (ParseException e) {
			System.out.println("Message Error: " + e.getMessage());
			//System.exit(1);
		} catch (IOException e) {
			System.out.println("Communication Error: " + e.getMessage());
			//System.exit(1);
		}

	}

	public void MessageReceive(SSLSocket socket, JSONObject message)
			throws IOException, ParseException {
		String type = (String) message.get("type");
		
		// ���յ��������˵�newidentity�ظ�ʱ
		if (type.equals("newidentity")) {
			
			//��ȡ�ظ���true����false
			String feedback = (String) message.get("approved");
			
			// ����ܾ����ӣ�identityֵ������Ҫ��
			if (feedback.equals("false")) {
				//��ʾ�û�������ʹ����
				this.frame.getTextOut().setText(state.getIdentity() + " already in use!\r\n");			
				//�ر���Ը�socket��������
				in.close();
				//�ر�socket���ӣ������˳��������û������ٴγ���
				socket.close();
			}
			// ����ܾ����ӣ��û��������벻����
			if(feedback.equals("NotMatch")){
				//��ʾ�û��������벻��
				this.frame.getTextOut().setText("Wrong username or password!\r\n");
				//�ر���Ը�socket��������
				in.close();
				//�ر�socket���ӣ������˳��������û������ٴγ���
				socket.close();
			}
			
			// ����ܾ����ӣ��˻����Ѿ���½��
			if(feedback.equals("repeatLogin")){
				//��ʾ�˻��Ѿ���½
				this.frame.getTextOut().setText("The username has already logged in!\r\n");
				//�ر���Ը�socket��������
				in.close();
				//�ر�socket���ӣ������˳��������û������ٴγ���
				socket.close();
			}
			
			//���������ټ����ж�
			return;
		}
		
		// ���������ظ�roomlistЭ��ʱ
		if (type.equals("roomlist")) {			
			//��ȡ��������
			ArrayList<String> array = (ArrayList<String>) message.get("rooms");

			//��������һ���������б���
			for (String a:array) {
				this.frame.getRoomList().addItem(a);
			}
			return;
		}

		// ������������roomchangeЭ��ʱ
		if (type.equals("roomchange")) {

			// ���roomid��ϢΪ�գ�˵���û�Ҫ�˳�
			if (message.get("roomid").equals("")) {
				
				// ����û�����ϢΪ��ǰ�û�
				if (message.get("identity").equals(state.getIdentity())) {
					//�ر�socket������
					in.close();
					//ֱ�ӹرմ��ڣ��˳�����
					System.exit(1);
				//���������û��˳�
				} else {
					//���ı��������ʾ�û���˭�˳���
					this.frame.getTextOut().append(message.get("identity") + " has quit!" +"\r\n");
				}
			// ����û�û����ʷroomid��Ϣ����Ϊ���û�
			} else if (message.get("former").equals("")) {
				
				// �����������Ϣ�У�identity�뵱ǰ��¼��identityһ�£�������roomid��Ϣ
				if (message.get("identity").equals(state.getIdentity())) {
					state.setRoomId((String) message.get("roomid"));
					//�����ӳɹ�ʱ����ʾ��ǰ�û����ڵķ���
					this.frame.getCurrentRoom().setText((String) message.get("roomid"));
					//���ı���ʾ������ʾ����½�ɹ���
					this.frame.getTextOut().append("----You login the Chatting System Successfully----\r\n");
					//�����ӳɹ��󣬽�Connect��ť���ã���ֹ�û��ٴε����ɱ���
					this.frame.getConnectButtton().setEnabled(false);
				}
				//��Ϊ�����û�ʱ		
				//���ı���ʾ������ʾ��XXX�û���½�ˡ�
				this.frame.getTextOut().append(message.get("identity") + " has moved to " + message.get("roomid") + "\r\n");
						
			// ����ʷ�������·���һֱʱ���û�ʵ��û���ƶ�
			} else if (message.get("former").equals(message.get("roomid"))) {
				
				//��ǰ�û�������ʾ���䣬�������κθĶ�
				this.frame.getTextOut().append("roomid is not available\r\n");
			}
			// ���û����ڷ���ȷ�иı�ʱ
			else {
				// ����û����뵱ǰ�û�һ�£�����������ڷ���
				if (message.get("identity").equals(state.getIdentity())) {
					state.setRoomId((String) message.get("roomid"));
					this.frame.getTextOut().append("You move the new roomid successfully\r\n");
					//���µ�ǰ�û����ڵķ���
					this.frame.getCurrentRoom().setText((String) message.get("roomid"));
				}
				
				//���ı���ʾ������ʾ��XXX�ƶ���ĳ���䡱
				this.frame.getTextOut().append(message.get("identity") + " has moved to " + message.get("roomid")+"\r\n");				
			}
			return;
		}
		
		// server reply of #who
		if (type.equals("roomcontents")) {
			JSONArray array = (JSONArray) message.get("identities");
			System.out.print(message.get("roomid") + " contains");
			for (int i = 0; i < array.size(); i++) {
				System.out.print(" " + array.get(i));
				if (message.get("owner").equals(array.get(i))) {
					System.out.print("*");
				}
			}
			System.out.println();
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			return;
		}
		
		// server forwards message
		if (type.equals("message")) {
			System.out.println(message.get("identity") + ": "
					+ message.get("content"));
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			return;
		}
		
		
		// server reply of #createroom
		if (type.equals("createroom")) {
			boolean approved = Boolean.parseBoolean((String)message.get("approved"));
			String temp_room = (String)message.get("roomid");
			if (!approved) {
				System.out.println("Create room " + temp_room + " failed.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			else {
				System.out.println("Room " + temp_room + " is created.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			return;
		}
		
		// server reply of # deleteroom
		if (type.equals("deleteroom")) {
			boolean approved = Boolean.parseBoolean((String)message.get("approved"));
			String temp_room = (String)message.get("roomid");
			if (!approved) {
				System.out.println("Delete room " + temp_room + " failed.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			else {
				System.out.println("Room " + temp_room + " is deleted.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			return;
		}
		
		// server directs the client to another server
		if (type.equals("route")) {
			String temp_room = (String)message.get("roomid");
			String host = (String)message.get("host");
			int port = Integer.parseInt((String)message.get("port"));
			
			// connect to the new server
			if (debug) {
				System.out.println("Connecting to server " + host + ":" + port);
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			
			//Socket temp_socket = new Socket(host, port);
			SSLSocket temp_socket = (SSLSocket) sslsocketfactory.createSocket(host, port);
			
			// send #movejoin
			//BufferedWriter out = new DataOutputStream(temp_socket.getOutputStream());
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(temp_socket.getOutputStream()));
			JSONObject request = ClientMessages.getMoveJoinRequest(state.getIdentity(), state.getRoomId(), temp_room);
			if (debug) {
				System.out.println("Sending: " + request.toJSONString());
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			send(out, request);
			
			// wait to receive serverchange
			BufferedReader temp_in = new BufferedReader(new InputStreamReader(temp_socket.getInputStream()));
			JSONObject obj = (JSONObject) parser.parse(temp_in.readLine());
			
			if (debug) {
				System.out.println("Receiving: " + obj.toJSONString());
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			
			// serverchange received and switch server
			if (obj.get("type").equals("serverchange") && obj.get("approved").equals("true")) {
				messageSendThread.switchServer(temp_socket, out);
				switchServer(temp_socket, temp_in);
				String serverid = (String)obj.get("serverid");
				System.out.println(state.getIdentity() + " switches to server " + serverid);
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			// receive invalid message
			else {
				temp_in.close();
				out.close();
				temp_socket.close();
				System.out.println("Server change failed");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			return;
		}
		
		if (debug) {
			System.out.println("Unknown Message: " + message);
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
		}
	}
	
	public void switchServer(SSLSocket temp_socket, BufferedReader temp_in) throws IOException {
		in.close();
		in = temp_in;
		socket.close();
		socket = temp_socket;
	}

	private void send(BufferedWriter out, JSONObject obj) throws IOException {
		out.write((obj.toJSONString() + "\n"));
		out.flush();
	}
}
