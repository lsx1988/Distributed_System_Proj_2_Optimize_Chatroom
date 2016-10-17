import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.net.ssl.SSLSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerConnection extends Thread {
	
	private SSLSocket server;
	private ServerDatabase ds;
	private JSONParser parser;
	
	public ServerConnection (SSLSocket server){
		this.server = server;
		this.ds = ServerDatabase.getInstance();
		this.parser = new JSONParser();
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		
		try {
					
			//create server socket bw and br
			BufferedWriter serverBW = new BufferedWriter(
											new OutputStreamWriter(
												server.getOutputStream(),"UTF-8"));
			BufferedReader serverBR = new BufferedReader(
											new InputStreamReader(
												server.getInputStream(),"UTF-8"));
			
			//get message from any other server
			String message = serverBR.readLine();

			/**
			 * receive the lockidentity message
			 */
			if(getMessage(message,"type").equals("lockidentity")){
			
				//get identity from message
				String identity = getMessage(message,"identity");
				
				//get serverid from message
				String serverID = getMessage(message,"serverid");
				
				//check if the requited identity is already exist in current server or its locking list
				String locked=null;
				if(ds.isIdentityExistInCurrentServer(identity) ||
						ds.isIdentityLocked(identity)){
					locked="false";
				}else{
					locked="true";
				}	
				
				//lock the identity
				ds.lockIdentity(serverID,identity);
				
				//create the lock identity reply message
				String str = lockReply(locked);
				
				//reply to the requesting sever
				serverBW.write(str+"\n");
				serverBW.flush();
			}
			
			/**
			 * 当type类型为releaseidentity时
			 */
			if(getMessage(message,"type").equals("releaseidentity")){
				
				//get the identity from message
				String identity = getMessage(message,"identity");
				
				//get the serverid from message
				String serverID = getMessage(message,"serverid");

				//release the identity in locking list
				ds.releaseIdentity(serverID, identity);

			}
			
			/**
			 * receive the lockuser message
			 */
			if(getMessage(message,"type").equals("lockuser")){
			
				//get username from message
				String username = getMessage(message,"username");
				
				//get password from message
				String password = getMessage(message,"password");
				
				//check if the requited username is already exist in current server or its locking list
				String locked=null;
				if(ds.isUsernameLocked(username)||
						ds.isUsernameHasLogined(username)){
					locked="false";
				}else{
					locked="true";
				}	
				
				//lock the username
				ds.lockUser(username,password);
				
				//create the lock identity reply message
				String str = lockReply(locked);
				
				//reply to the requesting sever
				serverBW.write(str+"\n");
				serverBW.flush();
			}
			
			/**
			 * 当type类型为releaseuser时
			 */
			if(getMessage(message,"type").equals("releaseuser")){
				
				//get the identity from message
				String username = getMessage(message,"username");
				
				//get the serverid from message
				String password = getMessage(message,"password");

				//release the identity in locking list
				ds.releaseUser(username,password);

			}
			
			/**
			 * When receive lockroomid message
			 */
			if(getMessage(message,"type").equals("lockroomid")){
				//get the roomid
				String roomid = getMessage(message,"roomid");
				
				//get the serverID
				String serverID = getMessage(message,"serverid");
				
				//create the reply message object
				JSONObject reply = new JSONObject();
				reply.put("type","lockroomid");
				reply.put("serverid", Server.serverID);
				reply.put("roomid", roomid);
											
				//check if above roomid is in current server or its lock list
				if(ds.isRoomidInCurrentServer(roomid)||ds.isRoomidLocked(roomid)){				
					reply.put("locked", "false");					
				}else{
					reply.put("locked", "true");
				}
				
				//lock the roomid
				ds.lockRoomid(serverID, roomid);
				
				//reply to the server
				serverBW.write(reply.toJSONString()+"\n");
				serverBW.flush();
			}
			
			/**
			 * When receive releaseroomid message
			 */
			if(getMessage(message,"type").equals("releaseroomid")){
				//get the approved data
				String approved = getMessage(message,"approved");
				
				//get the serverid
				String serverID = getMessage(message,"serverid");
				
				//get the roomid
				String roomid = getMessage(message,"roomid");
				
				if(approved.equals("false")){
					ds.releaseRoomid(serverID, roomid);
				}
			}
			
			/**
			 * When receive deleteroom message
			 */
			if(getMessage(message,"type").equals("deleteroom")){
				
				//get the roomid
				String roomid = getMessage(message,"roomid");
				
				//get the serverid
				String serverID = getMessage(message,"serverid");
				
				ds.releaseRoomid(serverID, roomid);
			}
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public String getMessage(String message, String key){
		
		JSONObject messageJSON = null;
		
		try {
			messageJSON = (JSONObject) this.parser.parse(message);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (String) messageJSON.get(key);
	}
		
	@SuppressWarnings("unchecked")
	public String lockReply(String locked){		
		JSONObject jsOb = new JSONObject();
		jsOb.put("locked", locked);
		return jsOb.toJSONString();			
	}
}
