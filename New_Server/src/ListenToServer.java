import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class ListenToServer extends Thread{
	
	private SSLServerSocket server_server;

	public ListenToServer(SSLServerSocket server_server){
		this.server_server = server_server;
	}
	
	public void run(){
	
		try {
			while(true){

				//Get one server connection, set up the server thread
				SSLSocket server = (SSLSocket) server_server.accept();
				new ServerConnection(server).start();
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}
