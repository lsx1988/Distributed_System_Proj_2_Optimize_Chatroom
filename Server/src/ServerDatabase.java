/**
 * @author Shixun Liu
 * @Usage DS_Assignment1
 * @Decription The class used for handling clientInfo, identity lock and roomid lock for each server
 *             Utilizing singleton mode which can record the data continuously.
 * @Date 18/09/2016
 * @KeyMethod 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;

public class ServerDatabase {
	
	/*---------The attributes of class----------------*/
	
	//Recording the related info of client, including its connection socket, bw, br, roomid, isOwner...
	//Index by client connection socket
	private Map<Socket,JSONObject> userInfo;
	
	//Recording the server info
	private ArrayList<String[]> serverInfo;
	
	//The locking list of identity, indexed by serverID
	private ArrayList<JSONObject> identityLock;
	
	//The locking list of roomid, indexed by serverID
	private ArrayList<JSONObject> roomidLock;
	
	//Final data, the mainhall name of current server
	private final String mainhall = "MainHall-"+Server.serverID;
	
	/*------------The singletom mode of class------------*/
	 	
	//Create the singleton mode
	private static final ServerDatabase instance = new ServerDatabase();
	
	//Constructor
	public ServerDatabase(){
		userInfo = new HashMap<Socket, JSONObject>();
		serverInfo = new ArrayList<String[]>();
		identityLock = new ArrayList<JSONObject>();
		roomidLock = new ArrayList<JSONObject>();
	}
	
	//Get the unique and same instance of class
	public static synchronized ServerDatabase getInstance(){
		return instance;
	}
	
	/*-------------Method used for handling the serverInfo------------*/
	
	   	//return all server info
		public ArrayList<String[]> getServerInfo(){
			return serverInfo;
		}
		
		//add server config info to list
		public void addServer(String[] serverConfig){
			this.serverInfo.add(serverConfig);
		}
		
		//get the client port
		public int getClientPort(String serverID){
		
			int result=0;
			for(String[] info:this.serverInfo){
				if(serverID.equals(info[0])){
					result = Integer.parseInt(info[2]);
				}
			}
			return result;
		}
		
		//get the serverport
		public int getServerPort(String serverID){
		
			int result=0;
			for(String[] info:this.serverInfo){
				if(serverID.equals(info[0])){
					result = Integer.parseInt(info[3]);
				}
			}
			return result;
		}
		
		//get the serverip
		public InetAddress getServerIP(String serverID){
			
			InetAddress result = null;
			for(String[] info:this.serverInfo){
				if(serverID.equals(info[0])){
					try {
						result = InetAddress.getByName(info[1]);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return result;
		}
		
		//get the config info of specific server
		public String[] getInfo(String serverID){
			
			String[] result = null;
			for(String[] info:this.serverInfo){
				if(serverID.equals(info[0])){
					result = info;
				}
			}
			return result;
			
		}
	
	/*-------------Method used for handling the userInfo--------------*/
	 
	//When catch the client connection request, create the client info
	@SuppressWarnings("unchecked")
	public void createClient(Socket socket,BufferedReader br, BufferedWriter bw){
		JSONObject jsOb = new JSONObject();
		jsOb.put("socket",socket);
		jsOb.put("bw", bw);
		jsOb.put("br", br);
		jsOb.put("roomid", mainhall);
		jsOb.put("owner", false);
		jsOb.put("identity",null);
		this.userInfo.put(socket, jsOb);
	}
	
	/*--------------Method of setting data----------------*/
	 	
	//Update the owner key
	@SuppressWarnings("unchecked")
	public void setOwner(Socket socket, boolean isRoomOwner){
        JSONObject jsOb = this.userInfo.get(socket);
        jsOb.replace("owner", isRoomOwner);
		this.userInfo.replace(socket, jsOb);
	}
	
	//Update the identity key
	@SuppressWarnings("unchecked")
	public void setIdentity(Socket socket, String identity){
        JSONObject jsOb = this.userInfo.get(socket);
        jsOb.replace("identity", identity);
		this.userInfo.replace(socket, jsOb);
	}
	
	//Update the roomid key
	@SuppressWarnings("unchecked")
	public void setRoomid(Socket socket, String roomid){
        JSONObject jsOb = this.userInfo.get(socket);
        jsOb.replace("roomid", roomid);
		this.userInfo.replace(socket, jsOb);
	}
	
	/*-----------------Method for getting info----------------- */
	
	//Get owner value
	@SuppressWarnings("unchecked")
	public boolean isOwner(Socket socket){
		return (boolean) this.userInfo.get(socket).get("owner");
	}
	
	//Get identity value
	@SuppressWarnings("unchecked")
	public String getIdentity(Socket socket){
		return (String) this.userInfo.get(socket).get("identity");
	}
	
	//Get roomid value
	@SuppressWarnings("unchecked")
	public String getRoomid(Socket socket){
		return (String) this.userInfo.get(socket).get("roomid");
	}
	
	//Get the bufferedwriter object of any other client in the same room
	public BufferedWriter[] getBWsInSameRoom(Socket socket, boolean isSelfIncluded){
		
		Socket[] sockets = getSocketsInSameRoom(socket,isSelfIncluded);
		
		ArrayList<BufferedWriter> temp = new ArrayList<BufferedWriter>();
		
		for(Socket s:sockets){
			temp.add((BufferedWriter) this.userInfo.get(s).get("bw"));
		}

		return temp.toArray(new BufferedWriter[temp.size()]);
	}
	
	//Get all identity in the same room with current client
	public ArrayList<String> getAllIdentityInSameRoom(Socket socket, boolean isSelfIncluded){
		
		Socket[] sockets = getSocketsInSameRoom(socket,isSelfIncluded);
		
		ArrayList<String> temp = new ArrayList<String>();
		
		for(Socket s:sockets){
			temp.add(getIdentity(s));
		}

		return temp;
	}
	
	//Get the bufferedwriter object of any other client in the same room
	//True=include current client itself
	public Socket[] getSocketsInSameRoom(Socket socket, boolean isInclude){
		
		ArrayList<Socket> temp = new ArrayList<Socket>();
		String roomid = (String) this.userInfo.get(socket).get("roomid");
		
		for(Socket s:this.userInfo.keySet()){
			
			if(isInclude==false){
				
				if(s==socket){

				}else{
					if(((String)this.userInfo.get(s).get("roomid")).equals(roomid)){
						temp.add(s);
					}
				}
			}else{
				if(((String)this.userInfo.get(s).get("roomid")).equals(roomid)){
					temp.add(s);
				}
			}
		}		
		return temp.toArray(new Socket[temp.size()]);
	}
	
	//Get the owner identity of current roomid
	public String getRoomOwner(String roomID){
		
		String str = null;
		
		if(roomID.equals(mainhall)){
			return "";
		}else{
			for(JSONObject jsOb:this.userInfo.values()){
				if(((String) jsOb.get("roomid")).equals(roomID) && ((boolean) jsOb.get("owner"))==true){
					str = (String) jsOb.get("identity");
				}
			}
		}
		
		return str;		
	}
	
	//Get the roomid in current server
	public ArrayList<String> getAllRoomidInCurrentServer(){
		ArrayList<String> roomlist = new ArrayList<String>();
		
		for(String[] s:serverInfo){
			roomlist.add("MainHall-"+s[0]);
		}
		
		roomlist.add(mainhall);
		
		for(JSONObject jsOb:this.userInfo.values()){
			String roomid = (String) jsOb.get("roomid");
			if(!roomlist.contains(roomid)){
				roomlist.add(roomid);
			}
		}
		
		return roomlist;
	}
	
	//return roomid list
	public ArrayList<String> getAllRoomidIntheSystem(){
		ArrayList<String> roomlist = new ArrayList<String>();
		
		for(JSONObject jsOb:this.roomidLock){
			roomlist.add((String) jsOb.get("roomid"));
		}
		for(String room:this.getAllRoomidInCurrentServer()){
			roomlist.add(room);
		}
		return roomlist;
	}
	
	//return bw in mainhall
	public ArrayList<BufferedWriter> getAllIBwInMainHall(){
		ArrayList<BufferedWriter> bwList= new ArrayList<BufferedWriter>();
		
		for(JSONObject jsOb:this.userInfo.values()){
			if(((String) jsOb.get("roomid")).equals(mainhall)){
				bwList.add((BufferedWriter) jsOb.get("bw"));
			}
		}
		return bwList;
	}
	
		
	/*------------Judging method-------------*/
	 	
	//if the client identity is already existed in current server
	public boolean isIdentityExistInCurrentServer(String identity){
		
		boolean isExist = false;
		
		for(JSONObject jsOb:this.userInfo.values()){
			if(((String)jsOb.get("identity")).equals(identity)){
				isExist = true;
			}
		}		
		return isExist;
	}
	
	//if the roomid exist in current server
	public boolean isRoomidInCurrentServer(String roomid){
	
		boolean isExist = false;
		
		for(JSONObject jsOb:this.userInfo.values()){
			if(((String)jsOb.get("roomid")).equals(roomid)){
				isExist = true;
			}
		}		
		return isExist;
	}
	
	/*--------------other method------------------*/

	//reset the roomid of each client in the current chat room
	public void resetRoomID(Socket socket,boolean isSelfInclude){
		
		Socket[] sockets = getSocketsInSameRoom(socket,isSelfInclude);
		for(Socket s:sockets){
			setRoomid(s, mainhall);
		}
	}
	
	//remove the client from current server
	public void deleteClient(Socket socket){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		this.userInfo.remove(socket);
	}

/*-------------------------------------------------------------------------------*/
	
	//lock the identity
	@SuppressWarnings("unchecked")
	public void lockIdentity(String serverID, String identity){
		JSONObject jsOb = new JSONObject();
		jsOb.put("serverID", serverID);
		jsOb.put("identity", identity);
		this.identityLock.add(jsOb);
	}
	
	//release lock of identity
	@SuppressWarnings("unchecked")
	public void releaseIdentity(String serverID,String identity){
		JSONObject temp = new JSONObject();
		for(JSONObject jsOb:this.identityLock){
			if(((String)jsOb.get("serverID")).equals(serverID)&&((String)jsOb.get("identity")).equals(identity)){
				temp = jsOb;
			}
		}
		this.identityLock.remove(temp);
	}
	
	//if identity is locked
	public boolean isIdentityLocked(String identity){
		for(JSONObject jsOb:this.identityLock){
			if(jsOb.get("identity").equals(identity)){
				return true;
			}
		}
		return false;
	}
	
	//lock the roomid
	@SuppressWarnings("unchecked")
	public void lockRoomid(String serverID, String roomid){
		JSONObject jsOb = new JSONObject();
		jsOb.put("serverID", serverID);
		jsOb.put("roomid", roomid);
		this.roomidLock.add(jsOb);
	}
	
	//release lock of roomid
	@SuppressWarnings("unchecked")
	public void releaseRoomid(String serverID,String roomid){
		JSONObject temp = new JSONObject();
		for(JSONObject jsOb:this.roomidLock){
			if(((String)jsOb.get("serverID")).equals(serverID)&&((String)jsOb.get("roomid")).equals(roomid)){
				temp = jsOb;
			}
		}
		this.roomidLock.remove(temp);
	}
	
	//if roomid is locked
	public boolean isRoomidLocked(String roomid){
		for(JSONObject jsOb:this.roomidLock){
			if(jsOb.get("roomid").equals(roomid)){
				return true;
			}
		}
		return false;
	}
	
	//get the serverid based on roomid
	public String getServerIDInRoomidLock(String roomid){
		
		String serverID = null;
		
		for(JSONObject jsOb:this.roomidLock){
			if(((String) jsOb.get("roomid")).equals(roomid)){
				serverID = (String) jsOb.get("serverID");
			}
		}
		
		return serverID;
	}
	
	/*-------------------Operation----------------------*/
	
	//send message to clients in the same char room
	public void sentToClientsInSameChatRoom(Socket socket, String message, boolean isSelfInclude){
		for(BufferedWriter bw:this.getBWsInSameRoom(socket,isSelfInclude)){
			try {
				bw.write(message);
				bw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}			
		}
	}
	
	//send message to clients in the mainhall
	public void sentToClientsInMainHall(String message){
		for(BufferedWriter bw:this.getAllIBwInMainHall()){
			try {
				bw.write(message);
				bw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}			
		}
	}
	
	
	
}
