import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientConnection extends Thread {
	
	private SSLSocket client;
	private ServerDatabase ds;
	private JSONParser parser;
	private JSONObject reply;
	SSLSocketFactory sslsocketfactory;
	
	public ClientConnection (SSLSocket client){
		this.client = client;
		this.reply = new JSONObject();
		this.ds = ServerDatabase.getInstance();
		this.parser = new JSONParser();
		this.sslsocketfactory= (SSLSocketFactory) SSLSocketFactory.getDefault();
	}
	
	public void run(){
		
		try {			
			//Get the br and bw of current client thread
			BufferedWriter clientBW = new BufferedWriter(
											new OutputStreamWriter(
												client.getOutputStream(),"UTF-8"));
			BufferedReader clientBR = new BufferedReader(
											new InputStreamReader(
												client.getInputStream(),"UTF-8"));
			//keep listening to client input
			while(client.isClosed()==false){
				
				//Get the message sent from client
				String message = clientBR.readLine();
				
				/**
				 * Function 4.1 Create the new identity in current server
				 * */
				if(getMessage(message,"type").equals("newidentity")){
					
					//Init the flag
					String isApproval = "true";
					
					//get the identity of new client
					String identity = getMessage(message,"identity");
					
					/*If the identity is already in current server of its locking list
					 * or the identity length is wrong, refuse the connection request
					 */
					if(ds.isIdentityExistInCurrentServer(identity)||
							ds.isIdentityLocked(identity)||
								identity.length()<3||identity.length()>16){
						//refuse the client
						clientBW.write(newidentity("false"));
						clientBW.flush();
						client.close();
						
					}else{

						//add the new client to server database first, in case other thread will add the same client again
						ds.createClient(client, clientBR, clientBW);
						
						//update client with its identity
						ds.setIdentity(client, identity);
						
						//send lockidentity message to each server and get their reply
						for(String[] s:ds.getServerInfo()){
							
							//send to server and get feedback
							JSONObject jsOb = sendToServerAndGetFeedback(s[0],lockidentity(Server.serverID,identity));
							
							//if any feedback with false, update the flag
							if(((String)jsOb.get("locked")).equals("false")){
								isApproval = "false";
							}
						}
						
						//reply to current client based on feedback from other server
						clientBW.write(newidentity(isApproval));
						clientBW.flush();
						
						//create roomchange message if new identity is approved
						if(isApproval.equals("true")){	
							
							//get new client roomid, namely the mainhall name of current server
							String roomid = ds.getRoomid(client);
							
							//send roomchange message to all client in mainhall, including self
							ds.sentToClientsInSameChatRoom(client,roomchange(identity,"",roomid),true);
							
						//if flag is false, close the connection and reomve client from server database
						}else{
							client.close();
							ds.deleteClient(client);
						}
						
						//create releaseidentity message to other server
						for(String[] s:ds.getServerInfo()){
							sendToServer(s[0],releaseidentity(Server.serverID,identity));
						}
					}				
				}
		
				/**
				 * Function 4.2 List out the chat room in the system
				 */
				
				if(getMessage(message,"type").equals("list")){					
					//get all rooms in system and send to current client					
					clientBW.write(roomlist(ds.getAllRoomidIntheSystem()));
					clientBW.flush();
				}
				
				/**
				 * Function 4.3 Ask for list of clients in the current chat room
				 */
				if(getMessage(message,"type").equals("who")){
					
					//Get the roomid of current client
					String roomid  = ds.getRoomid(client);
					
					//Get the room owner of about roomid
					String owner = ds.getRoomOwner(roomid);
					
					//Get all the identities of in the above roomid, self include
					ArrayList<String> identities = ds.getAllIdentityInSameRoom(client, true);
					
					//Send roomcontents to client
					clientBW.write(roomcontents(roomid,identities,owner));
					clientBW.flush();
				}
				
				/**
				 * Function 4.4 Create Room
				 */
				
				if(getMessage(message,"type").equals("createroom")){
					
					//init the flag
					String isApproval = "true";
					
					//get the roomid current client want to create
					String roomid = getMessage(message,"roomid");
					
					//get the identity of current client
					String identity = ds.getIdentity(client);
					
					//get the current roomid of client
					String formerRoomid = ds.getRoomid(client);
					
					/*if the roomid length is wrong or the roomid is already in current server
					 * or the roomid is in the roomlock list
					 */
					
					if(roomid.length()<3 || roomid.length()>16||
							ds.isRoomidLocked(roomid)||ds.isRoomidInCurrentServer(roomid)||
								ds.isOwner(client)){
						//refure the request
						clientBW.write(createroom(roomid,"false"));
						clientBW.flush();
					}else{						
						for(String[] s:ds.getServerInfo()){							
							//send to lockroomid message to other server
							JSONObject jsOb = sendToServerAndGetFeedback(s[0],lockroomid(Server.serverID,roomid));							
							
							//if any feedback with false, update the flag
							if(((String)jsOb.get("locked")).equals("false")){
								isApproval = "false";
							}
						}
						
						//create the createroom reply to current client based on above feedback
						clientBW.write(createroom(roomid,isApproval));
						clientBW.flush();
						
						//if create room is approved
						if(isApproval.equals("true")){
							
							//Broadcast roomchange message to all client in the current chat room, self included
							ds.sentToClientsInSameChatRoom(client, roomchange(identity, formerRoomid, roomid), true);
							
							//move the client to new roomid
							ds.setRoomid(client, roomid);
							
							//set current client as the room owner
							ds.setOwner(client, true);
						}
						
						//create releaseroomid message and send to other server
						for(String[] s:ds.getServerInfo()){
							sendToServer(s[0],releaseroomid(Server.serverID,roomid,isApproval));
						}
					}
				}
				
				/**
				 * Function 4.5 Join room
				 */
				
				if(getMessage(message,"type").equals("join")){
					
					//Get the former roomid of current client
					String formerRoomid = ds.getRoomid(client);
					
					//Get the roomid the current client want to join
					String roomid = getMessage(message,"roomid");
										
					//get the identity of current client
					String identity = ds.getIdentity(client);
					
					//check if roomid is not available in current server or its roomid lock list
					
					//if roomid is non-existent chat room || or client is room owner
					if((!ds.isRoomidInCurrentServer(roomid)&&!ds.isRoomidLocked(roomid)) ||
							ds.isOwner(client)){
						
						//refuse the join request
						clientBW.write(roomchange(identity, roomid, roomid));
						clientBW.flush();	
					
						//if roomid is in current server
					}else if(ds.isRoomidInCurrentServer(roomid)){
						
						//inform the other clients in current roomid, the current client is moving out
						ds.sentToClientsInSameChatRoom(client, roomchange(identity, formerRoomid, roomid), false);
						
						//move the current client to new chat room
						ds.setRoomid(client, roomid);
						
						//inform the client in new roomid, the current client is moving in
						ds.sentToClientsInSameChatRoom(client, roomchange(identity, formerRoomid, roomid), true);
						
						//if roomid is in remote server
					}else{
						
						//get the serverID of the new chat room
						String serverID = ds.getServerIDInRoomidLock(roomid);
						
						//get host of above serverID
						String serverIP = ds.getInfo(serverID)[1];
						
						//get port of above serverID
						String clientPort = ds.getInfo(serverID)[2];
						
						//send route reply message to current client
						clientBW.write(route(roomid, serverIP, clientPort));
						clientBW.flush();
						
						//broadcast the reply message to members in the former chat room, inform the current client is moving out
						ds.sentToClientsInSameChatRoom(client, roomchange(identity, formerRoomid, roomid), true);
						
						//remove the client from current server
						ds.deleteClient(client);
					}
				}
				
				/**
				 * when reveive the movejoin message
				 */
				if(getMessage(message,"type").equals("movejoin")){
					
					//get the identity of new join client
					String identity = getMessage(message,"identity");
					
					//get the roomid of new join client want to join
					String roomid = getMessage(message,"roomid");
					
					//get the former roomid of new join client
					String formerRoomid = getMessage(message,"former");
					
					//add the new coming client to server database, default roomid is mainhall
					ds.createClient(client, clientBR, clientBW);
					ds.setIdentity(client, identity);
					
					//if the request roomid is still avaiable, move client to the roomid
					if(ds.isRoomidInCurrentServer(roomid)){
						ds.setRoomid(client, roomid);
					}
					
					//seng serverchange message to client
					clientBW.write(serverchange("true", Server.serverID));
					clientBW.flush();
					
					//inform the clients in the chat room that a new client is moving in
					ds.sentToClientsInSameChatRoom(client, roomchange(identity, formerRoomid, roomid), true);
				}
				

				
				/**
				 * Function 4.6 delete room
				 */
				if(getMessage(message,"type").equals("deleteroom")){
					
					//get the roomid the client want to delete
					String roomid = getMessage(message,"roomid");
					
					//check if the client is the room owner
					if(!(ds.isOwner(client) && ds.getRoomid(client).equals(roomid))){
						//not the owner, defuse the request
						clientBW.write(deleteroomToClient(roomid, "false"));
						clientBW.flush();
					}else{
						//is the owner
						
						//get all clients in the deleted room
						ArrayList<String> clientInCurrentRoom = ds.getAllIdentityInSameRoom(client, true);
						
						//inform all the client in current chat room that every one is moving out
						//inform all the client in the mainhall that some clients are moving in
						for(String identity:clientInCurrentRoom){							
							ds.sentToClientsInSameChatRoom(client, roomchange(identity, roomid, "MainHall-"+Server.serverID), true);
							ds.sentToClientsInMainHall(roomchange(identity, roomid, "MainHall-"+Server.serverID));							
						}
						
						//reset all the client in the deleted room to mainhall
						ds.resetRoomID(client,true);
						
						//set the client owner to false
						ds.setOwner(client, false);
						
						//send deleteroom to other server
						for(String[] s:ds.getServerInfo()){
							sendToServer(s[0],deleteroomToServer(Server.serverID, roomid));
						}
						
						//send deleteroom to current client
						clientBW.write(deleteroomToClient(roomid, "true"));
						clientBW.flush();
					}
				}
				
				/**
				 * Function 4.7 Broadcast the message to all clients in the char room
				 */
				
				if(getMessage(message,"type").equals("message")){
					
					//Get the content send by current client
					String content = getMessage(message,"content");
					
					//Get the identity of current client
					String identity = ds.getIdentity(client);
					
					//Broadcase above message to other clients in the char room
					ds.sentToClientsInSameChatRoom(client, content(identity, content), false);
				}
				
				/**
				 * Function 4.8 client quit
				 */
				if(getMessage(message,"type").equals("quit")){
					
					//get the roomid of current client
					String roomid = ds.getRoomid(client);
					
					//get the identity of currnet client;
					String identity = ds.getIdentity(client);
					
					//check if the client is the room owner
					if(!ds.isOwner(client)){
						//not the owner, reply with roomchange and quit directly
						ds.sentToClientsInSameChatRoom(client, roomchange(identity, roomid, ""), true);
						
						//remove the client from serverdatabse
						ds.deleteClient(client);

					}else{
						//is the owner
						
						//get all client in the deleted room
						ArrayList<String> clientInCurrentRoom = ds.getAllIdentityInSameRoom(client, false);
						
						//tell every client in the room, the current client is quit
						ds.sentToClientsInSameChatRoom(client, roomchange(identity, roomid, ""), false);
					
						for(String str:clientInCurrentRoom){	
							//tell every client in the room, the other client is moving to mainhall
							ds.sentToClientsInSameChatRoom(client, roomchange(str, roomid, "MainHall-"+Server.serverID), false);
							
							//inform the clients in mainall, some clients are moving in 
							ds.sentToClientsInMainHall(roomchange(str, roomid, "MainHall-"+Server.serverID));
						}						
						
						//reset thr roomid to mainhall
						ds.resetRoomID(client,false);
						
						//tell other server to delete the roomid
						for(String[] s:ds.getServerInfo()){
							sendToServer(s[0],deleteroomToServer(Server.serverID, roomid));
						}
						
						//send deleteroom to current client
						clientBW.write(deleteroomToClient(roomid, "true"));
						clientBW.flush();
																
						//send roomchange to client and client will quit
						clientBW.write(roomchange(identity, roomid, ""));
						clientBW.flush();
						
						//remove the client from server database
						ds.deleteClient(client);					
					}
				}
			}
			
		} catch (Exception e) {

			//When the client end break down or quit, delete the client from current user
			//ds.deleteClient(client);
			
			//get the roomid of current client
			String roomid = ds.getRoomid(client);
			
			//get the identity of currnet client;
			String identity = ds.getIdentity(client);
			
			//check if the client is the room owner
			if(!ds.isOwner(client)){
				//not the owner, reply with roomchange and quit directly
				ds.sentToClientsInSameChatRoom(client, roomchange(identity, roomid, ""), true);
				
				//remove the client from serverdatabse
				ds.deleteClient(client);

			}else{
				//is the owner
				
				//get all client in the deleted room
				ArrayList<String> clientInCurrentRoom = ds.getAllIdentityInSameRoom(client, false);
				
				//tell every client in the room, the current client is quit
				ds.sentToClientsInSameChatRoom(client, roomchange(identity, roomid, ""), false);
			
				for(String str:clientInCurrentRoom){	
					//tell every client in the room, the other client is moving to mainhall
					ds.sentToClientsInSameChatRoom(client, roomchange(str, roomid, "MainHall-"+Server.serverID), false);
					
					//inform the clients in mainall, some clients are moving in 
					ds.sentToClientsInMainHall(roomchange(str, roomid, "MainHall-"+Server.serverID));
				}						
				
				//reset thr roomid to mainhall
				ds.resetRoomID(client,false);
				
				//tell other server to delete the roomid
				for(String[] s:ds.getServerInfo()){
					sendToServer(s[0],deleteroomToServer(Server.serverID, roomid));
				}

				ds.deleteClient(client);
			}
		}	
	}
	
	public String getMessage(String message, String key){
		
		JSONObject messageJSON = null;
		
		try {
			messageJSON = (JSONObject) this.parser.parse(message);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return (String) messageJSON.get(key);
	}
	
	//create newidentity message
	@SuppressWarnings("unchecked")
	public String newidentity(String approved){
		
		String str = null;		
		reply.put("type", "newidentity");
		reply.put("approved",approved);		
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	@SuppressWarnings("unchecked")
	public String lockidentity(String serverid,String identity){
			
		String str = null;		
		reply.put("type", "lockidentity");
		reply.put("serverid",serverid);	
		reply.put("identity",identity);	
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	@SuppressWarnings("unchecked")
	public String releaseidentity(String serverid,String identity){
		
		String str = null;		
		reply.put("type", "releaseidentity");
		reply.put("serverid",serverid);	
		reply.put("identity",identity);	
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}

	@SuppressWarnings("unchecked")
	public String roomchange(String identity, String former, String roomid){
		
		String str = null;		
		reply.put("type", "roomchange");		
		reply.put("identity",identity);
		reply.put("former",former);
		reply.put("roomid",roomid);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String roomlist(ArrayList<String> rooms){
		
		String str = null;		
		reply.put("type", "roomlist");		
		reply.put("rooms",rooms);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String roomcontents(String roomid,ArrayList<String> identities,String owner){
		
		String str = null;		
		reply.put("type","roomcontents");
		reply.put("roomid",roomid);
		reply.put("identities", identities);
		reply.put("owner", owner);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String createroom(String roomid,String approved){
		
		String str = null;		
		reply.put("type","createroom");
		reply.put("roomid",roomid);
		reply.put("approved", approved);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String lockroomid(String serverid,String roomid){
		
		String str = null;		
		reply.put("type","lockroomid");
		reply.put("serverid",serverid);
		reply.put("roomid", roomid);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String releaseroomid(String serverid,String roomid,String approved){
		
		String str = null;		
		reply.put("type","releaseroomid");
		reply.put("serverid",serverid);
		reply.put("roomid", roomid);
		reply.put("approved", approved);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String route(String roomid,String host,String port){
		
		String str = null;		
		reply.put("type","route");
		reply.put("roomid",roomid);
		reply.put("host", host);
		reply.put("port", port);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String content(String identity,String content){
		
		String str = null;		
		reply.put("type","message");
		reply.put("identity",identity);
		reply.put("content", content);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String serverchange(String approved,String serverid){
		
		String str = null;		
		reply.put("type","serverchange");
		reply.put("approved",approved);
		reply.put("serverid", serverid);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String deleteroomToClient(String roomid,String approved){
		
		String str = null;		
		reply.put("type","deleteroom");
		reply.put("roomid",roomid);
		reply.put("approved", approved);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}
	
	public String deleteroomToServer(String serverid,String roomid){
		
		String str = null;		
		reply.put("type","deleteroom");
		reply.put("serverid",serverid);
		reply.put("roomid", roomid);
		str = reply.toJSONString();	
		reply.clear();
		return str+"\n";
	}

	//send the message to requesting server and read the reply from that server
	public JSONObject sendToServerAndGetFeedback(String serverID,String message){
		
		JSONObject reply=null;
		
		try {						
			String[] info = ds.getInfo(serverID);
			//Create SSL socket and connect it to the remote server 
			SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(info[1], Integer.parseInt(info[3]));
			//Socket socket = new Socket(info[1],Integer.parseInt(info[3]));			
			BufferedWriter bw = new BufferedWriter(
											new OutputStreamWriter(
													socket.getOutputStream(),"UTF-8"));
			BufferedReader br = new BufferedReader(
									new InputStreamReader(
											socket.getInputStream(),"UTF-8"));			
			bw.write(message);
			bw.flush();
			reply = (JSONObject) parser.parse(br.readLine());
			socket.close();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return reply;
	}
	
	//send message to server
	public void sendToServer(String serverID,String message){
		
		try {						
			String[] info = ds.getInfo(serverID);
			//Create SSL socket and connect it to the remote server 
			SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(info[1], Integer.parseInt(info[3]));		
			BufferedWriter bw = new BufferedWriter(
											new OutputStreamWriter(
													socket.getOutputStream(),"UTF-8"));
			bw.write(message);
			bw.flush();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}
