import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class ListenToHeart extends Thread{

	private SSLServerSocket server_heart;

	public ListenToHeart(SSLServerSocket server_heart){
		this.server_heart = server_heart;
	}
	
	public void run(){
	
		try {
			while(true){
				 SSLSocket socket =(SSLSocket)server_heart.accept();
				 new TempForHeart(socket).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}
