package au.edu.unimelb.tcp.client;

public class State {

	private String identity;
	private String roomId;
	private String userName;
	private String password;
	
	public State(String identity, String roomId,String userName,String password) {
		this.identity = identity;
		this.roomId = roomId;
		this.userName = userName;
		this.password = password;
		
	}
	
	public synchronized String getRoomId() {
		return roomId;
	}
	public synchronized void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	public String getIdentity() {
		return identity;
	}
	
	public String getUsername(){
		return userName;
	}
	
	public String getPassword(){
		return password;
	}	
}
