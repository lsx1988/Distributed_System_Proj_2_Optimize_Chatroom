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
		
		// 当收到服务器端的newidentity回复时
		if (type.equals("newidentity")) {
			
			//获取回复是true还是false
			String feedback = (String) message.get("approved");
			
			// 如果拒绝链接（identity值不符合要求）
			if (feedback.equals("false")) {
				//提示用户名正在使用中
				this.frame.getTextOut().setText(state.getIdentity() + " already in use!\r\n");			
				//关闭针对该socket的输入流
				in.close();
				//关闭socket链接，但不退出程序，让用户可以再次尝试
				socket.close();
			}
			// 如果拒绝链接（用户名与密码不符）
			if(feedback.equals("NotMatch")){
				//提示用户名与密码不符
				this.frame.getTextOut().setText("Wrong username or password!\r\n");
				//关闭针对该socket的输入流
				in.close();
				//关闭socket链接，但不退出程序，让用户可以再次尝试
				socket.close();
			}
			
			// 如果拒绝链接（账户名已经登陆）
			if(feedback.equals("repeatLogin")){
				//提示账户已经登陆
				this.frame.getTextOut().setText("The username has already logged in!\r\n");
				//关闭针对该socket的输入流
				in.close();
				//关闭socket链接，但不退出程序，让用户可以再次尝试
				socket.close();
			}
			
			//结束，不再继续判定
			return;
		}
		
		// 当服务器回复roomlist协议时
		if (type.equals("roomlist")) {			
			//获取房间数组
			ArrayList<String> array = (ArrayList<String>) message.get("rooms");

			//将房间逐一加入下拉列表中
			for (String a:array) {
				this.frame.getRoomList().addItem(a);
			}
			return;
		}

		// 当服务器返回roomchange协议时
		if (type.equals("roomchange")) {

			// 如果roomid信息为空，说明用户要退出
			if (message.get("roomid").equals("")) {
				
				// 如果用户名信息为当前用户
				if (message.get("identity").equals(state.getIdentity())) {
					//关闭socket输入流
					in.close();
					//直接关闭窗口，退出程序
					System.exit(1);
				//若是其他用户退出
				} else {
					//在文本面板上提示用户是谁退出了
					this.frame.getTextOut().append(message.get("identity") + " has quit!" +"\r\n");
				}
			// 如果用户没有历史roomid信息，则为新用户
			} else if (message.get("former").equals("")) {
				
				// 如果发来的信息中，identity与当前记录的identity一致，更新其roomid信息
				if (message.get("identity").equals(state.getIdentity())) {
					state.setRoomId((String) message.get("roomid"));
					//当连接成功时，显示当前用户所在的房间
					this.frame.getCurrentRoom().setText((String) message.get("roomid"));
					//在文本显示窗口提示“登陆成功”
					this.frame.getTextOut().append("----You login the Chatting System Successfully----\r\n");
					//当链接成功后，将Connect按钮禁用，防止用户再次点击造成报错
					this.frame.getConnectButtton().setEnabled(false);
				}
				//当为其他用户时		
				//在文本显示窗口提示“XXX用户登陆了”
				this.frame.getTextOut().append(message.get("identity") + " has moved to " + message.get("roomid") + "\r\n");
						
			// 当历史房间与新房间一直时，用户实际没有移动
			} else if (message.get("former").equals(message.get("roomid"))) {
				
				//当前用户房间显示不变，不许做任何改动
				this.frame.getTextOut().append("roomid is not available\r\n");
			}
			// 当用户所在房间确有改变时
			else {
				// 如果用户名与当前用户一致，则更新其所在房间
				if (message.get("identity").equals(state.getIdentity())) {
					state.setRoomId((String) message.get("roomid"));
					this.frame.getTextOut().append("You move the new roomid successfully\r\n");
					//更新当前用户所在的房间
					this.frame.getCurrentRoom().setText((String) message.get("roomid"));
				}
				
				//在文本显示窗口提示“XXX移动到某房间”
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
