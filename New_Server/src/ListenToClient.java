import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class ListenToClient extends Thread{
	
	private SSLServerSocket server_client;

	public ListenToClient(SSLServerSocket server_client){
		this.server_client = server_client;
	}
	
	public void run(){
	
		try {
			while(true){
				
				//Get one client connection, set up the client thread
				SSLSocket client = (SSLSocket) server_client.accept();				
				new ClientConnection(client).start();
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}