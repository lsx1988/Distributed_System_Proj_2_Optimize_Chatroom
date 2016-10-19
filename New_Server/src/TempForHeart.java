import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLSocket;

public class TempForHeart extends Thread{
	
	private SSLSocket server;
	
	public TempForHeart(SSLSocket server){
		this.server=server;
	}
	
	public void run(){
		
		try {
					
			//create server socket bw and br
			BufferedReader serverBR = new BufferedReader(
											new InputStreamReader(
												server.getInputStream(),"UTF-8"));
			
			//get message from any other server
			String message = serverBR.readLine();
			System.out.println(message);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return;
		}
	}

}
