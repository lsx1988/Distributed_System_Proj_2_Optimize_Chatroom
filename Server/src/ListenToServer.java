import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenToServer extends Thread{
	
	private ServerSocket server_server;

	public ListenToServer(ServerSocket server_server){
		this.server_server = server_server;
	}
	
	public void run(){
	
		try {
			while(true){

				//Get one server connection, set up the server thread
				Socket server = server_server.accept();
				new ServerConnection(server).start();
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}
