import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenToClient extends Thread{
	
	private ServerSocket server_client;

	public ListenToClient(ServerSocket server_client){
		this.server_client = server_client;
	}
	
	public void run(){
	
		try {
			while(true){
				
				//Get one client connection, set up the client thread
				Socket client = server_client.accept();				
				new ClientConnection(client).start();
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}