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

import javax.net.ssl.SSLSocket;

import org.json.simple.JSONObject;

public class ServerDatabase {
	
	/*---------The attributes of class----------------*/
	
	//Recording the related info of client, including its connection socket, bw, br, roomid, isOwner...
	//Index by client connection socket
	private Map<SSLSocket,JSONObject> userInfo;
	
	//Recording the server info
	private Map<String, JSONObject> serverInfo;
	
	//Recording the approved user info
	private Map<String,String> approvedUserInfo;
	
	//The locking list of identity, indexed by serverID
	private ArrayList<JSONObject> identityLock;
	
	//The locking list of user, indexed by username
	private ArrayList<JSONObject> usernameLock;
	
	//The locking list of roomid, indexed by serverID
	private ArrayList<JSONObject> roomidLock;
	
	//Final data, the mainhall name of current server
	private final String mainhall = "MainHall-"+Server.serverID;
	
	/*------------The singletom mode of class------------*/
	 	
	//Create the singleton mode
	private static final ServerDatabase instance = new ServerDatabase();
	
	//Constructor
	public ServerDatabase(){
		userInfo = new HashMap<SSLSocket, JSONObject>();
		serverInfo = new HashMap<String, JSONObject>();
		identityLock = new ArrayList<JSONObject>();
		roomidLock = new ArrayList<JSONObject>();
		usernameLock = new ArrayList<JSONObject>();
		approvedUserInfo = new HashMap<String,String>();
	}
	
	//Get the unique and same instance of class
	public static synchronized ServerDatabase getInstance(){
		return instance;
	}
	
	/*-------------Method used for handling the approved user Info------------*/
	
	//add approved user info to list
	public void addUser(String username,String password){
		approvedUserInfo.put(username, password);
	}
	
	//check if the username and password is matching
	public boolean isUsernameAndPasswordMatch(String username,String password){
		if(approvedUserInfo.get(username).equals(password)){
			return true;
		}else{
			return false;
		}
	}
		
	/*-------------Method used for handling the serverInfo------------*/
	
   	//return all server info
	public Map<String, JSONObject> getServerInfo(){
		return serverInfo;
	}
		
	//add server config info to list
	public void addServer(String[] serverConfig){
		String serverid = serverConfig[0];
		String serverIP = serverConfig[1];
		int client_port = Integer.parseInt(serverConfig[2]);
		int server_port = Integer.parseInt(serverConfig[3]);
		boolean online = true;
		JSONObject jsOb = new JSONObject();	
		jsOb.put("serverIP", serverIP);
		jsOb.put("client_port", client_port);
		jsOb.put("server_port", server_port);
		jsOb.put("online", online);
		this.serverInfo.put(serverid, jsOb);
	}
	
	//get the client port
	public int getClientPort(String serverID){
		return (int)serverInfo.get(serverID).get("client_port");
	}
	
	//get the serverport
	public int getServerPort(String serverID){
		return (int)serverInfo.get(serverID).get("server_port");
	}
	
	//get the serverip
	public String getServerIP(String serverID) throws UnknownHostException{
		return (String)(serverInfo.get(serverID).get("serverIP"));
	}
	
	//get the config info of specific server
	public JSONObject getInfo(String serverID){

		return this.serverInfo.get(serverID);		
	}
	
	public void setServerOnline(String serverid, boolean isOnline){
		JSONObject jsOb = this.serverInfo.get(serverid);
		jsOb.replace("online", isOnline);
		this.serverInfo.replace(serverid, jsOb);
	}
	
	public boolean isServerOnline(String serverid){
		return (boolean)this.serverInfo.get(serverid).get("online");
	}
	
	public boolean isServerInServerInfo(String serverid){
		return this.serverInfo.containsKey(serverid);
	}
	
	public ArrayList<String> getOnlineServerID(){
		ArrayList<String> temp = new ArrayList<String>();
		for(String s:this.serverInfo.keySet()){
			if((boolean)this.serverInfo.get(s).get("online")){
				temp.add(s);
			}
		}
		return temp;
	}
	
	/*-------------Method used for handling the userInfo--------------*/
	 
	//When catch the client connection request, create the client info
	@SuppressWarnings("unchecked")
	public void createClient(SSLSocket socket,BufferedReader br, BufferedWriter bw, String username, String password){
		JSONObject jsOb = new JSONObject();
		jsOb.put("socket",socket);
		jsOb.put("bw", bw);
		jsOb.put("br", br);
		jsOb.put("roomid", mainhall);
		jsOb.put("owner", false);
		jsOb.put("identity",null);
		jsOb.put("username", username);
		jsOb.put("password",password);
		this.userInfo.put(socket, jsOb);
	}
	
	/*--------------Method of setting data----------------*/
	 	
	//Update the owner key
	@SuppressWarnings("unchecked")
	public void setOwner(SSLSocket socket, boolean isRoomOwner){
        JSONObject jsOb = this.userInfo.get(socket);
        jsOb.replace("owner", isRoomOwner);
		this.userInfo.replace(socket, jsOb);
	}
	
	//Update the identity key
	@SuppressWarnings("unchecked")
	public void setIdentity(SSLSocket socket, String identity){
        JSONObject jsOb = this.userInfo.get(socket);
        jsOb.replace("identity", identity);
		this.userInfo.replace(socket, jsOb);
	}
	
	//Update the roomid key
	@SuppressWarnings("unchecked")
	public void setRoomid(SSLSocket socket, String roomid){
        JSONObject jsOb = this.userInfo.get(socket);
        jsOb.replace("roomid", roomid);
		this.userInfo.replace(socket, jsOb);
	}
	
	/*-----------------Method for getting info----------------- */
	
	//Get owner value
	@SuppressWarnings("unchecked")
	public boolean isOwner(SSLSocket socket){
		return (boolean) this.userInfo.get(socket).get("owner");
	}
	
	//Get identity value
	@SuppressWarnings("unchecked")
	public String getIdentity(SSLSocket socket){
		return (String) this.userInfo.get(socket).get("identity");
	}
	
	//Get roomid value
	@SuppressWarnings("unchecked")
	public String getRoomid(SSLSocket socket){
		return (String) this.userInfo.get(socket).get("roomid");
	}
	
	//Get the bufferedwriter object of any other client in the same room
	public BufferedWriter[] getBWsInSameRoom(SSLSocket socket, boolean isSelfIncluded){
		
		SSLSocket[] sockets = getSocketsInSameRoom(socket,isSelfIncluded);
		
		ArrayList<BufferedWriter> temp = new ArrayList<BufferedWriter>();
		
		for(SSLSocket s:sockets){
			temp.add((BufferedWriter) this.userInfo.get(s).get("bw"));
		}

		return temp.toArray(new BufferedWriter[temp.size()]);
	}
	
	//Get all identity in the same room with current client
	public ArrayList<String> getAllIdentityInSameRoom(SSLSocket socket, boolean isSelfIncluded){
		
		SSLSocket[] sockets = getSocketsInSameRoom(socket,isSelfIncluded);
		
		ArrayList<String> temp = new ArrayList<String>();
		
		for(SSLSocket s:sockets){
			temp.add(getIdentity(s));
		}

		return temp;
	}
	
	//Get the bufferedwriter object of any other client in the same room
	//True=include current client itself
	public SSLSocket[] getSocketsInSameRoom(SSLSocket socket, boolean isInclude){
		
		ArrayList<SSLSocket> temp = new ArrayList<SSLSocket>();
		String roomid = (String) this.userInfo.get(socket).get("roomid");
		
		for(SSLSocket s:this.userInfo.keySet()){
			
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
		return temp.toArray(new SSLSocket[temp.size()]);
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
	
	//Remove the server and its room from roomlock
	public void removeRoomidOfServer(String serverid){
		for(JSONObject jsOb:this.roomidLock){
			if(((String)jsOb.get("serverID")).equals(serverid)){
				this.roomidLock.remove(jsOb);
			}
		}
	}
	
	//Get the roomid in current server
	public ArrayList<String> getAllRoomidInCurrentServer(){
		ArrayList<String> roomlist = new ArrayList<String>();		
		
		for(JSONObject jsOb:this.userInfo.values()){
			String roomid = (String) jsOb.get("roomid");
			if(!roomlist.contains(roomid)&& !roomid.equals(mainhall)){
				roomlist.add(roomid);
			}
		}
		
		return roomlist;
	}
	
	//return roomid list
	public ArrayList<String> getAllRoomidIntheSystem(){
		ArrayList<String> roomlist = new ArrayList<String>();
		System.out.println(this.getOnlineServerID());
		for(String s:this.getOnlineServerID()){
			for(JSONObject jsOb:this.roomidLock){
				if(jsOb.containsValue(s)){
					roomlist.add((String) jsOb.get("roomid"));
				}
			}
		}
		
		for(String s:this.getOnlineServerID()){
			roomlist.add("MainHall-"+s);
		}
		
		roomlist.add(mainhall);
		
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
	
	//if the username and password has already logined in this server
	public boolean isUsernameHasLogined(String username){
		boolean isExist = false;
		
		for(JSONObject jsOb:this.userInfo.values()){
			if(jsOb.containsValue(username)){
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
	public void resetRoomID(SSLSocket socket,boolean isSelfInclude){
		
		SSLSocket[] sockets = getSocketsInSameRoom(socket,isSelfInclude);
		for(SSLSocket s:sockets){
			setRoomid(s, mainhall);
		}
	}
	
	//remove the client from current server
	public void deleteClient(SSLSocket socket){
		try {
			this.userInfo.remove(socket);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}	
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
	
	//lock the user
	@SuppressWarnings("unchecked")
	public void lockUser(String username, String password){
		JSONObject jsOb = new JSONObject();
		jsOb.put("username", username);
		jsOb.put("password", password);
		this.usernameLock.add(jsOb);
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
	
	//release lock of username
	@SuppressWarnings("unchecked")
	public void releaseUser(String username,String password){
		JSONObject temp = new JSONObject();
		for(JSONObject jsOb:this.usernameLock){
			if(jsOb.containsValue(username)&&jsOb.containsValue(password)){
				temp = jsOb;
			}
		}
		this.usernameLock.remove(temp);
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
	
	//if username is locked
	public boolean isUsernameLocked(String username){
		for(JSONObject jsOb:this.usernameLock){
			if(jsOb.containsValue(username)){
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
	public void sentToClientsInSameChatRoom(SSLSocket socket, String message, boolean isSelfInclude){
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
