import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONObject;

public class HeartBeatThread extends Thread {
	
	private ServerDatabase ds;
	private String[] server_info;
	private boolean flag=false;
	//private Socket socket = null;
	private SSLSocketFactory sslsocketfactory;
	private JSONObject message;
	private BufferedWriter bw;
	
	public HeartBeatThread(String[] info){	
		this.ds = ServerDatabase.getInstance();
		this.server_info=info;
		this.sslsocketfactory=(SSLSocketFactory) SSLSocketFactory.getDefault();
		this.message = new JSONObject();
	}
	
	@SuppressWarnings("resource")
	public void run(){
		message.put("type", "heartbeat");
		JSONObject jsOb = new JSONObject();
		jsOb.put("serverid", Server.serverID);
		jsOb.put("serverIP", Server.server_IP.getHostName());
		jsOb.put("client_port", Integer.toString(Server.client_port));
		jsOb.put("server_port", Integer.toString(Server.server_port));
		jsOb.put("heartbeat_port", Integer.toString(Server.heartbeat_port));
		jsOb.put("online", false);
		message.put("serverInfo", jsOb.toJSONString());
		String msg = message.toJSONString();
			
		while(true){
			try {						
				//socket = new Socket(server_info[1],Integer.parseInt(server_info[3]));
				//System.out.println(server_info[1]);
				//System.out.println(server_info[3]);
				SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(server_info[1],Integer.parseInt(server_info[3]));
				bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));				
				bw.write(msg+"\n");
				bw.flush();
				if(flag){
					ds.setServerOnline(server_info[0], true);
					flag=false;
				}
				Thread.sleep(3000);
				
			}catch (Exception e) {
				//e.printStackTrace();
				System.out.println("Server "+server_info[0]+" is not online");
				if(!false){	
					ds.setServerOnline(server_info[0], false);
					ds.removeRoomidOfServer(server_info[0]);
					flag=true;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				}
			}
		}
	}
}
